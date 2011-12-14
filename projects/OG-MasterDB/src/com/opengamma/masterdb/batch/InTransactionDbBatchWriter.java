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
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.engine.view.calcnode.MissingInput;
import com.opengamma.extsql.ExtSqlBundle;
import com.opengamma.financial.conversion.ResultConverter;
import com.opengamma.financial.conversion.ResultConverterCache;
import com.opengamma.id.UniqueId;
import com.opengamma.masterdb.batch.document.DbBatchDocumentMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.functional.Function1;
import com.opengamma.util.tuple.Pair;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import javax.time.Instant;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newConcurrentMap;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.opengamma.util.db.DbUtil.eqOrIsNull;
import static com.opengamma.util.functional.Functional.any;
import static com.opengamma.util.functional.Functional.newArray;

/**
 * A batch master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the batch master using an SQL database.
 * This implementation uses Hibernate to write all static data, including LiveData snapshots.
 * Risk itself is written using direct JDBC.
 * <p>
 * Full details of the API are in {@link com.opengamma.masterdb.batch.document.BatchMaster}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
abstract public class InTransactionDbBatchWriter extends DbBatchDocumentMaster {


  final public Map<String, Long> _calculationConfigurations = newConcurrentMap();
  final public Map<String, Long> _riskValueNames = newConcurrentMap();
  final public Map<ValueRequirement, Long> _riskValueRequirements = newConcurrentMap();
  final public Map<ValueSpecification, Long> _riskValueSpecifications = newConcurrentMap();
  final public Map<ComputationTargetSpecification, Long> _computationTargets = newConcurrentMap();

  final public Map<Long, Map<Pair<Long, Long>, StatusEntry>> _statusCacheByRunId = newConcurrentMap();
  final public Map<Long, Map<ComputeFailureKey, ComputeFailure>> _computeFailureCacheByRunId = newConcurrentMap();

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(InTransactionDbBatchWriter.class);

  /**
   * The Result converter cache.
   */
  private ResultConverterCache _resultConverterCache;

  /**
   * Creates an instance.
   *
   * @param dbConnector  the database connector, not null
   */
  public InTransactionDbBatchWriter(final DbConnector dbConnector) {
    super(dbConnector);
    _resultConverterCache = new ResultConverterCache();
    setExtSqlBundle(ExtSqlBundle.of(dbConnector.getDialect().getExtSqlConfig(), InTransactionDbBatchWriter.class));
  }

  protected ComputeHost getOrCreateComputeHost(final String hostName) {
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
      getHibernateTemplate().flush();
    }
    return computeHost;
  }

  protected ComputeNode getOrCreateComputeNode(final String nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");
    String hostName = nodeId;
    int slashIndex = nodeId.indexOf('/'); // e.g., mymachine-t5500/0/1, see LocalCalculationNode.java. Should refactor nodeId to a class with two strings, host and node id
    if (slashIndex != -1) {
      hostName = nodeId.substring(0, slashIndex);
    }
    final ComputeHost host = getOrCreateComputeHost(hostName);

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
      getHibernateTemplate().flush();
    }
    return node;
  }

  protected LiveDataSnapshot getLiveDataSnapshotInTransaction(final UniqueId marketDataSnapshotUniqueId) {

    LiveDataSnapshot liveDataSnapshot = getHibernateTemplate().execute(new HibernateCallback<LiveDataSnapshot>() {
      @Override
      public LiveDataSnapshot doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("LiveDataSnapshot.one.byMarketDataSnapshotUid");
        query.setString("marketDataSnapshotUid", marketDataSnapshotUniqueId.toString());
        return (LiveDataSnapshot) query.uniqueResult();
      }
    });

    if (liveDataSnapshot == null) {
      throw new IllegalArgumentException("Snapshot for " + marketDataSnapshotUniqueId + " cannot be found");
    }
    return liveDataSnapshot;
  }


  protected LiveDataField getLiveDataField(final String fieldName) {
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
      getHibernateTemplate().flush();
    }
    return field;
  }

  public ComputationTarget getComputationTarget(final ComputationTargetSpecification spec) {
    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<ComputationTarget>() {
      @Override
      public ComputationTarget doInTransaction(final TransactionStatus status) {
        return getComputationTargetIntransaction(spec);
      }
    });
  }

  protected ComputationTarget getComputationTargetIntransaction(final ComputationTargetSpecification spec) {
    return getHibernateTemplate().execute(new HibernateCallback<ComputationTarget>() {
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
  }

  protected ComputationTarget getOrCreateComputationTargetInTransaction(final ComputationTargetSpecification spec, final String name) {
    ComputationTarget computationTarget = getComputationTarget(spec);
    if (computationTarget == null) {
      computationTarget = new ComputationTarget();
      computationTarget.setComputationTargetType(spec.getType());
      computationTarget.setIdScheme(spec.getUniqueId().getScheme());
      computationTarget.setIdValue(spec.getUniqueId().getValue());
      computationTarget.setIdVersion(spec.getUniqueId().getVersion());
      computationTarget.setName(name);
      getHibernateTemplate().save(computationTarget);
      getHibernateTemplate().flush();
    }
    return computationTarget;
  }

  protected ViewDefinition createViewDefinitionInTransaction(UniqueId viewDefinitionUid) {
    ViewDefinition vd = getViewDefinitionInTransaction(viewDefinitionUid);
    if (vd == null) {
      vd = new ViewDefinition();
      vd.setViewDefinitionUid(viewDefinitionUid.toString());
      getHibernateTemplate().save(vd);
      getHibernateTemplate().flush();
    }
    return vd;
  }

  protected ViewDefinition getViewDefinitionInTransaction(final UniqueId viewDefinitionUid) {
    return getHibernateTemplate().execute(new HibernateCallback<ViewDefinition>() {
      @Override
      public ViewDefinition doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("ViewDefinition.one.byUID");
        query.setString("uid", viewDefinitionUid.toString());
        return (ViewDefinition) query.uniqueResult();
      }
    });
  }

  protected VersionCorrection getVersionCorrection(final com.opengamma.id.VersionCorrection versionCorrection) {
    VersionCorrection vc = getVersionCorrectionInTransaction(versionCorrection);
    if (vc == null) {
      vc = new VersionCorrection();
      vc.setAsOf(versionCorrection.getVersionAsOf());
      vc.setCorrectedTo(versionCorrection.getCorrectedTo());
      getHibernateTemplate().save(vc);
      getHibernateTemplate().flush();
    }
    return vc;
  }

  protected VersionCorrection getVersionCorrectionInTransaction(final com.opengamma.id.VersionCorrection versionCorrection) {
    return getHibernateTemplate().execute(new HibernateCallback<VersionCorrection>() {
      @Override
      public VersionCorrection doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("VersionCorrection.one.byTimestamps");
        query.setParameter("asOf", versionCorrection.getVersionAsOf());
        query.setParameter("correctedTo", versionCorrection.getCorrectedTo());
        return (VersionCorrection) query.uniqueResult();
      }
    });
  }

  protected CalculationConfiguration getCalculationConfiguration(final String name) {
    CalculationConfiguration calcConfig = getHibernateTemplate().execute(new HibernateCallback<CalculationConfiguration>() {
      @Override
      public CalculationConfiguration doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("CalculationConfiguration.one.byName");
        query.setString("name", name);
        return (CalculationConfiguration) query.uniqueResult();
      }
    });
    if (calcConfig == null) {
      calcConfig = new CalculationConfiguration();
      calcConfig.setName(name);
      getHibernateTemplate().save(calcConfig);
      getHibernateTemplate().flush();
    }
    return calcConfig;
  }

  protected RiskValueName getRiskValueName(final String name) {
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
      getHibernateTemplate().flush();
    }
    return riskValueName;
  }

  protected RiskValueRequirement getRiskValueRequirement(final ValueProperties requirement) {
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
      getHibernateTemplate().flush();
    }
    return riskValueRequirement;
  }

  protected RiskValueSpecification getRiskValueSpecification(final ValueProperties specification) {
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
      getHibernateTemplate().flush();
    }
    return riskValueSpecification;
  }

  protected FunctionUniqueId getFunctionUniqueIdInTransaction(final String uniqueId) {
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
      getHibernateTemplate().flush();
    }
    return functionUniqueId;
  }

  //-------------------------------------------------------------------------


  protected Instant restartRunInTransaction(Batch batch, RiskRun riskRun) {
    Instant now = now();
    riskRun.setStartInstant(now);
    riskRun.setNumRestarts(riskRun.getNumRestarts() + 1);
    riskRun.setComplete(false);

    getHibernateTemplate().update(riskRun);
    getHibernateTemplate().flush();
    deleteRiskFailuresInTransaction(riskRun);

    return riskRun.getCreateInstant();
  }

  protected void populateRiskValueNames(Batch batch) {
    Collection<Pair<String, Map<String, Object>>> data = newLinkedList();
    for (final String configName : batch.getCycleInfo().getAllCalculationConfigurationNames()) {
      Map<ValueSpecification, Set<ValueRequirement>> outputs = batch.getCycleInfo().getTerminalOutputsByConfigName(configName);
      for (ValueSpecification specification : outputs.keySet()) {
        Map<String, Object> attribs = newHashMap();
        attribs.put("name", specification.getValueName());
        data.add(Pair.of(specification.getValueName(), attribs));
      }
    }
    _riskValueNames.putAll(populate(data, getExtSqlBundle().getSql("SelectRiskValueName"), getExtSqlBundle().getSql("InsertRiskValueName"), "rsk_batch_seq"));
  }


  protected void populateRiskValueRequirements(Batch batch) {
    Collection<Pair<ValueRequirement, Map<String, Object>>> data = newLinkedList();
    for (final String configName : batch.getCycleInfo().getAllCalculationConfigurationNames()) {
      Map<ValueSpecification, Set<ValueRequirement>> outputs = batch.getCycleInfo().getTerminalOutputsByConfigName(configName);
      for (ValueSpecification specification : outputs.keySet()) {
        for (ValueRequirement requirement : outputs.get(specification)) {
          Map<String, Object> attribs = newHashMap();
          attribs.put("synthetic_form", RiskValueSpecification.synthesize(requirement.getConstraints()));
          data.add(Pair.of(requirement, attribs));
        }
      }
    }
    _riskValueRequirements.putAll(populate(data, getExtSqlBundle().getSql("SelectRiskValueRequirement"), getExtSqlBundle().getSql("InsertRiskValueRequirement"), "rsk_batch_seq"));
  }

  protected void populateRiskValueSpecifications(Batch batch) {
    Collection<Pair<ValueSpecification, Map<String, Object>>> data = newLinkedList();
    for (final String configName : batch.getCycleInfo().getAllCalculationConfigurationNames()) {
      for (ValueSpecification specification : batch.getCycleInfo().getTerminalOutputsByConfigName(configName).keySet()) {
        Map<String, Object> attribs = newHashMap();
        attribs.put("synthetic_form", RiskValueSpecification.synthesize(specification.getProperties()));
        data.add(Pair.of(specification, attribs));
      }
    }
    _riskValueSpecifications.putAll(populate(data, getExtSqlBundle().getSql("SelectRiskValueSpecification"), getExtSqlBundle().getSql("InsertRiskValueSpecification"), "rsk_batch_seq"));
  }


  protected void populateComputationTargets(Batch batch) {
    Map<ComputationTargetType, Long> targetsTypes = populateComputationTargetsTypes(batch);

    Collection<Pair<ComputationTargetSpecification, Map<String, Object>>> computationTargetsData = newLinkedList();
    for (final String configName : batch.getCycleInfo().getAllCalculationConfigurationNames()) {
      for (com.opengamma.engine.ComputationTarget computationTarget : batch.getCycleInfo().getComputationTargetsByConfigName(configName)) {
        Map<String, Object> attribs = newHashMap();
        attribs.put("name", computationTarget.getName());
        attribs.put("id_scheme", computationTarget.getUniqueId().getScheme());
        attribs.put("id_value", computationTarget.getUniqueId().getValue());
        attribs.put("id_version", computationTarget.getUniqueId().getVersion());
        attribs.put("type_id", targetsTypes.get(computationTarget.getType()));
        computationTargetsData.add(Pair.of(computationTarget.toSpecification(), attribs));
      }
    }
    _computationTargets.putAll(populate(computationTargetsData, getExtSqlBundle().getSql("SelectComputationTarget"), getExtSqlBundle().getSql("InsertComputationTarget"), "rsk_batch_seq"));
  }

  protected Map<ComputationTargetType, Long> populateComputationTargetsTypes(Batch batch) {
    Collection<Pair<ComputationTargetType, Map<String, Object>>> data = newLinkedList();
    for (final String configName : batch.getCycleInfo().getAllCalculationConfigurationNames()) {
      for (com.opengamma.engine.ComputationTarget computationTarget : batch.getCycleInfo().getComputationTargetsByConfigName(configName)) {
        Map<String, Object> attribs = newHashMap();
        attribs.put("name", computationTarget.getType().name());
        data.add(Pair.of(computationTarget.getType(), attribs));
      }
    }
    return populate(data, getExtSqlBundle().getSql("SelectComputationTargetType"), getExtSqlBundle().getSql("InsertComputationTargetType"), "rsk_batch_seq");
  }

  protected void populateCalculationConfigurations(Batch batch) {
    Collection<Pair<String, Map<String, Object>>> data = newLinkedList();
    for (final String configName : batch.getCycleInfo().getAllCalculationConfigurationNames()) {
      Map<String, Object> map = newHashMap();
      map.put("name", configName);
      data.add(Pair.of(configName, map));
    }
    _calculationConfigurations.putAll(populate(data, getExtSqlBundle().getSql("SelectConfigName"), getExtSqlBundle().getSql("InsertConfigName"), "rsk_batch_seq"));
  }

  protected <T> Map<T, Long> populate(Collection<Pair<T, Map<String, Object>>> data, String selectSql, String insertSql, String pkSequenceName) {
    final List<DbMapSqlParameterSource> insertArgsList = new ArrayList<DbMapSqlParameterSource>();

    Map<T, Long> cache = newHashMap();
    for (Pair<T, Map<String, Object>> objectWithParams : data) {
      T object = objectWithParams.getFirst();
      Map<String, Object> params = objectWithParams.getSecond();
      final DbMapSqlParameterSource selectArgs = new DbMapSqlParameterSource();
      for (String paramName : params.keySet()) {
        selectArgs.addValue(paramName, params.get(paramName));
      }
      List<Map<String, Object>> results = getJdbcTemplate().queryForList(selectSql, selectArgs);
      if (results.isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long id = nextId(pkSequenceName);
        final DbMapSqlParameterSource insertArgs = new DbMapSqlParameterSource().addValue("id", id);
        for (String paramName : params.keySet()) {
          insertArgs.addValue(paramName, params.get(paramName));
        }
        insertArgsList.add(insertArgs);
        //
        cache.put(object, id);
      } else {
        for (Map<String, Object> result : results) {
          cache.put(object, (Long) result.get("ID"));
        }
      }
    }

    getJdbcTemplate().batchUpdate(insertSql, insertArgsList.toArray(new DbMapSqlParameterSource[insertArgsList.size()]));

    return cache;
  }

  protected Map<String, Object> getAttributes(Map<String, Object> attribs, String selectSql) {
    final DbMapSqlParameterSource selectArgs = new DbMapSqlParameterSource();
    for (String paramName : attribs.keySet()) {
      selectArgs.addValue(paramName, attribs.get(paramName));
    }
    List<Map<String, Object>> results = getJdbcTemplate().queryForList(selectSql, selectArgs);
    if (results.isEmpty()) {
      return null;
    } else if (results.size() == 1) {
      return results.get(0);
    } else {
      throw new IllegalArgumentException("The query: \n" + selectSql + " \nshould be constructed so it returns at most one record.");
    }
  }

  protected Long getId(Map<String, Object> attribs, String selectSql) {
    Map<String, Object> attributes = getAttributes(attribs, selectSql);
    if (attribs != null) {
      return (Long) attributes.get("ID");
    } else {
      return null;
    }
  }

  public static Class<?>[] getHibernateMappingClasses() {
    return new HibernateBatchDbFiles().getHibernateMappingFiles();
  }

  protected VersionCorrection createVersionCorrectionInTransaction(com.opengamma.id.VersionCorrection versionCorrection) {
    s_logger.info("Creating VersionCorrection {} ", versionCorrection);
    VersionCorrection vc;
    vc = getVersionCorrection(versionCorrection);
    if (vc != null) {
      s_logger.info("VersionCorrection " + versionCorrection + " already exists. No need to create.");
    } else {
      vc = new VersionCorrection();
      vc.setAsOf(versionCorrection.getVersionAsOf());
      vc.setCorrectedTo(versionCorrection.getCorrectedTo());
      getHibernateTemplate().save(vc);
      getHibernateTemplate().flush();
    }
    return vc;
  }

  protected void endBatchInTransaction(UniqueId batchUniqueId) {        
    ArgumentChecker.notNull(batchUniqueId, "uniqueId");
    s_logger.info("Ending batch {}", batchUniqueId);
    RiskRun run = getRiskRun(batchUniqueId);
    //
    _statusCacheByRunId.remove(run.getId());
    _computeFailureCacheByRunId.remove(run.getId());
    //
    Instant now = now();
    run.setEndInstant(now);
    run.setComplete(true);
    getHibernateTemplate().update(run);
  }

  protected void deleteRiskValuesInTransaction(RiskRun riskRun) {
    MapSqlParameterSource parameters = new MapSqlParameterSource()
      .addValue("run_id", riskRun.getId());
    getJdbcTemplate().update(getExtSqlBundle().getSql("DeleteRiskValues"), parameters);
  }

  protected void deleteRiskFailuresInTransaction(RiskRun riskRun) {
    MapSqlParameterSource parameters = new MapSqlParameterSource()
      .addValue("run_id", riskRun.getId());
    getJdbcTemplate().update(getExtSqlBundle().getSql("DeleteRiskFailureReason"), parameters);
    getJdbcTemplate().update(getExtSqlBundle().getSql("DeleteRiskFailure"), parameters);
  }

  protected void deleteRunInTransaction(RiskRun run) {
    s_logger.info("Deleting run {}", run);
    deleteRiskValuesInTransaction(run);
    deleteRiskFailuresInTransaction(run);
    getHibernateTemplate().deleteAll(run.getProperties());
    getHibernateTemplate().delete(run);
    getHibernateTemplate().flush();
  }

  protected void deleteBatchInTransaction(UniqueId batchUniqueId) {
    s_logger.info("Deleting batch {}", batchUniqueId);
    RiskRun run = getRiskRun(batchUniqueId);
    deleteRunInTransaction(run);
  }

  protected Set<LiveDataValue> getSnapshotValuesInTransaction(UniqueId snapshotUniqueId) {
    s_logger.info("Getting LiveData snapshot {}", snapshotUniqueId);
    LiveDataSnapshot liveDataSnapshot = getLiveDataSnapshotInTransaction(snapshotUniqueId);

    if (liveDataSnapshot == null) {
      throw new IllegalArgumentException("Snapshot " + snapshotUniqueId + " cannot be found");
    }
    Set<LiveDataValue> returnValues = new HashSet<LiveDataValue>();
    for (LiveDataSnapshotEntry entry : liveDataSnapshot.getSnapshotEntries()) {
      returnValues.add(entry.toLiveDataValue());
    }
    return returnValues;
  }

  protected RiskRun findRiskRunInDbInTransaction(final BatchId batchId) {
    return getHibernateTemplate().execute(new HibernateCallback<RiskRun>() {
      @Override
      public RiskRun doInHibernate(Session session) throws HibernateException, SQLException {
        Criteria criteria = session.createCriteria(RiskRun.class);
        criteria.add(Restrictions.eq("valuationTime", batchId.getValuationTime()));

        Criteria versionCorrectionCriteria = criteria.createCriteria("versionCorrection");
        versionCorrectionCriteria.add(eqOrIsNull("asOf", batchId.getVersionCorrection().getVersionAsOf()));
        versionCorrectionCriteria.add(eqOrIsNull("correctedTo", batchId.getVersionCorrection().getCorrectedTo()));

        criteria.createCriteria("viewDefinition").add(Restrictions.eq("viewDefinitionUid", batchId.getViewDefinitionUid().toString()));
        criteria.createCriteria("liveDataSnapshot").add(Restrictions.eq("marketDataSnapshotUid", batchId.getMarketDataSnapshotUid().toString()));

        return (RiskRun) criteria.uniqueResult();
      }
    });
  }

  protected RiskRun findRiskRunInDb(final BatchId batchId) {
    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<RiskRun>() {
      @Override
      public RiskRun doInTransaction(final TransactionStatus status) {
        return findRiskRunInDbInTransaction(batchId);
      }
    });
  }

  protected RiskRun findRiskRunInDb(final Batch batch) {
    return findRiskRunInDb(batch.getBatchId());
  }

  private RiskRun findRiskRunInDb(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final Long id = extractOid(uniqueId);
    return getRiskRunById(id);
  }

  protected RiskRun getRiskRun(UniqueId batchUniqueId) {
    RiskRun run = findRiskRunInDb(batchUniqueId);
    if (run == null) {
      throw new DataNotFoundException("Cannot find run in database for " + batchUniqueId);
    } else {
      return run;
    }
  }

  protected RiskRun createRiskRunInTransaction(final Batch batch, final SnapshotMode snapshotMode) {
    Instant now = Instant.now();

    VersionCorrection versionCorrection = createVersionCorrectionInTransaction(batch.getBatchId().getVersionCorrection());

    ViewDefinition viewDefinition = createViewDefinitionInTransaction(batch.getBatchId().getViewDefinitionUid());

    LiveDataSnapshot snapshot = createOrGetLiveDataSnapshotInTransaction(batch.getBatchId().getMarketDataSnapshotUid());

    RiskRun riskRun = new RiskRun();
    riskRun.setLiveDataSnapshot(snapshot);
    riskRun.setVersionCorrection(versionCorrection);
    riskRun.setViewDefinition(viewDefinition);
    riskRun.setValuationTime(batch.getBatchId().getValuationTime());
    riskRun.setCreateInstant(now);
    riskRun.setStartInstant(now);
    riskRun.setNumRestarts(0);
    riskRun.setComplete(false);

    for (Map.Entry<String, String> parameter : batch.getParametersMap().entrySet()) {
      riskRun.addProperty(parameter.getKey(), parameter.getValue());
    }

    getHibernateTemplate().save(riskRun);
    getHibernateTemplate().saveOrUpdateAll(riskRun.getProperties());
    getHibernateTemplate().flush();

    return riskRun;
  }

  protected void startBatchInTransaction(Batch batch, RunCreationMode runCreationMode, SnapshotMode snapshotMode) {
    s_logger.info("Starting batch {}", batch);

    RiskRun run;
    switch (runCreationMode) {
      case AUTO:
        run = findRiskRunInDb(batch);

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
          run = createRiskRunInTransaction(batch, snapshotMode);
        } else {
          restartRunInTransaction(batch, run);
        }
        break;

      case CREATE_NEW_OVERWRITE:
        run = findRiskRunInDb(batch);
        if (run != null) {
          deleteRunInTransaction(run);
        }

        run = createRiskRunInTransaction(batch, snapshotMode);
        break;

      case CREATE_NEW:
        run = createRiskRunInTransaction(batch, snapshotMode);
        break;

      case REUSE_EXISTING:
        run = findRiskRunInDb(batch);
        if (run == null) {
          throw new IllegalStateException("Cannot find run in database for " + batch);
        }
        restartRunInTransaction(batch, run);
        break;

      default:
        throw new RuntimeException("Unexpected run creation mode " + runCreationMode);
    }

    populateCalculationConfigurations(batch);
    populateRiskValueNames(batch);
    populateRiskValueRequirements(batch);
    populateRiskValueSpecifications(batch);
    populateComputationTargets(batch);
    
    _statusCacheByRunId.put(run.getId(), new ConcurrentHashMap<Pair<Long, Long>, StatusEntry>());
    _computeFailureCacheByRunId.put(run.getId(), new ConcurrentHashMap<ComputeFailureKey, ComputeFailure>());
    
    batch.setUniqueId(UniqueId.of(IDENTIFIER_SCHEME_DEFAULT, Long.toString(run.getId())));
  }

  protected LiveDataSnapshot createOrGetLiveDataSnapshotInTransaction(final UniqueId marketDataSnapshotUniqueId) {
    s_logger.info("Creating LiveData snapshot {} ", marketDataSnapshotUniqueId);
    LiveDataSnapshot liveDataSnapshot = getHibernateTemplate().execute(new HibernateCallback<LiveDataSnapshot>() {
      @Override
      public LiveDataSnapshot doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("LiveDataSnapshot.one.byMarketDataSnapshotUid");
        query.setString("marketDataSnapshotUid", marketDataSnapshotUniqueId.toString());
        return (LiveDataSnapshot) query.uniqueResult();
      }
    });
    if (liveDataSnapshot != null) {
      s_logger.info("Snapshot " + marketDataSnapshotUniqueId + " already exists. No need to create.");
    } else {
      liveDataSnapshot = new LiveDataSnapshot();
      liveDataSnapshot.setMarketDataSnapshotUid(marketDataSnapshotUniqueId.toString());
      getHibernateTemplate().save(liveDataSnapshot);
      getHibernateTemplate().flush();
    }
    return liveDataSnapshot;
  }

  protected void addValuesToSnapshotInTransaction(UniqueId marketDataSnapshotUniqueId, Set<LiveDataValue> values) {
    s_logger.info("Adding {} values to LiveData snapshot {}", values.size(), marketDataSnapshotUniqueId);

    LiveDataSnapshot snapshot = getLiveDataSnapshotInTransaction(marketDataSnapshotUniqueId);
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
        entry.setComputationTarget(getOrCreateComputationTargetInTransaction(value.getComputationTargetSpecification(), value.getValueName()));
        entry.setField(getLiveDataField(value.getFieldName()));
        entry.setValue(value.getValue());
        snapshot.addEntry(entry);
        changedEntries.add(entry);
      }
    }

    getHibernateTemplate().saveOrUpdateAll(changedEntries);
    getHibernateTemplate().flush();
  }

  // -------------------------------------------------------------------------------------------------------------------


  protected void addJobResultsInTransaction(UniqueId runUniqueId, ViewResultModel resultModel) {
    
    ArgumentChecker.notNull(runUniqueId, "runUniqueId");
    ArgumentChecker.notNull(resultModel, "resultModel");    
    final Long riskRunId = extractOid(runUniqueId);
    
    Map<ComputeFailureKey, ComputeFailure> computeFailureCache = _computeFailureCacheByRunId.get(riskRunId);
    Map<Pair<Long, Long>, StatusEntry> statusCache = _statusCacheByRunId.get(riskRunId);

    // STAGE 1. Populate error information in the cache.
    Map<ValueSpecification, BatchResultWriterFailure> errorCache = populateErrorCache(computeFailureCache, resultModel.getAllResults());

    for (String calcConfigName : resultModel.getCalculationConfigurationNames()) {


      // STAGE 2. Work out which targets:
      // 1) succeeded and should be written into rsk_value (because ALL items for that target succeeded)
      // 2) failed and should be written into rsk_failure (because AT LEAST ONE item for that target failed)


      ViewCalculationResultModel viewCalculationResultModel = resultModel.getCalculationResult(calcConfigName);

      Set<ComputationTargetSpecification> successfulTargets = newHashSet();
      Set<ComputationTargetSpecification> failedTargets = newHashSet();

      viewCalculationResultModel.getAllTargets();

      for (ComputationTargetSpecification targetSpecification : viewCalculationResultModel.getAllTargets()) {

        Collection<ComputedValue> values = viewCalculationResultModel.getAllValues(targetSpecification);

        if (failedTargets.contains(targetSpecification) || any(values, new Function1<ComputedValue, Boolean>() {
          @Override
          /**
           * Predcate checking for failed computation values or values for which there is no converter
           */
          public Boolean execute(ComputedValue cv) {
            if (cv.getInvocationResult() != InvocationResult.SUCCESS) {
              s_logger.error("The calculation of {} has failed, {}:{} ", newArray(cv.getSpecification(), cv.getInvocationResult(), cv.getExceptionMsg()));
              return true;
            } else {
              Object value = cv.getValue();
              try {
                _resultConverterCache.getConverter(value);
              } catch (IllegalArgumentException e) {
                s_logger.error("Cannot insert value of type " + value.getClass() + " for " + cv.getSpecification(), e);
                return true;
              }
              return false;
            }
          }
        })) {
          successfulTargets.remove(targetSpecification);
          failedTargets.add(targetSpecification);
        } else {
          successfulTargets.add(targetSpecification);
        }
      }

      // STAGE 3. Based on the results of stage 2, work out
      // SQL statements to write risk into rsk_value and rsk_failure (& rsk_failure_reason)

      List<DbMapSqlParameterSource> successes = newArrayList();
      List<DbMapSqlParameterSource> failures = newArrayList();
      List<DbMapSqlParameterSource> failureReasons = newArrayList();

      Instant evalInstant = Instant.now();

      Long calcConfId = _calculationConfigurations.get(calcConfigName);  

      for (ComputationTargetSpecification compTargetSpec : viewCalculationResultModel.getAllTargets()) {

        if (successfulTargets.contains(compTargetSpec)) {                    

          // make sure the values are not already in db, don't want to insert twice
          StatusEntry.Status status = getStatus(statusCache, calcConfigName, compTargetSpec);
          if (status == StatusEntry.Status.SUCCESS) {
            continue;
          }

          for (ComputedValue computedValue : viewCalculationResultModel.getAllValues(compTargetSpec)) {

            @SuppressWarnings("unchecked")
            ResultConverter<Object> resultConverter = (ResultConverter<Object>) _resultConverterCache.getConverter(computedValue.getValue());
            Map<String, Double> valuesAsDoubles = resultConverter.convert(computedValue.getSpecification().getValueName(), computedValue.getValue());

            Long computationTargetId = _computationTargets.get(compTargetSpec);

            for (Map.Entry<String, Double> riskValueEntry : valuesAsDoubles.entrySet()) {
              ValueSpecification specification = computedValue.getSpecification();

              ValueRequirement requirement = computedValue.getRequirement();

              Long valueRequirementId = _riskValueRequirements.get(requirement);
              Long valueSpecificationId = _riskValueSpecifications.get(specification);
              Long valueNameId = _riskValueNames.get(specification.getValueName());
              Long functionUniqueId = getFunctionUniqueIdInTransaction(specification.getFunctionUniqueId()).getId();
              Long computeNodeId = getOrCreateComputeNode(computedValue.getComputeNodeId()).getId();

              ArgumentChecker.notNull(calcConfId, "calcConfId");
              ArgumentChecker.notNull(valueNameId, "valueNameId");
              ArgumentChecker.notNull(valueRequirementId, "valueRequirementId");
              ArgumentChecker.notNull(valueSpecificationId, "valueSpecificationId");
              ArgumentChecker.notNull(functionUniqueId, "functionUniqueId");
              ArgumentChecker.notNull(computationTargetId, "computationTargetId");
              ArgumentChecker.notNull(riskRunId, "riskRunId");
              ArgumentChecker.notNull(computeNodeId, "computeNodeId");

              final DbMapSqlParameterSource insertArgs = new DbMapSqlParameterSource();
              final long successId = nextId("rsk_batch_seq");
              insertArgs.addValue("id", successId);
              insertArgs.addValue("calculation_configuration_id", calcConfId);
              insertArgs.addValue("value_name_id", valueNameId);
              insertArgs.addValue("value_requirement_id", valueRequirementId);
              insertArgs.addValue("value_specification_id", valueSpecificationId);
              insertArgs.addValue("function_unique_id", functionUniqueId);
              insertArgs.addValue("computation_target_id", computationTargetId);
              insertArgs.addValue("run_id", riskRunId);
              insertArgs.addValue("value", riskValueEntry.getValue());
              insertArgs.addTimestamp("eval_instant", evalInstant);
              insertArgs.addValue("compute_node_id", computeNodeId);
              successes.add(insertArgs);
            }
          }

          // the check below ensures that
          // if there is a partial failure (some successes, some failures) for a target,
          // only the failures will be written out in the database
        } else if (failedTargets.contains(compTargetSpec)) {

          Long computationTargetId = _computationTargets.get(compTargetSpec);

          for (ComputedValue computedValue : viewCalculationResultModel.getAllValues(compTargetSpec)) {
            ValueSpecification specification = computedValue.getSpecification();

            ValueRequirement requirement = computedValue.getRequirement();

            Long valueRequirementId = _riskValueRequirements.get(requirement);
            Long valueSpecificationId = _riskValueSpecifications.get(specification);
            Long valueNameId = _riskValueNames.get(specification.getValueName());
            Long functionUniqueId = getFunctionUniqueIdInTransaction(specification.getFunctionUniqueId()).getId();
            Long computeNodeId = getOrCreateComputeNode(computedValue.getComputeNodeId()).getId();

            ArgumentChecker.notNull(calcConfId, "calcConfId");
            ArgumentChecker.notNull(valueNameId, "valueNameId");
            ArgumentChecker.notNull(valueRequirementId, "valueRequirementId");
            ArgumentChecker.notNull(valueSpecificationId, "valueSpecificationId");
            ArgumentChecker.notNull(functionUniqueId, "functionUniqueId");
            ArgumentChecker.notNull(computationTargetId, "computationTargetId");
            ArgumentChecker.notNull(riskRunId, "riskRunId");
            ArgumentChecker.notNull(computeNodeId, "computeNodeId");

            final DbMapSqlParameterSource insertArgs = new DbMapSqlParameterSource();
            final long failureId = nextId("rsk_batch_seq");
            insertArgs.addValue("id", failureId);
            insertArgs.addValue("calculation_configuration_id", calcConfId);
            insertArgs.addValue("value_name_id", valueNameId);
            insertArgs.addValue("value_requirement_id", valueRequirementId);
            insertArgs.addValue("value_specification_id", valueSpecificationId);
            insertArgs.addValue("function_unique_id", functionUniqueId);
            insertArgs.addValue("computation_target_id", computationTargetId);
            insertArgs.addValue("run_id", riskRunId);
            insertArgs.addTimestamp("eval_instant", evalInstant);
            insertArgs.addValue("compute_node_id", computeNodeId);
            failures.add(insertArgs);

            switch (computedValue.getInvocationResult()) {

              case MISSING_INPUTS:
              case FUNCTION_THREW_EXCEPTION:

                BatchResultWriterFailure cachedFailure = errorCache.get(specification);
                if (cachedFailure != null) {
                  for (Number computeFailureId : cachedFailure.getComputeFailureIds()) {
                    ArgumentChecker.notNull(computeFailureId, "computeFailureId");
                    final DbMapSqlParameterSource failureReasonsInsertArgs = new DbMapSqlParameterSource();

                    failureReasonsInsertArgs.addValue("rsk_failure_id", failureId);
                    failureReasonsInsertArgs.addValue("compute_failure_id", computeFailureId);
                    failureReasons.add(failureReasonsInsertArgs);
                  }
                }

                break;

              case SUCCESS:

                // maybe this output succeeded, but some other outputs for the same target failed.
                s_logger.debug("Not adding any failure reasons for partial failures / unsupported outputs for now");
                break;

              default:
                throw new RuntimeException("Should not getId here");
            }

          }

        } else {
          // probably a PRIMITIVE target. See targetOutputMode == ResultOutputMode.NONE check above.
          s_logger.debug("Not writing anything for target {}", compTargetSpec);
        }
      }


      // STAGE 4. Actually execute the statements worked out in stage 3.

      if (successes.isEmpty()
        && failures.isEmpty()
        && failureReasons.isEmpty()
        && successfulTargets.isEmpty()
        && failedTargets.isEmpty()) {
        s_logger.debug("Nothing to write to DB for {}", resultModel);
        return;
      }

      getJdbcTemplate().batchUpdate(getExtSqlBundle().getSql("InsertRiskSuccess"), successes.toArray(new DbMapSqlParameterSource[failureReasons.size()]));
      getJdbcTemplate().batchUpdate(getExtSqlBundle().getSql("InsertRiskFailure"), failures.toArray(new DbMapSqlParameterSource[failureReasons.size()]));
      getJdbcTemplate().batchUpdate(getExtSqlBundle().getSql("InsertRiskFailureReason"), failureReasons.toArray(new DbMapSqlParameterSource[failureReasons.size()]));

      upsertStatusEntries(statusCache, calcConfigName, StatusEntry.Status.SUCCESS, successfulTargets);
      upsertStatusEntries(statusCache, calcConfigName, StatusEntry.Status.FAILURE, failedTargets);

    }
  }

  /**
   * STAGE 1. Populate error information in the cache.
   * This is done for all items and will populate table rsk_compute_failure. 
   */
  protected Map<ValueSpecification, BatchResultWriterFailure> populateErrorCache(Map<ComputeFailureKey, ComputeFailure> computeFailureCache, Collection<ViewResultEntry> results) {
    Map<ValueSpecification, BatchResultWriterFailure> errorCache = Maps.newHashMap();
    for (ViewResultEntry result : results) {
      populateErrorCache(computeFailureCache, errorCache, result);
    }
    return errorCache;
  }

  protected void upsertStatusEntries(
    Map<Pair<Long, Long>, StatusEntry> statusCache,
    String calcConfName,
    StatusEntry.Status status,
    Collection<ComputationTargetSpecification> targets) {

    Long calcConfId = _calculationConfigurations.get(calcConfName);

    List<DbMapSqlParameterSource> inserts = newArrayList();
    List<DbMapSqlParameterSource> updates = newArrayList();

    for (ComputationTargetSpecification target : targets) {
      Long computationTargetId = _computationTargets.get(target);

      DbMapSqlParameterSource params = new DbMapSqlParameterSource();

      // this assumes that _searchKey2StatusEntry has already been populated
      // in getStatus()
      Pair<Long, Long> key = Pair.of(calcConfId, computationTargetId);
      StatusEntry statusEntry = statusCache.get(key);
      if (statusEntry != null) {
        statusEntry.setStatus(status);
        params.addValue("id", statusEntry.getId());
        params.addValue("status", statusEntry.getStatus().ordinal());
        updates.add(params);
      } else {
        final long statusId = nextId("rsk_batch_seq");

        final DbMapSqlParameterSource insertArgs = new DbMapSqlParameterSource();
        insertArgs.addValue("ID", statusId);
        statusEntry = new StatusEntry();
        statusEntry.setId(statusId);
        statusEntry.setStatus(status);
        statusEntry.setCalculationConfigurationId(calcConfId);
        statusEntry.setComputationTargetId(computationTargetId);
        statusCache.put(key, statusEntry);

        params.addValue("id", statusId);
        params.addValue("calculation_configuration_id", calcConfId);
        params.addValue("computation_target_id", computationTargetId);
        params.addValue("status", statusEntry.getStatus().ordinal());
        inserts.add(params);
      }

    }

    s_logger.info("Inserting {} and updating {} {} status entries", newArray(inserts.size(), updates.size(), status));

    SqlParameterSource[] batchArgsArray = inserts.toArray(new DbMapSqlParameterSource[inserts.size()]);
    int[] counts = getJdbcTemplate().batchUpdate(getExtSqlBundle().getSql("InsertFromRunStatus"), batchArgsArray);
    checkCount(status + " insert", batchArgsArray, counts);

    batchArgsArray = updates.toArray(new DbMapSqlParameterSource[updates.size()]);
    counts = getJdbcTemplate().batchUpdate(getExtSqlBundle().getSql("UpdateFromRunStatus"), batchArgsArray);
    checkCount(status + " update", batchArgsArray, counts);

    s_logger.info("Inserted {} and updated {} {} status entries", newArray(inserts.size(), updates.size(), status));
  }

  private int checkCount(String rowType, SqlParameterSource[] batchArgsArray, int[] counts) {
    int totalCount = 0;
    for (int count : counts) {
      totalCount += count;
    }
    if (totalCount != batchArgsArray.length) {
      throw new RuntimeException(rowType + " insert count is wrong: expected = " +
        batchArgsArray.length + " actual = " + totalCount);
    }
    return totalCount;
  }

  protected StatusEntry.Status getStatus(Map<Pair<Long, Long>, StatusEntry> statusCache, String calcConfName, ComputationTargetSpecification ct) {
    Long calcConfId = _calculationConfigurations.get(calcConfName);
    Long computationTargetId = _computationTargets.get(ct);

    // first check to see if this status has already been queried for
    // and if the answer could therefore be found in the cache

    Pair<Long, Long> key = Pair.of(calcConfId, computationTargetId);
    if (statusCache.containsKey(key)) {
      StatusEntry existingStatusEntryInDb = statusCache.get(key);
      if (existingStatusEntryInDb != null) {
        // status entry in db.
        return existingStatusEntryInDb.getStatus();
      } else {
        // no status entry in db.
        return StatusEntry.Status.NOT_RUNNING;
      }
    }

    MapSqlParameterSource args = new MapSqlParameterSource();
    args.addValue("calculation_configuration_id", calcConfId);
    args.addValue("computation_target_id", computationTargetId);

    try {
      StatusEntry statusEntry = getJdbcTemplate().queryForObject(
        getExtSqlBundle().getSql("SelectStatusEntry"),
        StatusEntry.ROW_MAPPER,
        args);

      // status entry in db found.
      statusCache.put(key, statusEntry);

      return statusEntry.getStatus();

    } catch (IncorrectResultSizeDataAccessException e) {
      // no status entry in the db. 
      statusCache.remove(key);
      return StatusEntry.Status.NOT_RUNNING;
    }
  }


  protected void populateErrorCache(Map<ComputeFailureKey, ComputeFailure> computeFailureCache, Map<ValueSpecification, BatchResultWriterFailure> errorCache, ViewResultEntry item) {
    BatchResultWriterFailure cachedFailure = new BatchResultWriterFailure();

    switch (item.getComputedValue().getInvocationResult()) {

      case FUNCTION_THREW_EXCEPTION:

        // an "original" failure
        //
        // There will only be 1 failure reason.

        ComputeFailure computeFailure = getComputeFailureFromDb(computeFailureCache, item);
        cachedFailure.addComputeFailureId(computeFailure.getId());

        break;

      case MISSING_INPUTS:

        // There may be 1-N failure reasons - one for each failed
        // function in the subtree below this node. (This
        // only includes "original", i.e., lowest-level, failures.)

        for (ValueSpecification missingInput : item.getComputedValue().getMissingInputs()) {

          BatchResultWriterFailure inputFailure = errorCache.get(missingInput);

          if (inputFailure == null) {

            ComputeFailureKey computeFailureKey = new ComputeFailureKey(
              missingInput.getFunctionUniqueId(),
              "N/A",
              "Missing input " + missingInput,
              "N/A");
            computeFailure = getComputeFailureFromDb(computeFailureCache, computeFailureKey);
            cachedFailure.addComputeFailureId(computeFailure.getId());

          } else {

            cachedFailure.addComputeFailureIds(inputFailure.getComputeFailureIds());

          }
        }

        break;
    }

    if (!cachedFailure.getComputeFailureIds().isEmpty()) {
      errorCache.put(item.getComputedValue().getSpecification(), cachedFailure);
    }
  }

  /*package*/ ComputeFailure getComputeFailureFromDb(Map<ComputeFailureKey, ComputeFailure> computeFailureCache, ViewResultEntry item) {
    if (item.getComputedValue().getInvocationResult() != InvocationResult.FUNCTION_THREW_EXCEPTION) {
      throw new IllegalArgumentException("Please give a failed item");
    }

    ComputeFailureKey computeFailureKey = new ComputeFailureKey(
      item.getComputedValue().getSpecification().getFunctionUniqueId(),
      item.getComputedValue().getExceptionClass(),
      item.getComputedValue().getExceptionMsg(),
      item.getComputedValue().getStackTrace());
    return getComputeFailureFromDb(computeFailureCache, computeFailureKey);
  }

  public ComputeFailure getComputeFailureFromDb(Map<ComputeFailureKey, ComputeFailure> computeFailureCache, ComputeFailureKey computeFailureKey) {
    ComputeFailure computeFailure = computeFailureCache.get(computeFailureKey);
    if (computeFailure != null) {
      return computeFailure;
    }

    try {
      computeFailure = saveComputeFailure(computeFailureCache, computeFailureKey);
      return computeFailure;

    } catch (DataAccessException e) {
      // maybe the row was already there
      s_logger.debug("Failed to save compute failure", e);
    }

    try {
      int id = getJdbcTemplate().queryForInt(getExtSqlBundle().getSql("SelectComputeFailureId"), computeFailureKey.toSqlParameterSource());

      computeFailure = new ComputeFailure();
      computeFailure.setId(id);
      computeFailure.setFunctionId(computeFailureKey.getFunctionId());
      computeFailure.setExceptionClass(computeFailureKey.getExceptionClass());
      computeFailure.setExceptionMsg(computeFailureKey.getExceptionMsg());
      computeFailure.setStackTrace(computeFailureKey.getStackTrace());

      computeFailureCache.put(computeFailureKey, computeFailure);
      return computeFailure;

    } catch (IncorrectResultSizeDataAccessException e) {
      s_logger.error("Cannot getId {} from db", computeFailureKey);
      throw new RuntimeException("Cannot getId " + computeFailureKey + " from db", e);
    }
  }

  public ComputeFailure saveComputeFailure(Map<ComputeFailureKey, ComputeFailure> computeFailureCache, ComputeFailureKey computeFailureKey) {
    ComputeFailure computeFailure;
    computeFailure = new ComputeFailure();
    final long computeFailureId = nextId("rsk_batch_seq");
    computeFailure.setId(computeFailureId);
    computeFailure.setFunctionId(computeFailureKey.getFunctionId());
    computeFailure.setExceptionClass(computeFailureKey.getExceptionClass());
    computeFailure.setExceptionMsg(computeFailureKey.getExceptionMsg());
    computeFailure.setStackTrace(computeFailureKey.getStackTrace());

    int rowCount = getJdbcTemplate().update(getExtSqlBundle().getSql("InsertComputeFailure"), computeFailure.toSqlParameterSource());
    if (rowCount == 1) {
      computeFailureCache.put(computeFailureKey, computeFailure);
      return computeFailure;
    }
    return computeFailure;
  }

  /**
   * Instances of this class are saved in the computation cache for each
   * failure (whether the failure is 'original' or due to missing inputs).
   * The set of Longs is a set of compute failure IDs (referencing
   * rsk_compute_failure(id)). The set is built bottom up. 
   * For example, if A has two children, B and C, and B has failed
   * due to error 12, and C has failed due to errors 15 and 16, then
   * A has failed due to errors 12, 15, and 16.
   */
  protected static class BatchResultWriterFailure implements MissingInput, Serializable {
    /** Serialization version. */
    private static final long serialVersionUID = 1L;
    private Set<Long> _computeFailureIds = new HashSet<Long>();

    public Set<Long> getComputeFailureIds() {
      return Collections.unmodifiableSet(_computeFailureIds);
    }

    public void setComputeFailureIds(Set<Long> computeFailureIds) {
      _computeFailureIds = computeFailureIds;
    }

    public void addComputeFailureId(Long computeFailureId) {
      addComputeFailureIds(Collections.singleton(computeFailureId));
    }

    public void addComputeFailureIds(Set<? extends Long> computeFailureIds) {
      _computeFailureIds.addAll(computeFailureIds);
    }
  }
}
