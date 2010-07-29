/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;
import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.ResultWriter;
import com.opengamma.engine.view.calcnode.ResultWriterFactory;
import com.opengamma.financial.batch.BatchDbManager;
import com.opengamma.financial.batch.BatchJob;
import com.opengamma.financial.batch.LiveDataValue;
import com.opengamma.financial.batch.SnapshotId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.InetAddressUtils;
import com.opengamma.util.test.DBTool;
import com.opengamma.util.time.DateUtil;

/**
 * This implementation uses Hibernate to write all static data, including LiveData snapshots.
 * <p>
 * Risk itself is written using direct JDBC, however.
 */
public class BatchDbManagerImpl implements BatchDbManager {
  
  private static final Logger s_logger = LoggerFactory
    .getLogger(BatchDbManagerImpl.class);
  
  private String _jdbcUrl;
  private String _username;
  private String _password;
  
  /**
   * The template for Hibernate operations.
   */
  private HibernateTemplate _hibernateTemplate;
  
  /**
   * The template for direct JDBC operations.
   */
  private SimpleJdbcTemplate _jdbcTemplate;
  
  // --------------------------------------------------------------------------
  
  
  public String getJdbcUrl() {
    return _jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl) {
    _jdbcUrl = jdbcUrl;
  }

  public String getUsername() {
    return _username;
  }

  public void setUsername(String username) {
    _username = username;
  }

  public String getPassword() {
    return _password;
  }

  public void setPassword(String password) {
    _password = password;
  }
  
  public void setSessionFactory(SessionFactory sessionFactory) {
    _hibernateTemplate = new HibernateTemplate(sessionFactory);
    _hibernateTemplate.setAllowCreate(false);
  }
  
  public void setTransactionManager(DataSourceTransactionManager transactionManager) {
    DataSource dataSource = transactionManager.getDataSource();
    _jdbcTemplate = new SimpleJdbcTemplate(dataSource);
  }
  
  public SessionFactory getSessionFactory() {
    return _hibernateTemplate.getSessionFactory();
  }
  
  public void initialize(DBTool tool, SessionFactory factory) {
    setJdbcUrl(tool.getJdbcUrl());
    setUsername(tool.getUser());
    setPassword(tool.getPassword());
    
    setSessionFactory(factory);
    DataSourceTransactionManager jdbcTransactionManager = tool.getTransactionManager();
    setTransactionManager(jdbcTransactionManager);
  }
  
  // --------------------------------------------------------------------------
  
