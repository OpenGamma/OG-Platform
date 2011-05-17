/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.apache.commons.lang.ObjectUtils;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calc.BatchExecutor;
import com.opengamma.engine.view.calc.DependencyGraphExecutor;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.engine.view.calc.SingleNodeExecutor;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.financial.batch.AdHocBatchDbManager;
import com.opengamma.financial.batch.AdHocBatchJobRun;
import com.opengamma.financial.batch.AdHocBatchResult;
import com.opengamma.financial.batch.BatchDataSearchRequest;
import com.opengamma.financial.batch.BatchDataSearchResult;
import com.opengamma.financial.batch.BatchError;
import com.opengamma.financial.batch.BatchErrorSearchRequest;
import com.opengamma.financial.batch.BatchErrorSearchResult;
import com.opengamma.financial.batch.BatchId;
import com.opengamma.financial.batch.BatchJobRun;
import com.opengamma.financial.batch.BatchMaster;
import com.opengamma.financial.batch.BatchResultWriterExecutor;
import com.opengamma.financial.batch.BatchSearchRequest;
import com.opengamma.financial.batch.BatchSearchResult;
import com.opengamma.financial.batch.BatchSearchResultItem;
import com.opengamma.financial.batch.BatchStatus;
import com.opengamma.financial.batch.LiveDataValue;
import com.opengamma.financial.batch.SnapshotId;
import com.opengamma.financial.conversion.ResultConverterCache;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.InetAddressUtils;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbHelper;
import com.opengamma.util.db.DbSource;
import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;

