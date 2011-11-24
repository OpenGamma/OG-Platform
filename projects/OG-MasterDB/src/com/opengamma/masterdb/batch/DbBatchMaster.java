/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.financial.batch.BatchDocument;
import com.opengamma.financial.batch.BatchError;
import com.opengamma.financial.batch.BatchGetRequest;
import com.opengamma.financial.batch.BatchId;
import com.opengamma.financial.batch.BatchMaster;
import com.opengamma.financial.batch.BatchSearchRequest;
import com.opengamma.financial.batch.BatchSearchResult;
import com.opengamma.financial.batch.BatchStatus;
import com.opengamma.financial.batch.LiveDataValue;
import com.opengamma.id.UniqueId;
import com.opengamma.masterdb.AbstractDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
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
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import javax.time.Instant;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class DbBatchMaster extends AbstractDbMaster implements BatchMaster, BatchRunMaster {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbBatchMaster.class);

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbBat";
  /**
   * The database schema.
   */
  private static String s_dbSchema = "";

  /**
   * The Hibernate template.
   */
  private HibernateTemplate _hibernateTemplate;
  private BatchResultWriter _writer;

  /**
   * Creates an instance.
   *
   * @param dbConnector  the database connector, not null
   */
  public DbBatchMaster(final DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    _hibernateTemplate = new HibernateTemplate(dbConnector.getHibernateSessionFactory());
    _hibernateTemplate.setAllowCreate(false);
  }

  //-------------------------------------------------------------------------
  public static synchronized String getDatabaseSchema() {
    return s_dbSchema;
  }

  public static synchronized void setDatabaseSchema(String schema) {
    if (schema == null) {
      s_dbSchema = "";
    } else {
      s_dbSchema = schema + ".";
    }
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the Hibernate Session factory.
   *
   * @return the session factory, not null
   */
  public SessionFactory getSessionFactory() {
    return getDbConnector().getHibernateSessionFactory();
  }

  /**
   * Gets the local Hibernate template.
   *
   * @return the template, not null
   */
  public HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
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

  /*package*/ LiveDataSnapshot getLiveDataSnapshot(final UniqueId marketDataSnapshotUniqueId) {

    LiveDataSnapshot liveDataSnapshot = getHibernateTemplate().execute(new HibernateCallback<LiveDataSnapshot>() {
      @Override
      public LiveDataSnapshot doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("LiveDataSnapshot.one.byMarketDataSnapshotUniqueId");
        query.setString("marketDataSnapshotUniqueId", marketDataSnapshotUniqueId.toString());
        return (LiveDataSnapshot) query.uniqueResult();
      }
    });

    if (liveDataSnapshot == null) {
      throw new IllegalArgumentException("Snapshot for " + marketDataSnapshotUniqueId + " cannot be found");
    }
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

  /*package*/ ViewDefinition createViewDefinition(com.opengamma.engine.view.ViewDefinition viewDefinition) {
    ViewDefinition vd = getViewDefinitionImpl(viewDefinition);
    if (vd == null) {
      vd = new ViewDefinition();
      vd.setUid(viewDefinition.getUniqueId().toString());
      getHibernateTemplate().save(vd);
    }
    return vd;
  }

  private ViewDefinition getViewDefinitionImpl(final com.opengamma.engine.view.ViewDefinition viewDefinition) {
    return getHibernateTemplate().execute(new HibernateCallback<ViewDefinition>() {
      @Override
      public ViewDefinition doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("ViewDefinition.one.byUID");
        query.setString("uid", viewDefinition.getUniqueId().toString());
        return (ViewDefinition) query.uniqueResult();
      }
    });
  }

  /*package*/ VersionCorrection getVersionCorrection(final com.opengamma.id.VersionCorrection versionCorrection) {
    VersionCorrection vc = getVersionCorrectionImpl(versionCorrection);
    if (vc == null) {
      vc = new VersionCorrection();
      vc.setAsOf(DbDateUtils.toSqlTimestamp(versionCorrection.getVersionAsOf()));
      vc.setCorrectedTo(DbDateUtils.toSqlTimestamp(versionCorrection.getCorrectedTo()));
      getHibernateTemplate().save(vc);
    }
    return vc;
  }

  private VersionCorrection getVersionCorrectionImpl(final com.opengamma.id.VersionCorrection versionCorrection) {
    return getHibernateTemplate().execute(new HibernateCallback<VersionCorrection>() {
      @Override
      public VersionCorrection doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("VersionCorrection.one.byTimestamps");
        query.setTimestamp("asOf", DbDateUtils.toSqlTimestamp(versionCorrection.getVersionAsOf()));
        query.setTimestamp("correctedTo", DbDateUtils.toSqlTimestamp(versionCorrection.getCorrectedTo()));
        return (VersionCorrection) query.uniqueResult();
      }
    });
  }

  @Override
  public VersionCorrection createVersionCorrection(com.opengamma.id.VersionCorrection versionCorrection) {
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      VersionCorrection vc = createVersionCorrectionImpl(versionCorrection);

      getSessionFactory().getCurrentSession().getTransaction().commit();

      return vc;
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  private VersionCorrection createVersionCorrectionImpl(com.opengamma.id.VersionCorrection versionCorrection) {
    s_logger.info("Creating VersionCorrection {} ", versionCorrection);
    VersionCorrection vc;
    vc = getVersionCorrection(versionCorrection);
    if (vc != null) {
      s_logger.info("VersionCorrection " + versionCorrection + " already exists. No need to create.");
    } else {
      vc = new VersionCorrection();
      vc.setAsOf(DbDateUtils.toSqlTimestamp(versionCorrection.getVersionAsOf()));
      vc.setCorrectedTo(DbDateUtils.toSqlTimestamp(versionCorrection.getCorrectedTo()));
      getHibernateTemplate().save(vc);
    }
    return vc;
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

  /*package*/ RiskValueRequirement getRiskValueRequirement(final ValueProperties requirement) {
    final String synthesizedForm = RiskValueRequirement.synthesize(requirement);
    RiskValueRequirement riskValueRequirement = getHibernateTemplate().execute(new HibernateCallback<RiskValueRequirement>() {
      @Override
      public RiskValueRequirement doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("RiskValueRequirement.one.bySynthesizedForm");
        query.setString("requirement", synthesizedForm);
        return (RiskValueRequirement) query.uniqueResult();
      }
    });
    if (riskValueRequirement == null) {
      riskValueRequirement = new RiskValueRequirement(requirement);
      getHibernateTemplate().save(riskValueRequirement);
    }
    return riskValueRequirement;
  }

  /*package*/ RiskValueSpecification getRiskValueSpecification(final ValueProperties specification) {
    final String synthesizedForm = RiskValueSpecification.synthesize(specification);
    RiskValueSpecification riskValueSpecification = getHibernateTemplate().execute(new HibernateCallback<RiskValueSpecification>() {
      @Override
      public RiskValueSpecification doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("RiskValueSpecification.one.bySynthesizedForm");
        query.setString("specification", synthesizedForm);
        return (RiskValueSpecification) query.uniqueResult();
      }
    });
    if (riskValueSpecification == null) {
      riskValueSpecification = new RiskValueSpecification(specification);
      getHibernateTemplate().save(riskValueSpecification);
    }
    return riskValueSpecification;
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

  //-------------------------------------------------------------------------
  /*package*/ RiskRun getRiskRunFromDb(final BatchId batchId) {
    return getHibernateTemplate().execute(new HibernateCallback<RiskRun>() {
      @Override
      public RiskRun doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("RiskRun.one.byBatchID");

        query.setString("marketDataSpecificationUID", batchId.getMarketDataSnapshotUid().toString());
        query.setDate("valuationTime", DbDateUtils.toSqlTimestamp(batchId.getValuationTime()));
        query.setDate("versionCorrectionAsOf", DbDateUtils.toSqlTimestamp(batchId.getVersionCorrection().getVersionAsOf()));
        query.setDate("versionCorrectionCorrectedTo", DbDateUtils.toSqlTimestamp(batchId.getVersionCorrection().getCorrectedTo()));
        query.setString("viewDefinitionUID", batchId.getViewDefinitionUid().toString());

        return (RiskRun) query.uniqueResult();
      }
    });
  }

  /*package*/ RiskRun getRiskRunFromDb(final Batch batch) {
    return getRiskRunFromDb(batch.getId());
  }

  //-------------------------------------------------------------------------


  /*package*/ void deleteRun(RiskRun riskRun) {
    deleteRiskValues(riskRun);
    deleteRiskFailures(riskRun);
    getHibernateTemplate().deleteAll(riskRun.getCalculationConfigurations());
    getHibernateTemplate().deleteAll(riskRun.getProperties());
    getHibernateTemplate().delete(riskRun);
    getHibernateTemplate().flush();
  }

  /*package*/ Instant restartRun(Batch batch, RiskRun riskRun) {
    Instant now = now();
    riskRun.setStartInstant(DbDateUtils.toSqlTimestamp(now));
    riskRun.setNumRestarts(riskRun.getNumRestarts() + 1);
    riskRun.setComplete(false);

    getHibernateTemplate().update(riskRun);

    deleteRiskFailures(riskRun);

    return Instant.ofEpochMillis(riskRun.getCreateInstant().getTime());
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
    Instant now = now();

    riskRun.setEndInstant(DbDateUtils.toSqlTimestamp(now));
    riskRun.setComplete(true);

    getHibernateTemplate().update(riskRun);
  }

  /*package*/ RiskRun getRiskRunFromHandle(Batch batch) {
    return getDbHandle(batch)._riskRun;
  }

  private DbHandle getDbHandle(Batch batch) {
    Object handle = batch.getDbHandle();
    if (handle == null) {
      throw new IllegalStateException("Batch db handle is null");
    }
    if (!(handle instanceof DbHandle)) {
      throw new IllegalStateException("Batch db handle must be of type DbHandle, was " + handle.getClass());
    }
    return (DbHandle) handle;
  }

  /*package*/ Map<RiskValueName, Integer> populateRiskValueNames(final Batch batch) {

    Map<RiskValueName, Integer> returnValue = Maps.newHashMap();

    for (String calcConfName : batch.getCycleInfo().getViewDefinition().getAllCalculationConfigurationNames()) {
      Set<ValueSpecification> outputSpecs = batch.getCycleInfo().getDependencyGraphsByConfigName().get(calcConfName).getTerminalOutputSpecifications();
      for (ValueSpecification outputSpec : outputSpecs) {
        RiskValueName riskValueName = getRiskValueName(outputSpec.getValueName());
        returnValue.put(riskValueName, riskValueName.getId());
      }
    }

    return returnValue;
  }

  /*package*/ Map<RiskValueRequirement, Integer> populateRiskValueRequirements(final Batch batch) {
    Map<RiskValueRequirement, Integer> returnValue = new HashMap<RiskValueRequirement, Integer>();

    for (String calcConfName : batch.getCycleInfo().getViewDefinition().getAllCalculationConfigurationNames()) {
      Map<ValueSpecification, Set<ValueRequirement>> outputs = batch.getCycleInfo().getDependencyGraphsByConfigName().get(calcConfName).getTerminalOutputs();
      for (ValueSpecification specification : outputs.keySet()) {
        for (ValueRequirement requirement : outputs.get(specification)) {
          RiskValueRequirement riskValueRequirement = getRiskValueRequirement(requirement.getConstraints());
          returnValue.put(riskValueRequirement, riskValueRequirement.getId());
        }
      }
    }

    return returnValue;
  }

  /*package*/ Map<RiskValueSpecification, Integer> populateRiskValueSpecifications(Batch batch) {
    Map<RiskValueSpecification, Integer> returnValue = new HashMap<RiskValueSpecification, Integer>();

    for (String calcConfName : batch.getCycleInfo().getViewDefinition().getAllCalculationConfigurationNames()) {
      Set<ValueSpecification> outputSpecs = batch.getCycleInfo().getDependencyGraphsByConfigName().get(calcConfName).getTerminalOutputSpecifications();
      for (ValueSpecification specification : outputSpecs) {
        RiskValueSpecification riskValueSpecification = getRiskValueSpecification(specification.getProperties());
        returnValue.put(riskValueSpecification, riskValueSpecification.getId());
      }
    }

    return returnValue;
  }

  /*package*/ Map<ComputationTarget, Integer> populateComputationTargets(Batch batch) {
    Map<ComputationTarget, Integer> returnValue = new HashMap<ComputationTarget, Integer>();

    for (String calcConfName : batch.getCycleInfo().getViewDefinition().getAllCalculationConfigurationNames()) {
      DependencyGraph dependencyGraph = batch.getCycleInfo().getDependencyGraphsByConfigName().get(calcConfName);
      for (com.opengamma.engine.ComputationTarget computationTarget : dependencyGraph.getAllComputationTargets()) {
        ComputationTarget dbTarget = getComputationTarget(computationTarget.toSpecification());
        returnValue.put(dbTarget, dbTarget.getId());
      }
    }

    return returnValue;
  }

  // --------------------------------------------------------------------------


  @Override
  public void addValuesToSnapshot(UniqueId marketDataSnapshotUniqueId, Set<LiveDataValue> values) {
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      addValuesToSnapshotImpl(marketDataSnapshotUniqueId, values);

      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  private void addValuesToSnapshotImpl(UniqueId marketDataSnapshotUniqueId, Set<LiveDataValue> values) {
    s_logger.info("Adding {} values to LiveData snapshot {}", values.size(), marketDataSnapshotUniqueId);

    LiveDataSnapshot snapshot = getLiveDataSnapshot(marketDataSnapshotUniqueId);
    if (snapshot == null) {
      throw new IllegalArgumentException("Snapshot " + marketDataSnapshotUniqueId + " cannot be found");
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
  public LiveDataSnapshot createLiveDataSnapshot(UniqueId marketDataSnapshotUniqueId) {
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      LiveDataSnapshot snapshot = createLiveDataSnapshotImpl(marketDataSnapshotUniqueId);

      getSessionFactory().getCurrentSession().getTransaction().commit();

      return snapshot;
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  private LiveDataSnapshot createLiveDataSnapshotImpl(UniqueId marketDataSnapshotUniqueId) {
    s_logger.info("Creating LiveData snapshot {} ", marketDataSnapshotUniqueId);
    LiveDataSnapshot snapshot;
    snapshot = getLiveDataSnapshot(marketDataSnapshotUniqueId);
    if (snapshot != null) {
      s_logger.info("Snapshot " + marketDataSnapshotUniqueId + " already exists. No need to create.");
    } else {
      snapshot = new LiveDataSnapshot();
      snapshot.setMarketDataSnapshotUniqueId(marketDataSnapshotUniqueId.toString());
      getHibernateTemplate().save(snapshot);
    }
    return snapshot;
  }


  @Override
  public void endBatch(Batch batch) {
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      endBatchImpl(batch);

      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  private void endBatchImpl(Batch batch) {
    s_logger.info("Ending batch {}", batch);

    RiskRun run = getRiskRunFromHandle(batch);
    endRun(run);
  }


  @Override
  public Set<LiveDataValue> getSnapshotValues(UniqueId snapshotUniqueId) {
    s_logger.info("Getting LiveData snapshot {}", snapshotUniqueId);

    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      LiveDataSnapshot liveDataSnapshot = getLiveDataSnapshot(snapshotUniqueId);

      if (liveDataSnapshot == null) {
        throw new IllegalArgumentException("Snapshot " + snapshotUniqueId + " cannot be found");
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
  public void startBatch(Batch batch) {
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      startBatchImpl(batch);

      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  private void startBatchImpl(Batch batch) {
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
          batch.setOriginalCreationTime(restartRun(batch, run));
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

    Map<RiskValueName, Integer> riskValueNames = populateRiskValueNames(batch);
    Map<RiskValueRequirement, Integer> riskValueRequirements = populateRiskValueRequirements(batch);
    Map<RiskValueSpecification, Integer> riskValueSpecifications = populateRiskValueSpecifications(batch);
    Map<ComputationTarget, Integer> computationTargets = populateComputationTargets(batch);

    DbHandle dbHandle = new DbHandle();
    dbHandle._computationTargets = computationTargets;
    dbHandle._riskRun = run;
    dbHandle._riskValueNames = riskValueNames;
    dbHandle._riskValueRequirements = riskValueRequirements;
    dbHandle._riskValueSpecifications = riskValueSpecifications;


    batch.setDbHandle(dbHandle);

    _writer = new BatchResultWriter(
        getDbConnector(),
        computationTargets.keySet(),
        run,
        riskValueNames.keySet(),
        riskValueRequirements.keySet(),
        riskValueSpecifications.keySet());
  }

  public static class DbHandle {
    private Map<ComputationTarget, Integer> _computationTargets;
    private RiskRun _riskRun;
    private Map<RiskValueName, Integer> _riskValueNames;
    private Map<RiskValueRequirement, Integer> _riskValueRequirements;
    private Map<RiskValueSpecification, Integer> _riskValueSpecifications;
    private ResultModelDefinition _resultModelDefinition;
    private DependencyGraph _dependencyGraph;
  }


  @Override
  public void addJobResults(Batch batch, ViewResultModel result) {
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      /*SnapshotId snapshotId = createAdHocBatchSnapshotId(result.getBatchId());

      createLiveDataSnapshotImpl(snapshotId);

      Set<LiveDataValue> values = new HashSet<LiveDataValue>();
      for (ComputedValue liveData : result.getAllMarketData()) {
        values.add(new LiveDataValue(liveData));
      }

      addValuesToSnapshotImpl(snapshotId, values);

      AdHocBatchJobRun batch = new AdHocBatchJobRun(result, snapshotId);
      startBatchImpl(batch);*/


      _writer.write(result);

      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException ex) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw ex;
    }
  }

  @Override
  public RiskRun createRiskRun(final Batch batch) {
    Instant now = Instant.now();

    LiveDataSnapshot snapshot;

    VersionCorrection versionCorrection = createVersionCorrection(batch.getCycleInfo().getVersionCorrection());

    ViewDefinition viewDefinition = createViewDefinition(batch.getCycleInfo().getViewDefinition());

    if (batch.getSnapshotMode().equals(Batch.SnapshotMode.PREPARED)) {
      snapshot = getLiveDataSnapshot(batch.getCycleInfo().getMarketDataSnapshotUniqueId());
      if (snapshot == null) {
        throw new DataNotFoundException("Snapshot not found: " + batch.getCycleInfo().getMarketDataSnapshotUniqueId());
      }
    } else {
      snapshot = createLiveDataSnapshot(batch.getCycleInfo().getMarketDataSnapshotUniqueId());
    }

    RiskRun riskRun = new RiskRun();
    riskRun.setLiveDataSnapshot(snapshot);
    riskRun.setVersionCorrection(versionCorrection);
    riskRun.setViewDefinition(viewDefinition);
    riskRun.setValuationTime(DbDateUtils.toSqlTimestamp(batch.getCycleInfo().getValuationTime()));
    riskRun.setCreateInstant(DbDateUtils.toSqlTimestamp(now));
    riskRun.setStartInstant(DbDateUtils.toSqlTimestamp(now));
    riskRun.setNumRestarts(0);
    riskRun.setComplete(false);

    for (Map.Entry<String, String> parameter : batch.getParametersMap().entrySet()) {
      riskRun.addProperty(parameter.getKey(), parameter.getValue());
    }

    for (String calcConf : batch.getCycleInfo().getViewDefinition().getAllCalculationConfigurationNames()) {
      riskRun.addCalculationConfiguration(calcConf);
    }

    getHibernateTemplate().save(riskRun);
    getHibernateTemplate().saveOrUpdateAll(riskRun.getCalculationConfigurations());
    getHibernateTemplate().saveOrUpdateAll(riskRun.getProperties());

    return riskRun;
  }

  /*public CommandLineBatchResultWriter createTestResultWriter(BatchJobRun batch) {
    BatchResultWriterFactory factory = new BatchResultWriterFactory(batch);
    return factory.createTestWriter();
  }

  private class BatchResultWriterFactory implements DependencyGraphExecutorFactory<Object> {

    private final BatchJobRun _batch;

    public BatchResultWriterFactory(BatchJobRun batch) {
      ArgumentChecker.notNull(batch, "batch");
      _batch = batch;
    }

    public BatchResultWriter createTestWriter() {
      BatchResultWriter resultWriter = new BatchResultWriter(
          getDbConnector(),
          new ResultModelDefinition(),
          new HashMap<String, ViewComputationCache>(),

          getDbHandle(_batch)._computationTargets,
          getRiskRunFromHandle(_batch),
          getDbHandle(_batch)._riskValueNames,
          getDbHandle(_batch)._riskValueRequirements,
          getDbHandle(_batch)._riskValueSpecifications);

      return resultWriter;
    }
  }*/

  public static Class<?>[] getHibernateMappingClasses() {
    return new HibernateBatchDbFiles().getHibernateMappingFiles();
  }

  //-------------------------------------------------------------------------
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
              getDialect().sqlWildcardAdjustValue(request.getObservationTime()), Hibernate.STRING));
    }

    BatchSearchResult result = new BatchSearchResult();
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      if (request.getPagingRequest().equals(PagingRequest.ALL)) {
        result.setPaging(Paging.of(request.getPagingRequest(), result.getDocuments()));
      } else {
        criteria.setProjection(Projections.rowCount());
        Long totalCount = (Long) getHibernateTemplate().findByCriteria(criteria).get(0);
        result.setPaging(Paging.of(request.getPagingRequest(), totalCount.intValue()));
        criteria.setProjection(null);
        criteria.setResultTransformer(Criteria.ROOT_ENTITY);
      }

      runTimeCriteria.addOrder(Order.asc("date"));
      observationTimeCriteria.addOrder(Order.asc("label"));

      List<RiskRun> runs = Collections.emptyList();
      if (request.getPagingRequest().equals(PagingRequest.NONE) == false) {
        runs = getHibernateTemplate().findByCriteria(
            criteria,
            request.getPagingRequest().getFirstItem(),
            request.getPagingRequest().getPagingSize());
      }

      for (RiskRun riskRun : runs) {
        BatchDocument doc = new BatchDocument(
            UniqueId.parse(riskRun.getViewDefinition().getUid()),
            UniqueId.parse(riskRun.getLiveDataSnapshot().getMarketDataSnapshotUniqueId()),
            DbDateUtils.fromSqlTimestamp(riskRun.getValuationTime()),
            riskRun.getVersionCorrection().getBaseType());
        parseRiskRun(riskRun, doc);
        result.getDocuments().add(doc);
      }

      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException ex) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw ex;
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public BatchDocument get(UniqueId uniqueId) {
    return get(new BatchGetRequest(uniqueId));
  }

  @Override
  public BatchDocument get(BatchGetRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getUniqueId(), "request.uniqueId");
    ArgumentChecker.isTrue(request.getUniqueId().getValue().length() >= 12, "Invalid uniqueId");
    checkScheme(request.getUniqueId());

    RiskRun riskRun;
    BatchDocument doc = new BatchDocument(request.getUniqueId());
    try {
      getSessionFactory().getCurrentSession().beginTransaction();
      riskRun = getRiskRunFromDb(new BatchId(doc.getMarketDataSnapshotUid(), doc.getViewDefinitionUid(), doc.getVersionCorrection(), doc.getValuationTime()));
      if (riskRun != null) {
        parseRiskRun(riskRun, doc);
      }
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException ex) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw ex;
    }
    if (riskRun == null) {
      throw new DataNotFoundException("Batch not found: " + request.getUniqueId());
    }

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("rsk_run_id", riskRun.getId());

    final int dataCount = getJdbcTemplate().queryForInt(ViewResultEntryMapper.sqlCount(), params);
    String dataSql = getDialect().sqlApplyPaging(ViewResultEntryMapper.sqlGet(), " ", request.getDataPagingRequest());
    List<ViewResultEntry> data = getJdbcTemplate().query(dataSql, ViewResultEntryMapper.ROW_MAPPER, params);

    final int errorCount = getJdbcTemplate().queryForInt(BatchErrorMapper.sqlCount(), params);
    String errorSql = getDialect().sqlApplyPaging(BatchErrorMapper.sqlGet(), " ", request.getErrorPagingRequest());
    List<BatchError> errors = getJdbcTemplate().query(errorSql, BatchErrorMapper.ROW_MAPPER, params);

    doc.setDataPaging(Paging.of(request.getDataPagingRequest(), dataCount));
    doc.setData(data);
    doc.setErrorsPaging(Paging.of(request.getErrorPagingRequest(), errorCount));
    doc.setErrors(errors);
    return doc;
  }

  /**
   * Parses the Hibernate object to a document.
   *
   * @param riskRun  the database risk run object, not null
   * @param doc  the batch document, not null
   */
  protected void parseRiskRun(RiskRun riskRun, BatchDocument doc) {
    doc.setStatus(riskRun.isComplete() ? BatchStatus.COMPLETE : BatchStatus.RUNNING);
    doc.setValuationTime(DbDateUtils.fromSqlTimestamp(riskRun.getValuationTime()));
    doc.setCreationInstant(DbDateUtils.fromSqlTimestamp(riskRun.getCreateInstant()));
    doc.setStartInstant(DbDateUtils.fromSqlTimestamp(riskRun.getStartInstant()));
    if (riskRun.getEndInstant() != null) {
      doc.setEndInstant(DbDateUtils.fromSqlTimestamp(riskRun.getEndInstant()));
    }
    doc.setNumRestarts(riskRun.getNumRestarts());
  }

  @Override
  public void delete(BatchId batchId) {
    s_logger.info("Deleting batch {}", batchId);
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      RiskRun run = getRiskRunFromDb(batchId);
      if (run == null) {
        throw new DataNotFoundException("Batch not found: " + batchId);
      }
      deleteRun(run);

      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException ex) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw ex;
    }
  }

  @Override
  public void end(Batch batch) {
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      BatchId batchId = batch.getId();
      RiskRun riskRun = getRiskRunFromDb(batchId);
      if (riskRun == null) {
        throw new DataNotFoundException("Batch not found: " + batchId);
      }
      endRun(riskRun);
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException ex) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw ex;
    }
  }
}