  /*package*/ OpenGammaVersion getOpenGammaVersion(final BatchJob job) {
    OpenGammaVersion version = (OpenGammaVersion) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("OpenGammaVersion.one.byVersionAndHash");
        query.setString("version", job.getOpenGammaVersion());
        query.setString("hash", job.getOpenGammaVersionHash());
        return query.uniqueResult();
      }
    });
    if (version == null) {
      version = new OpenGammaVersion();
      version.setVersion(job.getOpenGammaVersion());
      version.setHash(job.getOpenGammaVersionHash());
      _hibernateTemplate.save(version);
    }
    return version;
  }
  
  
  /*package*/ ObservationTime getObservationTime(final BatchJob job) {
    return getObservationTime(job.getObservationTime());
  }
  
  /*package*/ ObservationTime getObservationTime(final String label) {
    ObservationTime observationTime = (ObservationTime) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ObservationTime.one.byLabel");
        query.setString("label", label);
        return query.uniqueResult();
      }
    });
    if (observationTime == null) {
      observationTime = new ObservationTime();
      observationTime.setLabel(label);
      _hibernateTemplate.save(observationTime);
    }
    return observationTime;
  }
 
  
  /*package*/ ObservationDateTime getObservationDateTime(final BatchJob job) {
    return getObservationDateTime(job.getObservationDate(), job.getObservationTime());     
  }
  
  /*package*/ ObservationDateTime getObservationDateTime(final LocalDate observationDate, final String observationTime) {
    ObservationDateTime dateTime = (ObservationDateTime) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ObservationDateTime.one.byDateAndTime");
        query.setDate("date", DateUtil.toSqlDate(observationDate));
        query.setString("time", observationTime);
        return query.uniqueResult();
      }
    });
    if (dateTime == null) {
      dateTime = new ObservationDateTime();
      dateTime.setDate(DateUtil.toSqlDate(observationDate));
      dateTime.setObservationTime(getObservationTime(observationTime));
      _hibernateTemplate.save(dateTime);
    }
    return dateTime;
  }
  
  /*package*/ ComputeHost getComputeHost(final String hostName) {
    ComputeHost computeHost = (ComputeHost) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ComputeHost.one.byHostName");
        query.setString("hostName", hostName);
        return query.uniqueResult();
      }
    });
    if (computeHost == null) {
      computeHost = new ComputeHost();
      computeHost.setHostName(hostName);
      _hibernateTemplate.save(computeHost);
    }
    return computeHost;
  }
 
  /*package*/ ComputeHost getLocalComputeHost() {
    return getComputeHost(InetAddressUtils.getLocalHostName());
  }
  
  /*package*/ ComputeNode getComputeNode(String nodeId) {
    // todo
    final ComputeHost host = getComputeHost(nodeId);
    
    ComputeNode node = (ComputeNode) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ComputeNode.one.byHostName");
        query.setString("hostName", host.getHostName());
        return query.uniqueResult();
      }
    });
    if (node == null) {
      node = new ComputeNode();
      node.setComputeHost(host);
      node.setConfigOid("UNDEFINED"); // TODO
      node.setConfigVersion(1);
      node.setNodeName(host.getHostName());
      _hibernateTemplate.save(node);
    }
    return node;
  }
  
  /*package*/ ComputeNode getLocalComputeNode() {
    return getComputeNode(InetAddressUtils.getLocalHostName());
  }
  
  /*package*/ LiveDataSnapshot getLiveDataSnapshot(final BatchJob job) {

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
    LiveDataSnapshot liveDataSnapshot = (LiveDataSnapshot) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("LiveDataSnapshot.one.byDateAndTime");
        query.setDate("date", DateUtil.toSqlDate(observationDate));
        query.setString("time", observationTime);
        return query.uniqueResult();
      }
    });
    return liveDataSnapshot;
  }
  
  /*package*/ LiveDataField getLiveDataField(final String fieldName) {
    LiveDataField field = (LiveDataField) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("LiveDataField.one.byName");
        query.setString("name", fieldName);
        return query.uniqueResult();
      }
    });
    if (field == null) {
      field = new LiveDataField();
      field.setName(fieldName);
      _hibernateTemplate.save(field);
    }
    return field;
  }
  
  /*package*/ ComputationTarget getComputationTarget(final ComputationTargetSpecification spec) {
    ComputationTarget computationTarget = (ComputationTarget) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ComputationTarget.one.byTypeAndId");
        query.setInteger("computationTargetType", ComputationTarget.getType(spec.getType()));
        query.setString("idScheme", spec.getIdentifier().getScheme().getName());
        query.setString("idValue", spec.getIdentifier().getValue());
        return query.uniqueResult();
      }
    });
    if (computationTarget == null) {
      computationTarget = new ComputationTarget();
      computationTarget.setComputationTargetType(spec.getType());
      computationTarget.setIdScheme(spec.getIdentifier().getScheme().getName());      
      computationTarget.setIdValue(spec.getIdentifier().getValue());
      _hibernateTemplate.save(computationTarget);
    }
    return computationTarget;
  }
  
  /*package*/ RiskValueName getRiskValueName(final String name) {
    RiskValueName riskValueName = (RiskValueName) _hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("RiskValueName.one.byName");
        query.setString("name", name);
        return query.uniqueResult();
      }
    });
    if (riskValueName == null) {
      riskValueName = new RiskValueName();
      riskValueName.setName(name);
      _hibernateTemplate.save(riskValueName);
    }
    return riskValueName;
  }
  
  /*package*/ RiskRun getRiskRunFromDb(final BatchJob job) {
    RiskRun riskRun = null;
    
    if (job.isForceNewRun() == false) {
      riskRun = (RiskRun) _hibernateTemplate.execute(new HibernateCallback() {
        @Override
        public Object doInHibernate(Session session) throws HibernateException,
            SQLException {
          Query query = session.getNamedQuery("RiskRun.one.byViewAndRunTime");
          query.setString("viewOid", job.getViewOid());
          query.setInteger("viewVersion", job.getViewVersion());
          query.setDate("runDate", DateUtil.toSqlDate(job.getObservationDate()));
          query.setString("runTime", job.getObservationTime());
          return query.uniqueResult();
        }
      });
      
      // here, if riskRun != null, we should check a hash of the configuration to see it's still the same as before  
    } 
    
    return riskRun;
  }
  
  /*package*/ RiskRun createRiskRun(final BatchJob job) {
    Instant now = Instant.nowSystemClock();
    
    LiveDataSnapshot snapshot = getLiveDataSnapshot(job);
    if (!snapshot.isComplete()) {
      throw new IllegalStateException(snapshot + " is not yet complete.");
    }
    
    RiskRun riskRun = new RiskRun();
    riskRun.setOpenGammaVersion(getOpenGammaVersion(job));
    riskRun.setMasterProcessHost(getLocalComputeHost());
    riskRun.setRunReason(job.getRunReason());
    riskRun.setRunTime(getObservationDateTime(job));
    riskRun.setValuationTime(DateUtil.toSqlTimestamp(job.getValuationTime()));
    riskRun.setViewOid(job.getViewOid());
    riskRun.setViewVersion(job.getViewVersion());
    riskRun.setLiveDataSnapshot(snapshot);
    riskRun.setCreateInstant(DateUtil.toSqlTimestamp(now));
    riskRun.setStartInstant(DateUtil.toSqlTimestamp(now));
    riskRun.setComplete(false);
    
    for (ViewCalculationConfiguration calcConf : job.getCalculationConfigurations()) {
      riskRun.addCalculationConfiguration(calcConf);
    }
    
    _hibernateTemplate.save(riskRun);
    _hibernateTemplate.saveOrUpdateAll(riskRun.getCalculationConfigurations());
    return riskRun;
  }
  
  /*package*/ void restartRun(RiskRun riskRun) {
    Instant now = Instant.nowSystemClock();
    
    riskRun.setStartInstant(DateUtil.toSqlTimestamp(now));
    riskRun.setComplete(false);
    
    _hibernateTemplate.update(riskRun);
  }
  
  /*package*/ void endRun(RiskRun riskRun) {
    Instant now = Instant.nowSystemClock();
    
    riskRun.setEndInstant(DateUtil.toSqlTimestamp(now));
    riskRun.setComplete(true);
    
    _hibernateTemplate.update(riskRun);
  }
  
  /*package*/ RiskRun getRiskRunFromHandle(BatchJob job) {
    return getDbHandle(job)._riskRun;
  }
  
  private DbHandle getDbHandle(BatchJob job) {
    Object handle = job.getDbHandle();
    if (handle == null) {
      throw new IllegalStateException("Job db handle is null");
    }
    if (!(handle instanceof DbHandle)) {
      throw new IllegalStateException("Job db handle must be of type DbHandle, was " + handle.getClass());
    }
    return (DbHandle) handle;
  }
  
  /*package*/ Set<RiskValueName> populateRiskValueNames(BatchJob job) {
    Set<RiskValueName> returnValue = new HashSet<RiskValueName>();
    
    Set<String> riskValueNames = job.getView().getPortfolioEvaluationModel().getAllOutputValueNames();
    for (String name : riskValueNames) {
      RiskValueName riskValueName = getRiskValueName(name);
      returnValue.add(riskValueName);
    }
    
    return returnValue;
  }
  
  /*package*/ Set<ComputationTarget> populateComputationTargets(BatchJob job) {
    Set<ComputationTarget> returnValue = new HashSet<ComputationTarget>();
    
    Set<ComputationTargetSpecification> computationTargets = job.getView().getAllComputationTargets();
    for (ComputationTargetSpecification ct : computationTargets) {
      ComputationTarget computationTarget = getComputationTarget(ct);
      returnValue.add(computationTarget);
    }
    
    return returnValue;
  }
  
  // --------------------------------------------------------------------------
  

  @Override
  public void addValuesToSnapshot(SnapshotId snapshotId, Set<LiveDataValue> values) {
    s_logger.info("Adding {} values to LiveData snapshot {}", values.size(), snapshotId);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();
      
      LiveDataSnapshot snapshot = getLiveDataSnapshot(snapshotId.getObservationDate(), snapshotId.getObservationTime());
      if (snapshot == null) {
        throw new IllegalArgumentException("Snapshot " + snapshotId + " cannot be found");
      }
      if (snapshot.isComplete()) {
        throw new IllegalStateException("Snapshot " + snapshotId + " is already complete.");
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
      
      _hibernateTemplate.saveOrUpdateAll(changedEntries);
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  @Override
  public void createLiveDataSnapshot(SnapshotId snapshotId) {
    s_logger.info("Creating LiveData snapshot {} ", snapshotId);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      LiveDataSnapshot snapshot = getLiveDataSnapshot(snapshotId.getObservationDate(), snapshotId.getObservationTime());
      if (snapshot != null) {
        s_logger.info("Snapshot " + snapshotId + " already exists. No need to create.");
        return;
      }
      
      snapshot = new LiveDataSnapshot();
      snapshot.setComplete(false);
      
      ObservationDateTime snapshotTime = getObservationDateTime(
          snapshotId.getObservationDate(), 
          snapshotId.getObservationTime());
      snapshot.setSnapshotTime(snapshotTime);
      
      _hibernateTemplate.save(snapshot);
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  @Override
  public void endBatch(BatchJob batch) {
    s_logger.info("Ending batch {}", batch);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      RiskRun run = getRiskRunFromHandle(batch);
      endRun(run);
    
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
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
      
      snapshot.getSnapshotTime().setTime(DateUtil.toSqlTime(fix));
      _hibernateTemplate.save(snapshot);
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  @Override
  public void markLiveDataSnapshotComplete(SnapshotId snapshotId) {
    s_logger.info("Marking LiveData snapshot {} complete", snapshotId);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      LiveDataSnapshot snapshot = getLiveDataSnapshot(snapshotId.getObservationDate(), snapshotId.getObservationTime());
      
      if (snapshot == null) {
        throw new IllegalArgumentException("Snapshot " + snapshotId + " cannot be found");
      }
      
      if (snapshot.isComplete()) {
        throw new IllegalStateException(snapshot + " is already complete.");
      }
      
      snapshot.setComplete(true);
      _hibernateTemplate.update(snapshot);
      
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
  public void startBatch(BatchJob batch) {
    s_logger.info("Starting batch {}", batch);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      RiskRun run = getRiskRunFromDb(batch);
      if (run == null) {
        run = createRiskRun(batch);
      } else {
        restartRun(run);
      }
      
      Set<RiskValueName> riskValueNames = populateRiskValueNames(batch);
      Set<ComputationTarget> computationTargets = populateComputationTargets(batch);
      
      DbHandle dbHandle = new DbHandle();
      dbHandle._riskRun = run;
      dbHandle._riskValueNames = riskValueNames;
      dbHandle._computationTargets = computationTargets;
      
      batch.setDbHandle(dbHandle);
      batch.setCreationTime(DateUtil.fromSqlTimestamp(run.getCreateInstant()));
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }
  
  private static class DbHandle {
    private RiskRun _riskRun;
    private Set<RiskValueName> _riskValueNames;
    private Set<ComputationTarget> _computationTargets;
  }
  
  @Override
  public ResultWriterFactory createResultWriterFactory(BatchJob batch) {
    return new BatchResultWriterFactory(batch);
  }
  
  private class BatchResultWriterFactory implements ResultWriterFactory {
    
    private final BatchJob _batch;
    
    public BatchResultWriterFactory(BatchJob batch) {
      ArgumentChecker.notNull(batch, "batch");
      _batch = batch;
    }
    
    @Override
    public ResultWriter create(CalculationJobSpecification jobSpec, List<CalculationJobItem> items, String nodeId) {
      try {
        getSessionFactory().getCurrentSession().beginTransaction();

        BatchResultWriter resultWriter = new BatchResultWriter();
        
        resultWriter.setJdbcUrl(_jdbcUrl);
        resultWriter.setUsername(_username);
        resultWriter.setPassword(_password);
        
        resultWriter.setSessionFactory(getSessionFactory());
        resultWriter.setJdbcTemplate(_jdbcTemplate);
        
        resultWriter.setRiskRun(getRiskRunFromHandle(_batch));
        if (nodeId != null) {
          resultWriter.setComputeNode(getComputeNode(nodeId));
        }
        
        // should be optimized
        resultWriter.setComputationTargets(getDbHandle(_batch)._computationTargets);
        resultWriter.setRiskValueNames(getDbHandle(_batch)._riskValueNames);
        
        getSessionFactory().getCurrentSession().getTransaction().commit();
        return resultWriter;
      } catch (RuntimeException e) {
        getSessionFactory().getCurrentSession().getTransaction().rollback();
        throw e;
      }
    }

  }

  public static Class<?>[] getHibernateMappingClasses() {
    return new Class[] {
      CalculationConfiguration.class,
      ComputeHost.class,
      ComputeNode.class,
      LiveDataField.class,
      LiveDataSnapshot.class,
      LiveDataSnapshotEntry.class,
      ObservationDateTime.class,
      ObservationTime.class,
      OpenGammaVersion.class,
      RiskComputeFailure.class,
      RiskRun.class,
      RiskValueName.class,
      ComputationTarget.class,
      RiskValue.class
    };
  }
  
}