/**
 * A batch master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the batch master using an SQL database.
 * This implementation uses Hibernate to write all static data, including LiveData snapshots.
 * Risk itself is written using direct JDBC.
 * <p>
 * Full details of the API are in {@link BatchMaster}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbBatchMaster implements BatchMaster, AdHocBatchDbManager {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbBatchMaster.class);
  /**
   * The database schema.
   */
  private static String s_dbSchema = "";

  /**
   * The database connection.
   */
  private DbSource _dbSource;
  /**
   * The Hibernate template.
   */
  private HibernateTemplate _hibernateTemplate;

  //-------------------------------------------------------------------------
  public static synchronized String getDatabaseSchema() {
    return s_dbSchema;
  }

  public static synchronized void setDatabaseSchema(String schema) {
    if (schema == null) {
      s_dbSchema = "";      
    } else {
      s_dbSchema = schema  + ".";
    }
  }

  //-------------------------------------------------------------------------
  public PlatformTransactionManager getTransactionManager() {
    return _dbSource.getTransactionManager();
  }

  public SessionFactory getSessionFactory() {
    return _dbSource.getHibernateSessionFactory();
  }

  public HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
  }

  public SimpleJdbcTemplate getJdbcTemplate() {
    return _dbSource.getJdbcTemplate();
  }

  public DbHelper getDbHelper() {
    return _dbSource.getDialect();
  }

  public void setDbSource(DbSource dbSource) {
    ArgumentChecker.notNull(dbSource, "dbSource");
    _dbSource = dbSource;
    _hibernateTemplate = new HibernateTemplate(_dbSource.getHibernateSessionFactory());
    _hibernateTemplate.setAllowCreate(false);
  }

  //-------------------------------------------------------------------------
  /*package*/OpenGammaVersion getOpenGammaVersion(final BatchJobRun job) {
    OpenGammaVersion version = getHibernateTemplate().execute(new HibernateCallback<OpenGammaVersion>() {
      @Override
      public OpenGammaVersion doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("OpenGammaVersion.one.byVersion");
        query.setString("version", job.getOpenGammaVersion());
        return (OpenGammaVersion) query.uniqueResult();
      }
    });
    if (version == null) {
      version = new OpenGammaVersion();
      version.setVersion(job.getOpenGammaVersion());
      getHibernateTemplate().save(version);
    }
    return version;
  }

  /*package*/ObservationTime getObservationTime(final BatchJobRun job) {
    return getObservationTime(job.getObservationTime());
  }

  /*package*/ ObservationTime getObservationTime(final String label) {
    ObservationTime observationTime = getHibernateTemplate().execute(new HibernateCallback<ObservationTime>() {
      @Override
      public ObservationTime doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("ObservationTime.one.byLabel");
        query.setString("label", label);
        return (ObservationTime) query.uniqueResult();
      }
    });
    if (observationTime == null) {
      observationTime = new ObservationTime();
      observationTime.setLabel(label);
      getHibernateTemplate().save(observationTime);
    }
    return observationTime;
  }
 
  
  /*package*/ ObservationDateTime getObservationDateTime(final BatchJobRun job) {
    return getObservationDateTime(job.getObservationDate(), job.getObservationTime());     
  }
  
  /*package*/ ObservationDateTime getObservationDateTime(final LocalDate observationDate, final String observationTime) {
    ObservationDateTime dateTime = getHibernateTemplate().execute(new HibernateCallback<ObservationDateTime>() {
      @Override
      public ObservationDateTime doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("ObservationDateTime.one.byDateAndTime");
        query.setDate("date", DbDateUtils.toSqlDate(observationDate));
        query.setString("time", observationTime);
        return (ObservationDateTime) query.uniqueResult();
      }
    });
    if (dateTime == null) {
      dateTime = new ObservationDateTime();
      dateTime.setDate(DbDateUtils.toSqlDate(observationDate));
      dateTime.setObservationTime(getObservationTime(observationTime));
      getHibernateTemplate().save(dateTime);
    }
    return dateTime;
  }
  
  /*package*/ ComputeHost getComputeHost(final String hostName) {
    ComputeHost computeHost = getHibernateTemplate().execute(new HibernateCallback<ComputeHost>() {
      @Override
      public ComputeHost doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("ComputeHost.one.byHostName");
        query.setString("hostName", hostName);
        return (ComputeHost) query.uniqueResult();
      }
    });
    if (computeHost == null) {
      computeHost = new ComputeHost();
      computeHost.setHostName(hostName);
      getHibernateTemplate().save(computeHost);
    }
    return computeHost;
  }
 
  /*package*/ ComputeHost getLocalComputeHost() {
    return getComputeHost(InetAddressUtils.getLocalHostName());
  }
  
  /*package*/ ComputeNode getComputeNode(final String nodeId) {
    String hostName = nodeId; 
    int slashIndex = nodeId.indexOf('/'); // e.g., mymachine-t5500/0/1, see LocalCalculationNode.java. Should refactor nodeId to a class with two strings, host and node id
    if (slashIndex != -1) {
      hostName = nodeId.substring(0, slashIndex);      
    }
    final ComputeHost host = getComputeHost(hostName);
    
    ComputeNode node = getHibernateTemplate().execute(new HibernateCallback<ComputeNode>() {
      @Override
      public ComputeNode doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("ComputeNode.one.byNodeName");
        query.setString("nodeName", nodeId);
        return (ComputeNode) query.uniqueResult();
      }
    });
    if (node == null) {
      node = new ComputeNode();
      node.setComputeHost(host);
      node.setNodeName(nodeId);
      getHibernateTemplate().save(node);
    }
    return node;
  }
  
  /*package*/ ComputeNode getLocalComputeNode() {
    return getComputeNode(InetAddressUtils.getLocalHostName());
  }
  
  /*package*/ LiveDataSnapshot getLiveDataSnapshot(final BatchJobRun job) {

    LiveDataSnapshot liveDataSnapshot = getLiveDataSnapshot(
        job.getSnapshotObservationDate(),
        job.getSnapshotObservationTime());
    
    if (liveDataSnapshot == null) {
      throw new IllegalArgumentException("Snapshot for " 
          + job.getSnapshotObservationDate() 
          + "/" 
          + job.getSnapshotObservationTime() 
          + " cannot be found");
    }
    
    return liveDataSnapshot;
  }
  
  /*package*/ LiveDataSnapshot getLiveDataSnapshot(final LocalDate observationDate, final String observationTime) {
    LiveDataSnapshot liveDataSnapshot = getHibernateTemplate().execute(new HibernateCallback<LiveDataSnapshot>() {
      @Override
      public LiveDataSnapshot doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("LiveDataSnapshot.one.byDateAndTime");
        query.setDate("date", DbDateUtils.toSqlDate(observationDate));
        query.setString("time", observationTime);
        return (LiveDataSnapshot) query.uniqueResult();
      }
    });
    return liveDataSnapshot;
  }
  
  /*package*/ LiveDataField getLiveDataField(final String fieldName) {
    LiveDataField field = getHibernateTemplate().execute(new HibernateCallback<LiveDataField>() {
      @Override
      public LiveDataField doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("LiveDataField.one.byName");
        query.setString("name", fieldName);
        return (LiveDataField) query.uniqueResult();
      }
    });
    if (field == null) {
      field = new LiveDataField();
      field.setName(fieldName);
      getHibernateTemplate().save(field);
    }
    return field;
  }
  
  private ComputationTarget getComputationTargetImpl(final ComputationTargetSpecification spec) {
    ComputationTarget computationTarget = getHibernateTemplate().execute(new HibernateCallback<ComputationTarget>() {
      @Override
      public ComputationTarget doInHibernate(Session session) throws HibernateException, SQLException {
        Query query;
        if (spec.getUniqueId().getVersion() == null) {
          query = session.getNamedQuery("ComputationTarget.one.byUniqueIdNullVersion");
        } else {
          query = session.getNamedQuery("ComputationTarget.one.byUniqueIdNonNullVersion");
          query.setString("idVersion", spec.getUniqueId().getVersion());
        }
        query.setInteger("computationTargetType", ComputationTarget.getType(spec.getType()));
        query.setString("idScheme", spec.getUniqueId().getScheme());
        query.setString("idValue", spec.getUniqueId().getValue());
        return (ComputationTarget) query.uniqueResult();
      }
    });
    return computationTarget;
  }
  
  /*package*/ ComputationTarget getComputationTarget(final ComputationTargetSpecification spec) {
    ComputationTarget computationTarget = getComputationTargetImpl(spec);
    if (computationTarget == null) {
      computationTarget = new ComputationTarget();
      computationTarget.setComputationTargetType(spec.getType());
      computationTarget.setIdScheme(spec.getUniqueId().getScheme());      
      computationTarget.setIdValue(spec.getUniqueId().getValue());
      computationTarget.setIdVersion(spec.getUniqueId().getVersion());
      getHibernateTemplate().save(computationTarget);
    }
    return computationTarget;
  }
  
  /*package*/ ComputationTarget getComputationTarget(final com.opengamma.engine.ComputationTarget ct) {
    ComputationTarget computationTarget = getComputationTargetImpl(ct.toSpecification());
    if (computationTarget == null) {
      computationTarget = new ComputationTarget();
      computationTarget.setComputationTargetType(ct.getType());
      computationTarget.setIdScheme(ct.getUniqueId().getScheme());      
      computationTarget.setIdValue(ct.getUniqueId().getValue());
      computationTarget.setIdVersion(ct.getUniqueId().getVersion());
      computationTarget.setName(ct.getName());
      getHibernateTemplate().save(computationTarget);
    } else {
      if (!ObjectUtils.equals(computationTarget.getName(), ct.getName())) {
        computationTarget.setName(ct.getName());
        getHibernateTemplate().update(computationTarget);
      }
    }
    return computationTarget;
  }
  
  /*package*/ RiskValueName getRiskValueName(final String name) {
    RiskValueName riskValueName = getHibernateTemplate().execute(new HibernateCallback<RiskValueName>() {
      @Override
      public RiskValueName doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("RiskValueName.one.byName");
        query.setString("name", name);
        return (RiskValueName) query.uniqueResult();
      }
    });
    if (riskValueName == null) {
      riskValueName = new RiskValueName();
      riskValueName.setName(name);
      getHibernateTemplate().save(riskValueName);
    }
    return riskValueName;
  }
  
  /*package*/ FunctionUniqueId getFunctionUniqueId(final String uniqueId) {
    FunctionUniqueId functionUniqueId = getHibernateTemplate().execute(new HibernateCallback<FunctionUniqueId>() {
      @Override
      public FunctionUniqueId doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("FunctionUniqueId.one.byUniqueId");
        query.setString("uniqueId", uniqueId);
        return (FunctionUniqueId) query.uniqueResult();
      }
    });
    if (functionUniqueId == null) {
      functionUniqueId = new FunctionUniqueId();
      functionUniqueId.setUniqueId(uniqueId);
      getHibernateTemplate().save(functionUniqueId);
    }
    return functionUniqueId;
  }
  
  /*package*/ RiskRun getRiskRunFromDb(final LocalDate observationDate, final String observationTime) {
    RiskRun riskRun = getHibernateTemplate().execute(new HibernateCallback<RiskRun>() {
      @Override
      public RiskRun doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("RiskRun.one.byRunTime");
        query.setDate("runDate", DbDateUtils.toSqlDate(observationDate));
        query.setString("runTime", observationTime);
        return (RiskRun) query.uniqueResult();
      }
    });
    
    return riskRun;
  }

  /*package*/ RiskRun getRiskRunFromDb(final BatchJobRun job) {
    return getRiskRunFromDb(
        job.getObservationDate(),
        job.getObservationTime());
  }
  
  /*package*/ RiskRun createRiskRun(final BatchJobRun job) {
    Instant now = job.getCreationTime();
    
    LiveDataSnapshot snapshot = getLiveDataSnapshot(job);
    
    RiskRun riskRun = new RiskRun();
    riskRun.setOpenGammaVersion(getOpenGammaVersion(job));
    riskRun.setMasterProcessHost(getLocalComputeHost());
    riskRun.setRunTime(getObservationDateTime(job));
    riskRun.setLiveDataSnapshot(snapshot);
    riskRun.setCreateInstant(DbDateUtils.toSqlTimestamp(now));
    riskRun.setStartInstant(DbDateUtils.toSqlTimestamp(now));
    riskRun.setNumRestarts(0);
    riskRun.setComplete(false);
    
    for (Map.Entry<String, String> parameter : job.getParametersMap().entrySet()) {
      riskRun.addProperty(parameter.getKey(), parameter.getValue());      
    }
    
    for (String calcConf : job.getCalculationConfigurations()) {
      riskRun.addCalculationConfiguration(calcConf);
    }
    
    getHibernateTemplate().save(riskRun);
    getHibernateTemplate().saveOrUpdateAll(riskRun.getCalculationConfigurations());
    getHibernateTemplate().saveOrUpdateAll(riskRun.getProperties());
    
    job.setOriginalCreationTime(job.getCreationTime().toInstant());
    
    return riskRun;
  }
  
  /*package*/ void deleteRun(RiskRun riskRun) {
    deleteRiskValues(riskRun);
    deleteRiskFailures(riskRun);
    getHibernateTemplate().deleteAll(riskRun.getCalculationConfigurations());
    getHibernateTemplate().deleteAll(riskRun.getProperties());
    getHibernateTemplate().delete(riskRun);
    getHibernateTemplate().flush();
  }
  
  /*package*/ void restartRun(BatchJobRun batch, RiskRun riskRun) {
    Instant now = Instant.now();
    
    riskRun.setOpenGammaVersion(getOpenGammaVersion(batch));
    riskRun.setMasterProcessHost(getLocalComputeHost());
    riskRun.setStartInstant(DbDateUtils.toSqlTimestamp(now));
    riskRun.setNumRestarts(riskRun.getNumRestarts() + 1);
    riskRun.setComplete(false);
    
    getHibernateTemplate().update(riskRun);
    
    deleteRiskFailures(riskRun);
    
    batch.setOriginalCreationTime(Instant.ofEpochMillis(riskRun.getCreateInstant().getTime()));
  }
  
  private void deleteRiskValues(RiskRun riskRun) {
    MapSqlParameterSource parameters = new MapSqlParameterSource()
      .addValue("run_id", riskRun.getId());
    getJdbcTemplate().update(RiskValue.sqlDeleteRiskValues(), parameters);
  }

  private void deleteRiskFailures(RiskRun riskRun) {
    MapSqlParameterSource parameters = new MapSqlParameterSource()
      .addValue("run_id", riskRun.getId());
    getJdbcTemplate().update(FailureReason.sqlDeleteRiskFailureReasons(), parameters);
    getJdbcTemplate().update(RiskFailure.sqlDeleteRiskFailures(), parameters);
  }
  
  /*package*/ void endRun(RiskRun riskRun) {
    Instant now = Instant.now();
    
    riskRun.setEndInstant(DbDateUtils.toSqlTimestamp(now));
    riskRun.setComplete(true);
    
    getHibernateTemplate().update(riskRun);
  }
  
  /*package*/ RiskRun getRiskRunFromHandle(BatchJobRun job) {
    return getDbHandle(job)._riskRun;
  }
  
  private DbHandle getDbHandle(BatchJobRun job) {
    Object handle = job.getDbHandle();
    if (handle == null) {
      throw new IllegalStateException("Job db handle is null");
    }
    if (!(handle instanceof DbHandle)) {
      throw new IllegalStateException("Job db handle must be of type DbHandle, was " + handle.getClass());
    }
    return (DbHandle) handle;
  }
  
  /*package*/ Set<RiskValueName> populateRiskValueNames(BatchJobRun job) {
    Set<RiskValueName> returnValue = new HashSet<RiskValueName>();
    
    Set<String> riskValueNames = job.getAllOutputValueNames();
    for (String name : riskValueNames) {
      RiskValueName riskValueName = getRiskValueName(name);
      returnValue.add(riskValueName);
    }
    
    return returnValue;
  }
  
  /*package*/ Set<ComputationTarget> populateComputationTargets(BatchJobRun job) {
    Set<ComputationTarget> returnValue = new HashSet<ComputationTarget>();
    
    Collection<ComputationTargetSpecification> computationTargetSpecs = job.getAllComputationTargets();
    for (ComputationTargetSpecification spec : computationTargetSpecs) {
      com.opengamma.engine.ComputationTarget inMemoryTarget = job.resolve(spec);
      com.opengamma.masterdb.batch.ComputationTarget dbTarget;
      if (inMemoryTarget != null) {
        dbTarget = getComputationTarget(inMemoryTarget);
      } else {
        dbTarget = getComputationTarget(spec);
      }
      returnValue.add(dbTarget);
    }    
    
    return returnValue;
  }
  
  // --------------------------------------------------------------------------
  

  @Override
  public void addValuesToSnapshot(SnapshotId snapshotId, Set<LiveDataValue> values) {
    try {
      getSessionFactory().getCurrentSession().beginTransaction();
      
      addValuesToSnapshotImpl(snapshotId, values);
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  private void addValuesToSnapshotImpl(SnapshotId snapshotId, Set<LiveDataValue> values) {
    s_logger.info("Adding {} values to LiveData snapshot {}", values.size(), snapshotId);
    
    LiveDataSnapshot snapshot = getLiveDataSnapshot(snapshotId.getObservationDate(), snapshotId.getObservationTime());
    if (snapshot == null) {
      throw new IllegalArgumentException("Snapshot " + snapshotId + " cannot be found");
    }
    
    Collection<LiveDataSnapshotEntry> changedEntries = new ArrayList<LiveDataSnapshotEntry>();
    for (LiveDataValue value : values) {
      LiveDataSnapshotEntry entry = snapshot.getEntry(value.getComputationTargetSpecification(), value.getFieldName());
      if (entry != null) {
        if (entry.getValue() != value.getValue()) {
          entry.setValue(value.getValue());
          changedEntries.add(entry);
        }
      } else {
        entry = new LiveDataSnapshotEntry();
        entry.setSnapshot(snapshot);
        entry.setComputationTarget(getComputationTarget(value.getComputationTargetSpecification()));
        entry.setField(getLiveDataField(value.getFieldName()));
        entry.setValue(value.getValue());
        snapshot.addEntry(entry);
        changedEntries.add(entry);
      }
    }
    
    getHibernateTemplate().saveOrUpdateAll(changedEntries);
  }

  @Override
  public void createLiveDataSnapshot(SnapshotId snapshotId) {
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      createLiveDataSnapshotImpl(snapshotId);
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  private void createLiveDataSnapshotImpl(SnapshotId snapshotId) {
    s_logger.info("Creating LiveData snapshot {} ", snapshotId);

    LiveDataSnapshot snapshot = getLiveDataSnapshot(snapshotId.getObservationDate(), snapshotId.getObservationTime());
    if (snapshot != null) {
      s_logger.info("Snapshot " + snapshotId + " already exists. No need to create.");
      return;
    }
    
    snapshot = new LiveDataSnapshot();
    
    ObservationDateTime snapshotTime = getObservationDateTime(
        snapshotId.getObservationDate(), 
        snapshotId.getObservationTime());
    snapshot.setSnapshotTime(snapshotTime);
    
    getHibernateTemplate().save(snapshot);
  }

  @Override
  public void endBatch(BatchJobRun batch) {
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      endBatchImpl(batch);
    
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  private void endBatchImpl(BatchJobRun batch) {
    s_logger.info("Ending batch {}", batch);
    
    RiskRun run = getRiskRunFromHandle(batch);
    endRun(run);
  }

  @Override
  public void fixLiveDataSnapshotTime(SnapshotId snapshotId, OffsetTime fix) {
    s_logger.info("Fixing LiveData snapshot {} at {}", snapshotId, fix);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      LiveDataSnapshot snapshot = getLiveDataSnapshot(snapshotId.getObservationDate(), snapshotId.getObservationTime());
      
      if (snapshot == null) {
        throw new IllegalArgumentException("Snapshot " + snapshotId + " cannot be found");
      }
      
      ObservationDateTime snapshotTime = snapshot.getSnapshotTime(); 
      snapshotTime.setTime(DbDateUtils.toSqlTime(fix));
      getHibernateTemplate().save(snapshotTime);
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  @Override
  public Set<LiveDataValue> getSnapshotValues(SnapshotId snapshotId) {
    s_logger.info("Getting LiveData snapshot {}", snapshotId);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      LiveDataSnapshot liveDataSnapshot = getLiveDataSnapshot(
          snapshotId.getObservationDate(), 
          snapshotId.getObservationTime());
      
      if (liveDataSnapshot == null) {
        throw new IllegalArgumentException("Snapshot " + snapshotId + " cannot be found");
      }
      
      Set<LiveDataValue> returnValues = new HashSet<LiveDataValue>();
      for (LiveDataSnapshotEntry entry : liveDataSnapshot.getSnapshotEntries()) {
        returnValues.add(entry.toLiveDataValue());      
      }
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
      return returnValues;
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }
  
  @Override
  public void deleteBatch(BatchJobRun batch) {
    s_logger.info("Deleting batch {}", batch);
    try {
      getSessionFactory().getCurrentSession().beginTransaction();
      
      RiskRun run = getRiskRunFromDb(batch);
      if (run == null) {
        throw new IllegalStateException("Batch " + batch + " does not exist");
      }
      deleteRun(run);
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  @Override
  public void startBatch(BatchJobRun batch) {
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      startBatchImpl(batch);
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  private void startBatchImpl(BatchJobRun batch) {
    s_logger.info("Starting batch {}", batch);
    
    RiskRun run;
    switch (batch.getRunCreationMode()) {
      case AUTO:
        run = getRiskRunFromDb(batch);
        
        if (run != null) {
          // also check parameter equality
          Map<String, String> existingProperties = run.getPropertiesMap();
          Map<String, String> newProperties = batch.getParametersMap();
          
          if (!existingProperties.equals(newProperties)) {
            Set<Map.Entry<String, String>> symmetricDiff = 
              Sets.symmetricDifference(existingProperties.entrySet(), newProperties.entrySet());
            throw new IllegalStateException("Run parameters stored in DB differ from new parameters with respect to: " + symmetricDiff);
          }
        }
        
        if (run == null) {
          run = createRiskRun(batch);
        } else {
          restartRun(batch, run);
        }
        break;
        
      case CREATE_NEW_OVERWRITE:
        run = getRiskRunFromDb(batch);
        if (run != null) {
          deleteRun(run);            
        }
        
        run = createRiskRun(batch);
        break;
          
      case CREATE_NEW:
        run = createRiskRun(batch);
        break;
      
      case REUSE_EXISTING:
        run = getRiskRunFromDb(batch);
        if (run == null) {
          throw new IllegalStateException("Cannot find run in database for " + batch);
        }
        restartRun(batch, run);
        break;
      
      default:
        throw new RuntimeException("Unexpected run creation mode " + batch.getRunCreationMode());
    }

    // make sure calc conf collection is inited
    for (CalculationConfiguration cc : run.getCalculationConfigurations()) { 
      assert cc != null;
    }
    
    Set<RiskValueName> riskValueNames = populateRiskValueNames(batch);
    Set<ComputationTarget> computationTargets = populateComputationTargets(batch);
    
    DbHandle dbHandle = new DbHandle();
    dbHandle._riskRun = run;
    dbHandle._riskValueNames = riskValueNames;
    dbHandle._computationTargets = computationTargets;
    
    batch.setDbHandle(dbHandle);
  }
  
  private static class DbHandle {
    private RiskRun _riskRun;
    private Set<RiskValueName> _riskValueNames;
    private Set<ComputationTarget> _computationTargets;
  }
  
  @Override
  public DependencyGraphExecutorFactory<Object> createDependencyGraphExecutorFactory(BatchJobRun batch) {
    return new BatchResultWriterFactory(batch);
  }
  
  public CommandLineBatchResultWriter createTestResultWriter(BatchJobRun batch) {
    BatchResultWriterFactory factory = new BatchResultWriterFactory(batch);
    return factory.createTestWriter();    
  }
  
  private class BatchResultWriterFactory implements DependencyGraphExecutorFactory<Object> {
    
    private final BatchJobRun _batch;
    
    public BatchResultWriterFactory(BatchJobRun batch) {
      ArgumentChecker.notNull(batch, "batch");
      _batch = batch;
    }
    
    @Override
    public BatchExecutor createExecutor(SingleComputationCycle cycle) {
      
      Map<String, ViewComputationCache> cachesByCalculationConfiguration = cycle.getCachesByCalculationConfiguration();
      
      CommandLineBatchResultWriter writer = new CommandLineBatchResultWriter(
          _dbSource,
          cycle.getViewDefinition().getResultModelDefinition(),
          cachesByCalculationConfiguration,
          getDbHandle(_batch)._computationTargets,
          getRiskRunFromHandle(_batch),
          getDbHandle(_batch)._riskValueNames);
      
      // Ultimate executor of the tasks
      DependencyGraphExecutor<CalculationJobResult> level3Executor =
        new SingleNodeExecutor(cycle);
      
      // 'Wrapper' executor that will write
      // results from the underlying executor 
      // to batch DB as soon as they are received
      // and pass the result back to level 1 executor
      BatchResultWriterExecutor level2Executor =
        new BatchResultWriterExecutor(
            writer,
            level3Executor);
      
      // This executor is needed to guarantee that
      // BatchResultWriterImpl.write() is called once
      // and once only for each computation target
      BatchExecutor level1Executor =
        new BatchExecutor(level2Executor);
      
      return level1Executor;
    }
    
    public CommandLineBatchResultWriter createTestWriter() {
      CommandLineBatchResultWriter resultWriter = new CommandLineBatchResultWriter(
          _dbSource,
          new ResultModelDefinition(),
          new HashMap<String, ViewComputationCache>(),
          getDbHandle(_batch)._computationTargets,
          getRiskRunFromHandle(_batch),
          getDbHandle(_batch)._riskValueNames);
      
      return resultWriter;
    }

  }
  
  public static Class<?>[] getHibernateMappingClasses() {
    return new HibernateBatchDbFiles().getHibernateMappingFiles();
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public BatchSearchResult search(BatchSearchRequest request) {
    DetachedCriteria criteria = DetachedCriteria.forClass(RiskRun.class);
    DetachedCriteria runTimeCriteria = criteria.createCriteria("runTime");
    DetachedCriteria observationTimeCriteria = runTimeCriteria.createCriteria("observationTime");
    
    if (request.getObservationDate() != null) {
      runTimeCriteria.add(
          Restrictions.eq("date", DbDateUtils.toSqlDate(request.getObservationDate())));
    }
    
    if (request.getObservationTime() != null) {
      observationTimeCriteria.add(
          Restrictions.sqlRestriction("UPPER(label) like UPPER(?)", 
              getDbHelper().sqlWildcardAdjustValue(request.getObservationTime()), Hibernate.STRING));
    }
    
    BatchSearchResult result = new BatchSearchResult();
    try {
      getSessionFactory().getCurrentSession().beginTransaction();
      
      if (request.getPagingRequest().equals(PagingRequest.ALL)) {
        result.setPaging(Paging.of(request.getPagingRequest(), result.getItems()));
      } else {
        criteria.setProjection(Projections.rowCount());
        Long totalCount = (Long) getHibernateTemplate().findByCriteria(criteria).get(0);
        result.setPaging(Paging.of(request.getPagingRequest(), totalCount.intValue()));
        criteria.setProjection(null);
        criteria.setResultTransformer(Criteria.ROOT_ENTITY);
      }
      
      runTimeCriteria.addOrder(Order.asc("date"));
      observationTimeCriteria.addOrder(Order.asc("label"));

      List<RiskRun> runs = getHibernateTemplate().findByCriteria(
          criteria,
          request.getPagingRequest().getFirstItemIndex(),
          request.getPagingRequest().getPagingSize());
      
      for (RiskRun run : runs) {
        BatchSearchResultItem item = new BatchSearchResultItem();
        item.setObservationDate(DbDateUtils.fromSqlDate(run.getRunTime().getDate()));      
        item.setObservationTime(run.getRunTime().getObservationTime().getLabel());
        item.setStatus(run.isComplete() ? BatchStatus.COMPLETE : BatchStatus.RUNNING);
        result.getItems().add(item);
      }
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }

    return result;
  }
  
  @Override
  public BatchDataSearchResult getResults(BatchDataSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObservationDate(), "observationDate");
    ArgumentChecker.notNull(request.getObservationTime(), "observationTime");
    
    RiskRun riskRun;
    try {
      getSessionFactory().getCurrentSession().beginTransaction();
      riskRun = getRiskRunFromDb(request.getObservationDate(), request.getObservationTime());
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
    
    if (riskRun == null) {
      throw new IllegalArgumentException("Batch " + request.getObservationDate() + "/" + request.getObservationTime() + " not found");
    }

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("rsk_run_id", riskRun.getId());
    
    final int count = _dbSource.getJdbcTemplate().queryForInt(ViewResultEntryMapper.sqlCount(), params);
    
    String sql = getDbHelper().sqlApplyPaging(ViewResultEntryMapper.sqlGet(), " ", request.getPagingRequest());
    
    List<ViewResultEntry> values = _dbSource.getJdbcTemplate().query(
        sql, 
        ViewResultEntryMapper.ROW_MAPPER, 
        params);
    
    BatchDataSearchResult result = new BatchDataSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), count));
    result.setItems(values);
    return result;
  }

  @Override
  public BatchErrorSearchResult getErrors(BatchErrorSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObservationDate(), "observationDate");
    ArgumentChecker.notNull(request.getObservationTime(), "observationTime");
    
    RiskRun riskRun;
    try {
      getSessionFactory().getCurrentSession().beginTransaction();
      riskRun = getRiskRunFromDb(request.getObservationDate(), request.getObservationTime());
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
    
    if (riskRun == null) {
      throw new IllegalArgumentException("Batch " + request.getObservationDate() + "/" + request.getObservationTime() + " not found");
    }

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("rsk_run_id", riskRun.getId());
    
    final int count = _dbSource.getJdbcTemplate().queryForInt(BatchErrorMapper.sqlCount(), params);
    
    String sql = getDbHelper().sqlApplyPaging(BatchErrorMapper.sqlGet(), " ", request.getPagingRequest());
    
    List<BatchError> values = _dbSource.getJdbcTemplate().query(
        sql, 
        BatchErrorMapper.ROW_MAPPER, 
        params);
    
    BatchErrorSearchResult result = new BatchErrorSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), count));
    result.setItems(values);
    return result;
  }
  
  public SnapshotId getAdHocBatchSnapshotId(BatchId batchId) {
    return new SnapshotId(batchId.getObservationDate(), "AD_HOC_" + Instant.now().toString());
  }

  @Override
  public void write(AdHocBatchResult result) {
    try {
      getSessionFactory().getCurrentSession().beginTransaction();
    
      SnapshotId snapshotId = getAdHocBatchSnapshotId(result.getBatchId());
    
      createLiveDataSnapshotImpl(snapshotId);
      
      Set<LiveDataValue> values = new HashSet<LiveDataValue>();
      for (ComputedValue liveData : result.getResult().getAllLiveData()) {
        values.add(new LiveDataValue(liveData));      
      }
      
      addValuesToSnapshotImpl(snapshotId, values);
      
      AdHocBatchJobRun batch = new AdHocBatchJobRun(result, snapshotId);
      startBatchImpl(batch);
      
      AdHocBatchResultWriter writer = new AdHocBatchResultWriter(
          _dbSource,
          getRiskRunFromHandle(batch),
          getLocalComputeNode(),
          new ResultConverterCache(),
          getDbHandle(batch)._computationTargets,
          getDbHandle(batch)._riskValueNames);
      
      writer.write(result.getResult());
      
      endBatchImpl(batch);
     
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }
  
}
