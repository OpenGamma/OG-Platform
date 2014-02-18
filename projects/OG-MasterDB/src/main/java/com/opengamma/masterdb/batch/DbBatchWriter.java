/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.opengamma.lambdava.streams.Lambdava.newArray;
import static com.opengamma.util.db.HibernateDbUtils.eqOrIsNull;
import static org.apache.commons.lang.StringUtils.defaultString;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.joda.beans.Bean;
import org.joda.beans.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.threeten.bp.Instant;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.batch.RunCreationMode;
import com.opengamma.batch.SnapshotMode;
import com.opengamma.batch.domain.CalculationConfiguration;
import com.opengamma.batch.domain.ComputeFailure;
import com.opengamma.batch.domain.ComputeFailureKey;
import com.opengamma.batch.domain.ComputeHost;
import com.opengamma.batch.domain.ComputeNode;
import com.opengamma.batch.domain.FunctionUniqueId;
import com.opengamma.batch.domain.HbComputationTargetSpecification;
import com.opengamma.batch.domain.LiveDataField;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.domain.RiskValueRequirement;
import com.opengamma.batch.domain.RiskValueSpecification;
import com.opengamma.batch.domain.StatusEntry;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.InvocationResult;
import com.opengamma.engine.calcnode.MissingValue;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogWithContext;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.financial.conversion.ResultConverter;
import com.opengamma.financial.conversion.ResultConverterCache;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.masterdb.AbstractDbMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

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
public class DbBatchWriter extends AbstractDbMaster {

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbBat";
  /**
   * The batch risk sequence name.
   */
  public static final String RSK_SEQUENCE_NAME = "rsk_batch_seq";
  /**
   * The set of types to skip when encountered in market data because they already belong to a snapshot.
   */
  private static final Set<Class<?>> SKIP_MARKET_DATA_WRITE_TYPES = ImmutableSet.<Class<?>>of(SnapshotDataBundle.class);

  private final Map<String, Long> _calculationConfigurations = newConcurrentMap();
  private final Map<ValueRequirement, Long> _riskValueRequirements = newConcurrentMap();
  private final Map<ValueSpecification, Long> _riskValueSpecifications = newConcurrentMap();
  private final Map<ComputationTargetSpecification, Long> _computationTargets = newConcurrentMap();

  private final Map<Long, RiskRun> _riskRunsByIds = newConcurrentMap();
  private final Map<Long, Map<Pair<Long, Long>, StatusEntry>> _statusCacheByRunId = newConcurrentMap();
  private final Map<Long, Map<ComputeFailureKey, ComputeFailure>> _computeFailureCacheByRunId = newConcurrentMap();

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbBatchWriter.class);
  private final ComputationTargetResolver _computationTargetResolver;

  /**
   * The Result converter cache.
   */
  private ResultConverterCache _resultConverterCache;

  /**
   * Creates an instance.
   *
   * @param dbConnector  the database connector, not null
   * @param computationTargetResolver  the resolver
   */
  public DbBatchWriter(final DbConnector dbConnector, final ComputationTargetResolver computationTargetResolver) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
    _resultConverterCache = new ResultConverterCache();
    _computationTargetResolver = computationTargetResolver;
    setElSqlBundle(ElSqlBundle.of(dbConnector.getDialect().getElSqlConfig(), DbBatchWriter.class));
  }

  public RiskRun getRiskRunById(final Long id) {
    return getHibernateTemplate().execute(new HibernateCallback<RiskRun>() {
      @Override
      public RiskRun doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("RiskRun.one.byId");
        query.setLong("id", id);
        return (RiskRun) query.uniqueResult();
      }
    });
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

  protected MarketData getMarketDataInTransaction(final ObjectId snapshotId) {

    MarketData liveDataValues = getHibernateTemplate().execute(new HibernateCallback<MarketData>() {
      @Override
      public MarketData doInHibernate(Session session) throws HibernateException, SQLException {
        Long id = extractOid(snapshotId);
        return (MarketData) session.get(MarketData.class, id);
      }
    });

    if (liveDataValues == null) {
      throw new IllegalArgumentException("Snapshot for " + snapshotId + " cannot be found");
    }
    return liveDataValues;
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

  public HbComputationTargetSpecification getComputationTarget(final ComputationTargetSpecification spec) {
    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<HbComputationTargetSpecification>() {
      @Override
      public HbComputationTargetSpecification doInTransaction(final TransactionStatus status) {
        return getComputationTargetIntransaction(spec);
      }
    });
  }

  protected HbComputationTargetSpecification getComputationTargetIntransaction(final ComputationTargetSpecification spec) {
    return getHibernateTemplate().execute(new HibernateCallback<HbComputationTargetSpecification>() {
      @Override
      public HbComputationTargetSpecification doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.getNamedQuery("ComputationTargetSpecification.one.byTypeAndUid");

        query.setString("uidScheme", spec.getUniqueId().getScheme());
        query.setString("uidValue", spec.getUniqueId().getValue());
        query.setString("uidVersion", spec.getUniqueId().getVersion());

        query.setParameter("type", spec.getType());

        return (HbComputationTargetSpecification) query.uniqueResult();
      }
    });
  }

  protected HbComputationTargetSpecification getOrCreateComputationTargetInTransaction(final ComputationTargetSpecification spec) {
    HbComputationTargetSpecification hbComputationTargetSpecification = getComputationTarget(spec);
    if (hbComputationTargetSpecification == null) {
      hbComputationTargetSpecification = new HbComputationTargetSpecification();
      hbComputationTargetSpecification.setType(spec.getType());
      hbComputationTargetSpecification.setUniqueId(spec.getUniqueId());
      getHibernateTemplate().save(hbComputationTargetSpecification);
      getHibernateTemplate().flush();
    }
    return hbComputationTargetSpecification;
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


  protected Instant restartRunInTransaction(RiskRun riskRun) {
    Instant now = now();
    riskRun.setStartInstant(now);
    riskRun.setNumRestarts(riskRun.getNumRestarts() + 1);
    riskRun.setComplete(false);

    getHibernateTemplate().update(riskRun);
    getHibernateTemplate().flush();
    deleteRiskFailuresInTransaction(riskRun);

    return riskRun.getCreateInstant();
  }

  protected void populateRiskValueRequirements(ViewCycleMetadata cycleMetadata) {
    populateRiskValueSpecifications(cycleMetadata);
    
    Multimap<Map<String, Object>, ValueRequirement> data = ArrayListMultimap.create();
    for (final String configName : cycleMetadata.getAllCalculationConfigurationNames()) {
      Map<ValueSpecification, Set<ValueRequirement>> outputs = cycleMetadata.getTerminalOutputs(configName);
      for (ValueSpecification specification : outputs.keySet()) {
        Long specificationId = _riskValueSpecifications.get(specification);
        for (ValueRequirement requirement : outputs.get(specification)) {
          Map<String, Object> attribs = newHashMap();
          attribs.put("synthetic_form", RiskValueSpecification.synthesize(requirement.getConstraints()));
          attribs.put("specification_id", specificationId);
          data.put(attribs, requirement);
        }
      }
    }
    _riskValueRequirements.putAll(populate(data, getElSqlBundle().getSql("SelectRiskValueRequirement"), getElSqlBundle().getSql("InsertRiskValueRequirement"), RSK_SEQUENCE_NAME));
  }

  protected void populateRiskValueSpecifications(ViewCycleMetadata cycleMetadata) {
    Multimap<Map<String, Object>, ValueSpecification> data = ArrayListMultimap.create();
    for (final String configName : cycleMetadata.getAllCalculationConfigurationNames()) {
      for (ValueSpecification specification : cycleMetadata.getTerminalOutputs(configName).keySet()) {
        Map<String, Object> attribs = newHashMap();
        attribs.put("synthetic_form", RiskValueSpecification.synthesize(specification.getProperties()));
        data.put(attribs, specification);
      }
    }
    _riskValueSpecifications.putAll(populate(data, getElSqlBundle().getSql("SelectRiskValueSpecification"), getElSqlBundle().getSql("InsertRiskValueSpecification"), RSK_SEQUENCE_NAME));
  }

  protected void populateComputationTargets(Collection<ComputationTargetSpecification> computationTargetSpecifications) {
    Multimap<Map<String, Object>, ComputationTargetSpecification> computationTargetsData = ArrayListMultimap.create();
    for (ComputationTargetSpecification targetSpecification : computationTargetSpecifications) {
      Map<String, Object> attribs = newHashMap();
      
      String idScheme;
      String idValue;
      String idVersion;
      if (ComputationTargetSpecification.NULL.equals(targetSpecification)) {
        idScheme = null;
        idValue = null;
        idVersion = null;
      } else {
        UniqueId uniqueId = targetSpecification.getUniqueId();
        idScheme = uniqueId.getScheme();
        idValue = uniqueId.getValue();
        idVersion = uniqueId.getVersion();
      }
      attribs.put("id_scheme", idScheme);
      attribs.put("id_value", idValue);
      attribs.put("id_version", idVersion);
      attribs.put("type", targetSpecification.getType().toString());
      computationTargetsData.put(attribs, targetSpecification);
    }

    //------------------------------

    String selectComputationTargetSpecificationSql = getElSqlBundle().getSql("SelectComputationTargetSpecification");
    String selectComputationTargetSpecificationWithNullVersionSql = getElSqlBundle().getSql("SelectComputationTargetSpecificationWithNullVersion");
    String insertComputationTargetSpecificationSql = getElSqlBundle().getSql("InsertComputationTargetSpecification");


    final List<DbMapSqlParameterSource> insertArgsList = new ArrayList<DbMapSqlParameterSource>();

    Map<ComputationTargetSpecification, Long> cache = newHashMap();

    for (Map.Entry<Map<String, Object>, Collection<ComputationTargetSpecification>> attribsToObjects : computationTargetsData.asMap().entrySet()) {
      Map<String, Object> attribs = attribsToObjects.getKey();

      String selectSql;
      if (attribs.get("id_version") == null) {
        selectSql = selectComputationTargetSpecificationWithNullVersionSql;
      } else {
        selectSql = selectComputationTargetSpecificationSql;
      }

      final DbMapSqlParameterSource selectArgs = createParameterSource();
      for (String attribName : attribs.keySet()) {
        selectArgs.addValue(attribName, attribs.get(attribName));
      }
      List<Map<String, Object>> results = getJdbcTemplate().queryForList(selectSql, selectArgs);
      if (results.isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long id = nextId(RSK_SEQUENCE_NAME);
        final DbMapSqlParameterSource insertArgs = createParameterSource().addValue("id", id);
        for (String attribName : attribs.keySet()) {
          insertArgs.addValue(attribName, attribs.get(attribName));
        }
        insertArgsList.add(insertArgs);
        //
        for (ComputationTargetSpecification obj : attribsToObjects.getValue()) {
          cache.put(obj, id);
        }
      } else {
        Map<String, Object> result = results.get(0);
        for (ComputationTargetSpecification obj : attribsToObjects.getValue()) {
          cache.put(obj, ((Number) result.get("ID")).longValue());
        }
      }
    }
    getJdbcTemplate().batchUpdate(insertComputationTargetSpecificationSql, insertArgsList.toArray(new DbMapSqlParameterSource[insertArgsList.size()]));

    //------------------------------

    _computationTargets.putAll(cache);
  }

  protected void populateCalculationConfigurations(Long riskRunId, ViewCycleMetadata cycleMetadata) {
    Multimap<Map<String, Object>, String> data = ArrayListMultimap.create();
    for (final String configName : cycleMetadata.getAllCalculationConfigurationNames()) {
      Map<String, Object> map = newHashMap();
      map.put("name", configName);
      map.put("run_id", riskRunId);
      data.put(map, configName);
    }
    _calculationConfigurations.putAll(populate(data, getElSqlBundle().getSql("SelectConfigName"), getElSqlBundle().getSql("InsertConfigName"), RSK_SEQUENCE_NAME));
  }

  protected <T> Map<T, Long> populate(Multimap<Map<String, Object>, T> data, String selectSql, String insertSql, String pkSequenceName) {
    final List<DbMapSqlParameterSource> insertArgsList = new ArrayList<DbMapSqlParameterSource>();

    Map<T, Long> cache = newHashMap();
    for (Map.Entry<Map<String, Object>, Collection<T>> attribsToObjects : data.asMap().entrySet()) {
      Map<String, Object> attribs = attribsToObjects.getKey();
      final DbMapSqlParameterSource selectArgs = createParameterSource();
      for (String attribName : attribs.keySet()) {
        selectArgs.addValue(attribName, attribs.get(attribName));
      }
      List<Map<String, Object>> results = getJdbcTemplate().queryForList(selectSql, selectArgs);
      if (results.isEmpty()) {
        // select avoids creating unecessary id, but id may still not be used
        final long id = nextId(pkSequenceName);
        final DbMapSqlParameterSource insertArgs = createParameterSource().addValue("id", id);
        for (String attribName : attribs.keySet()) {
          insertArgs.addValue(attribName, attribs.get(attribName));
        }
        insertArgsList.add(insertArgs);
        //
        for (T obj : attribsToObjects.getValue()) {
          cache.put(obj, id);
        }
      } else {
        Map<String, Object> result = results.get(0);
        for (T obj : attribsToObjects.getValue()) {
          cache.put(obj, ((Number) result.get("ID")).longValue());
        }
      }
    }
    getJdbcTemplate().batchUpdate(insertSql, insertArgsList.toArray(new DbMapSqlParameterSource[insertArgsList.size()]));

    return cache;
  }

  protected Map<String, Object> getAttributes(Map<String, Object> attribs, String selectSql) {
    final DbMapSqlParameterSource selectArgs = createParameterSource();
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

  public void deleteSnapshotInTransaction(ObjectId batchSnapshotId) {
    final Long id = extractOid(batchSnapshotId);
    MapSqlParameterSource parameters = new MapSqlParameterSource()
      .addValue("snapshot_id", id);
    getJdbcTemplate().update(getElSqlBundle().getSql("DeleteDataSnapshotEntries"), parameters);
    getJdbcTemplate().update(getElSqlBundle().getSql("DeleteDataSnapshot"), parameters);
  }

  public void endBatchInTransaction(ObjectId batchUniqueId) {
    ArgumentChecker.notNull(batchUniqueId, "uniqueId");
    s_logger.info("Ending batch {}", batchUniqueId);
    RiskRun run = getRiskRun(batchUniqueId);
    //
    _statusCacheByRunId.remove(run.getId());
    _computeFailureCacheByRunId.remove(run.getId());
    _riskRunsByIds.remove(run.getId());
    //
    Instant now = now();
    run.setEndInstant(now);
    run.setComplete(true);
    getHibernateTemplate().update(run);
  }

  protected void deleteRiskValuesInTransaction(RiskRun riskRun) {
    MapSqlParameterSource parameters = new MapSqlParameterSource()
      .addValue("run_id", riskRun.getId());
    getJdbcTemplate().update(getElSqlBundle().getSql("DeleteRiskValues"), parameters);
  }

  protected void deleteRiskFailuresInTransaction(RiskRun riskRun) {
    MapSqlParameterSource parameters = new MapSqlParameterSource()
      .addValue("run_id", riskRun.getId());
    getJdbcTemplate().update(getElSqlBundle().getSql("DeleteRiskFailureReason"), parameters);
    getJdbcTemplate().update(getElSqlBundle().getSql("DeleteRiskFailure"), parameters);
  }

  protected void deleteRunInTransaction(RiskRun run) {
    s_logger.info("Deleting run {}", run);
    deleteRiskValuesInTransaction(run);
    deleteRiskFailuresInTransaction(run);
    getHibernateTemplate().deleteAll(run.getProperties());
    getHibernateTemplate().deleteAll(run.getCalculationConfigurations());
    getHibernateTemplate().delete(run);
    getHibernateTemplate().flush();
  }

  public void deleteBatchInTransaction(ObjectId batchUniqueId) {
    s_logger.info("Deleting batch {}", batchUniqueId);
    RiskRun run = getRiskRun(batchUniqueId);
    deleteRunInTransaction(run);
  }

  protected RiskRun findRiskRunInDbInTransaction(
    final Instant valuationTime,
    final VersionCorrection versionCorrection,
    final UniqueId viewDefinitionUid,
    final UniqueId marketDataBaseUid) {
    return getHibernateTemplate().execute(new HibernateCallback<RiskRun>() {
      @Override
      public RiskRun doInHibernate(Session session) throws HibernateException, SQLException {
        Criteria criteria = session.createCriteria(RiskRun.class);
        criteria.add(Restrictions.eq("valuationTime", valuationTime));
        criteria.add(Restrictions.eq("versionCorrection", versionCorrection));
        criteria.add(Restrictions.eq("viewDefinitionUidScheme", viewDefinitionUid.getScheme()));
        criteria.add(Restrictions.eq("viewDefinitionUidValue", viewDefinitionUid.getValue()));
        criteria.add(eqOrIsNull("viewDefinitionUidVersion", viewDefinitionUid.getVersion()));

        criteria.createCriteria("marketData")
          .add(Restrictions.eq("baseUidScheme", marketDataBaseUid.getScheme()))
          .add(Restrictions.eq("baseUidValue", marketDataBaseUid.getValue()))
          .add(eqOrIsNull("baseUidVersion", marketDataBaseUid.getVersion()));

        return (RiskRun) criteria.uniqueResult();
      }
    });
  }

  protected RiskRun findRiskRunInDb(
    final Instant valuationTime,
    final VersionCorrection versionCorrection,
    final UniqueId viewDefinitionUid,
    final UniqueId marketDataBaseUid) {
    return getTransactionTemplateRetrying(getMaxRetries()).execute(new TransactionCallback<RiskRun>() {
      @Override
      public RiskRun doInTransaction(final TransactionStatus status) {
        return findRiskRunInDbInTransaction(valuationTime, versionCorrection, viewDefinitionUid, marketDataBaseUid);
      }
    });
  }

  private RiskRun findRiskRunInDb(ObjectId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final Long id = extractOid(uniqueId);
    return getRiskRunById(id);
  }

  protected RiskRun getRiskRun(ObjectId batchId) {
    RiskRun run = findRiskRunInDb(batchId);
    if (run == null) {
      throw new DataNotFoundException("Cannot find run in database for " + batchId);
    } else {
      return run;
    }
  }

  protected RiskRun createRiskRunInTransaction(
    final UniqueId viewDefinitionUid,
    final UniqueId baseMarketDataSnapshotUid,
    final VersionCorrection versionCorrection,
    final Instant valuationTime,
    final Map<String, String> batchParameters,
    final SnapshotMode snapshotMode,
    final String name) {

    Instant now = Instant.now();

    MarketData values = createOrGetMarketDataInTransaction(baseMarketDataSnapshotUid);

    RiskRun riskRun = new RiskRun();
    riskRun.setMarketData(values);
    riskRun.setVersionCorrection(versionCorrection);
    riskRun.setViewDefinitionUid(viewDefinitionUid);
    riskRun.setValuationTime(valuationTime);
    riskRun.setCreateInstant(now);
    riskRun.setStartInstant(now);
    riskRun.setNumRestarts(0);
    riskRun.setComplete(false);
    riskRun.setSnapshotMode(snapshotMode);
    riskRun.setName(name);

    for (Map.Entry<String, String> parameter : batchParameters.entrySet()) {
      riskRun.addProperty(parameter.getKey(), parameter.getValue());
    }

    getHibernateTemplate().save(riskRun);
    getHibernateTemplate().saveOrUpdateAll(riskRun.getProperties());
    getHibernateTemplate().flush();

    return riskRun;
  }

  public synchronized RiskRun startBatchInTransaction(ViewCycleMetadata cycleMetadata, Map<String, String> batchParameters, RunCreationMode runCreationMode, SnapshotMode snapshotMode) {
    s_logger.info("Starting batch ... {}", cycleMetadata);

    RiskRun run;
    switch (runCreationMode) {
      case AUTO:
        run = findRiskRunInDb(cycleMetadata.getValuationTime(), cycleMetadata.getVersionCorrection(),
            cycleMetadata.getViewDefinitionId(), cycleMetadata.getMarketDataSnapshotId());

        if (run != null) {
          // also check parameter equality
          Map<String, String> existingProperties = run.getPropertiesMap();

          if (!existingProperties.equals(batchParameters)) {
            Set<Map.Entry<String, String>> symmetricDiff = Sets.symmetricDifference(existingProperties.entrySet(), batchParameters.entrySet());
            throw new IllegalStateException("Run parameters stored in DB differ from new parameters with respect to: " + symmetricDiff);
          }
        }

        if (run == null) {
          run = createRiskRunInTransaction(cycleMetadata.getViewDefinitionId(), cycleMetadata.getMarketDataSnapshotId(),
              cycleMetadata.getVersionCorrection(), cycleMetadata.getValuationTime(), batchParameters, snapshotMode, cycleMetadata.getName());
        } else {
          restartRunInTransaction(run);
        }
        break;

      case CREATE_NEW_OVERWRITE:
        run = findRiskRunInDb(cycleMetadata.getValuationTime(), cycleMetadata.getVersionCorrection(), cycleMetadata.getViewDefinitionId(), cycleMetadata.getMarketDataSnapshotId());
        if (run != null) {
          deleteRunInTransaction(run);
        }

        run = createRiskRunInTransaction(cycleMetadata.getViewDefinitionId(), cycleMetadata.getMarketDataSnapshotId(),
            cycleMetadata.getVersionCorrection(), cycleMetadata.getValuationTime(), batchParameters, snapshotMode, cycleMetadata.getName());
        break;

      case CREATE_NEW:
        run = createRiskRunInTransaction(cycleMetadata.getViewDefinitionId(), cycleMetadata.getMarketDataSnapshotId(),
            cycleMetadata.getVersionCorrection(), cycleMetadata.getValuationTime(), batchParameters, snapshotMode, cycleMetadata.getName());
        break;

      case REUSE_EXISTING:
        run = findRiskRunInDb(cycleMetadata.getValuationTime(), cycleMetadata.getVersionCorrection(), cycleMetadata.getViewDefinitionId(), cycleMetadata.getMarketDataSnapshotId());
        if (run == null) {
          throw new IllegalStateException("Cannot find run in database for " + cycleMetadata);
        }
        restartRunInTransaction(run);
        break;

      default:
        throw new RuntimeException("Unexpected run creation mode " + runCreationMode);
    }

    populateCalculationConfigurations(run.getId(), cycleMetadata);
    populateRiskValueRequirements(cycleMetadata);

    Collection<ComputationTargetSpecification> computationTargets = newArrayList();
    for (final String configName : cycleMetadata.getAllCalculationConfigurationNames()) {
      for (com.opengamma.engine.ComputationTargetSpecification computationTarget : cycleMetadata.getComputationTargets(configName)) {
        computationTargets.add(computationTarget);
      }
    }
    populateComputationTargets(computationTargets);

    _statusCacheByRunId.put(run.getId(), new ConcurrentHashMap<Pair<Long, Long>, StatusEntry>());
    _computeFailureCacheByRunId.put(run.getId(), new ConcurrentHashMap<ComputeFailureKey, ComputeFailure>());

    _riskRunsByIds.put(run.getId(), run);

    return run;
  }

  public MarketData createOrGetMarketDataInTransaction(final UniqueId baseUid) {
    s_logger.info("Creating Market Data {} ", baseUid);
    MarketData marketData = getHibernateTemplate().execute(new HibernateCallback<MarketData>() {
      @Override
      public MarketData doInHibernate(Session session) throws HibernateException, SQLException {

        final DetachedCriteria criteria = DetachedCriteria.forClass(MarketData.class);
        criteria.add(Restrictions.eq("baseUidScheme", baseUid.getScheme()))
          .add(Restrictions.eq("baseUidValue", baseUid.getValue()))
          .add(eqOrIsNull("baseUidVersion", baseUid.getVersion()));

        @SuppressWarnings("unchecked")
        List<MarketData> datas = getHibernateTemplate().findByCriteria(criteria, 0, 1);
        if (datas.size() > 0) {
          return datas.get(0);
        } else {
          return null;
        }
      }
    });
    if (marketData != null) {
      s_logger.info("Snapshot " + baseUid + " already exists. No need to create.");
    } else {
      marketData = new MarketData();
      marketData.setBaseUid(baseUid);
      getHibernateTemplate().save(marketData);
      getHibernateTemplate().flush();
    }
    return marketData;
  }
  
  @SuppressWarnings("unchecked")
  public void addComputedValuesToMarketDataInTransaction(ObjectId marketDataId, Set<ComputedValue> values) {
    if (values == null || values.isEmpty()) {
      return;
    }
    Set<MarketDataValue> marketDataValues = newHashSet();
    for (ComputedValue value : values) {
      if (value.getValue() != null && SKIP_MARKET_DATA_WRITE_TYPES.contains(value.getValue().getClass())) {
        s_logger.debug("Skipping market data value " + value + " because market data already persisted in snapshot");
        continue;
      }
      final ResultConverter<Object> resultConverter;
      try {
        resultConverter = (ResultConverter<Object>) _resultConverterCache.getConverter(value.getValue());
      } catch (IllegalArgumentException e) {
        s_logger.error("No converter for market data value of type " + value.getValue().getClass() + " for " + value.getSpecification());
        continue;
      }
      Map<String, Double> valueAsDoublesMap = resultConverter.convert(value.getSpecification().getValueName(), value.getValue());
      for (Map.Entry<String, Double> valueEntry : valueAsDoublesMap.entrySet()) {
        final String doubleValueName = valueEntry.getKey();
        final Double doubleValue = ensureDatabasePrecision(valueEntry.getValue());
        if (doubleValue == null) {
          //TODO - this should record a MissingInput failure.
          continue;
        }
        marketDataValues.add(new MarketDataValue(value.getSpecification().getTargetSpecification(), doubleValue, doubleValueName));
      }
    }
    addValuesToMarketDataInTransaction(marketDataId, marketDataValues);
  }

  public void addValuesToMarketDataInTransaction(ObjectId marketDataId, Set<MarketDataValue> values) {
    if (values == null || values.isEmpty()) {
      return;
    }
    s_logger.info("Adding {} market data values to {}", values.size(), marketDataId);
    
    MarketData marketData = getMarketDataInTransaction(marketDataId);
    if (marketData == null) {
      throw new IllegalArgumentException("Market data " + marketDataId + " cannot be found");
    }
    
    List<DbMapSqlParameterSource> marketDataValuesInserts = newArrayList();

    Collection<ComputationTargetSpecification> computationTargetSpecifications = newHashSet();
    for (MarketDataValue value : values) {
      ComputationTargetSpecification targetSpecification = value.getComputationTargetSpecification();
      computationTargetSpecifications.add(targetSpecification);
    }
    populateComputationTargets(computationTargetSpecifications);

    Collection<Long> ids = newArrayList();

    for (MarketDataValue value : values) {
      final ComputationTargetSpecification targetSpec = value.getComputationTargetSpecification();
      final long id = nextId(RSK_SEQUENCE_NAME);
      ids.add(id);
      final DbMapSqlParameterSource insertArgs = createParameterSource()
        .addValue("id", id)
        .addValue("snapshot_id", marketData.getId())
        .addValue("computation_target_id", _computationTargets.get(targetSpec))
        .addValue("name", value.getName())
        .addValue("value", value.getValue());
      marketDataValuesInserts.add(insertArgs);          
    }

    getJdbcTemplate().batchUpdate(
      getElSqlBundle().getSql("InsertMarketDataValue"),
      marketDataValuesInserts.toArray(new DbMapSqlParameterSource[marketDataValuesInserts.size()])
    );

    String sqlUpdate = getElSqlBundle().getSql("CopyMarketDataValue").replace("INSERTION_IDS", StringUtils.join(ids, ", "));
    getJdbcTemplate().getJdbcOperations().update(sqlUpdate);
    
    String sqlDelete = getElSqlBundle().getSql("DeleteMarketDataValue").replace("INSERTION_IDS", StringUtils.join(ids, ", "));
    getJdbcTemplate().getJdbcOperations().update(sqlDelete);
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  public synchronized void addJobResultsInTransaction(TransactionStatus transactionStatus, ObjectId runId, ViewComputationResultModel resultModel) {
    ArgumentChecker.notNull(runId, "runId");
    ArgumentChecker.notNull(resultModel, "resultModel");
    
    final long riskRunId = extractOid(runId);
    ArgumentChecker.notNull(riskRunId, "riskRunId");

    Map<ComputeFailureKey, ComputeFailure> computeFailureCache = _computeFailureCacheByRunId.get(riskRunId);
    Map<Pair<Long, Long>, StatusEntry> statusCache = _statusCacheByRunId.get(riskRunId);

    Map<ValueSpecification, BatchResultWriterFailure> errorCache = populateErrorCache(computeFailureCache, resultModel.getAllResults());
    
    RiskRun run = _riskRunsByIds.get(riskRunId);
    if (run.getSnapshotMode().equals(SnapshotMode.WRITE_THROUGH)) {
      addComputedValuesToMarketDataInTransaction(run.getMarketData().getObjectId(), resultModel.getAllMarketData());
    }

    for (String calcConfigName : resultModel.getCalculationConfigurationNames()) {
      ViewCalculationResultModel viewCalculationResultModel = resultModel.getCalculationResult(calcConfigName);
      
      final Set<ComputationTargetSpecification> successfulTargets = newHashSet();
      final Set<ComputationTargetSpecification> failedTargets = newHashSet();

      List<SqlParameterSource> targetProperties = newArrayList();
      List<SqlParameterSource> successes = newArrayList();
      List<SqlParameterSource> failures = newArrayList();
      List<SqlParameterSource> failureReasons = newArrayList();

      Instant evalInstant = Instant.now();

      long calcConfId = _calculationConfigurations.get(calcConfigName);

      for (final ComputationTargetSpecification targetSpec : viewCalculationResultModel.getAllTargets()) {
        final long computationTargetId = _computationTargets.get(targetSpec);
        boolean specFailures = false;
        for (final ComputedValueResult computedValue : viewCalculationResultModel.getAllValues(targetSpec)) {
          ResultConverter<Object> resultConverter  = null;
          if (!(computedValue.getValue() instanceof MissingValue)) {
            try {
              resultConverter = (ResultConverter<Object>) _resultConverterCache.getConverter(computedValue.getValue());
            } catch (IllegalArgumentException e) {
              s_logger.info("No converter for value of type " + computedValue.getValue().getClass() + " for " + computedValue.getSpecification());
            }            
          }

          final ValueSpecification specification = computedValue.getSpecification();
          if (!_riskValueSpecifications.containsKey(specification)) {
            s_logger.error("Unexpected result specification " + specification + ". Result cannot be written. Result value was " + computedValue.getValue());
            continue;
          }
          final long valueSpecificationId = _riskValueSpecifications.get(specification);
          final long functionUniqueId = getFunctionUniqueIdInTransaction(specification.getFunctionUniqueId()).getId();
          final long computeNodeId = getOrCreateComputeNode(computedValue.getComputeNodeId()).getId();
          
          if (resultConverter != null && computedValue.getInvocationResult() == InvocationResult.SUCCESS) {
            s_logger.debug("Writing value {} for value spec {}", computedValue.getValue(), specification);
            Map<String, Double> valueAsDoublesMap = resultConverter.convert(computedValue.getSpecification().getValueName(), computedValue.getValue());
            for (Map.Entry<String, Double> valueEntry : valueAsDoublesMap.entrySet()) {
              final String valueName = valueEntry.getKey();
              final Double doubleValue = ensureDatabasePrecision(valueEntry.getValue());
              final long successId = nextId(RSK_SEQUENCE_NAME);
              successes.add(getSuccessArgs(successId, riskRunId, evalInstant, calcConfId, computationTargetId, valueSpecificationId, functionUniqueId, computeNodeId, valueName, doubleValue));
            }
          } else {
            s_logger.info("Writing failure for {} with invocation result {}, {} ",
                newArray(computedValue.getSpecification(), computedValue.getInvocationResult(), computedValue.getAggregatedExecutionLog()));
            specFailures = true;
            
            final long failureId = nextId(RSK_SEQUENCE_NAME);
            failures.add(getFailureArgs(failureId, riskRunId, evalInstant, calcConfId, computationTargetId, valueSpecificationId, functionUniqueId, computeNodeId, specification.getValueName()));

            BatchResultWriterFailure cachedFailure = errorCache.get(specification);
            if (cachedFailure != null) {
              for (long computeFailureId : cachedFailure.getComputeFailureIds()) {
                ArgumentChecker.notNull(computeFailureId, "computeFailureId");
                final long failureReasonId = nextId(RSK_SEQUENCE_NAME);
                failureReasons.add(getFailureReasonArgs(failureReasonId, failureId, computeFailureId));
              }
            }       
          }
        }
        StatusEntry.Status status = getStatus(statusCache, calcConfigName, targetSpec);
        if (specFailures || status == StatusEntry.Status.FAILURE) {
          successfulTargets.remove(targetSpec);
          failedTargets.add(targetSpec);
        } else {
          successfulTargets.add(targetSpec);
        }

        // storing target data
        ComputationTarget computationTarget = _computationTargetResolver.resolve(targetSpec, VersionCorrection.LATEST);
        Object targetValue = computationTarget.getValue();
        if (targetValue instanceof Bean) {
          Bean bean = (Bean) targetValue;
          for (String propertyName : bean.propertyNames()) {
            Property<Object> property = bean.property(propertyName);
            final long targetPropertyId = nextId(RSK_SEQUENCE_NAME);
            targetProperties.add(getTargetPropertyArgs(targetPropertyId, computationTargetId, propertyName, property.get() == null ? "NULL" : property.get().toString()));
          }
        }
      }

      if (successes.isEmpty()
        && failures.isEmpty()
        && failureReasons.isEmpty()
        && successfulTargets.isEmpty()
        && failedTargets.isEmpty()) {
        s_logger.debug("Nothing to write to DB for {}", resultModel);
        return;
      }

      Object preSuccessSavepoint = transactionStatus.createSavepoint();
      try {
        getJdbcTemplate().batchUpdate(getElSqlBundle().getSql("InsertRiskSuccess"), successes.toArray(new DbMapSqlParameterSource[successes.size()]));
      } catch (Exception e) {
        s_logger.error("Failed to write successful calculations to batch database. Converting to failures.", e);
        transactionStatus.rollbackToSavepoint(preSuccessSavepoint);
        if (!successes.isEmpty()) {
          String exceptionClass = e.getClass().getName();
          String exceptionMsg = e.getMessage();
          final StringBuilder buffer = new StringBuilder();
          for (StackTraceElement element : e.getStackTrace()) {
            buffer.append(element.toString()).append("\n");
          }
          final String stackTrace = buffer.toString();
          for (SqlParameterSource success : successes) {
            failures.add(convertSuccessToFailure(success));
            final long failureId = getId(success);
            final long functionId = getFunctionId(success);
            ComputeFailureKey computeFailureKey = new ComputeFailureKey(String.valueOf(functionId), exceptionClass, exceptionMsg, stackTrace);
            ComputeFailure computeFailure = getComputeFailureFromDb(computeFailureCache, computeFailureKey);
            final long failureReasonId = nextId(RSK_SEQUENCE_NAME);
            failureReasons.add(getFailureReasonArgs(failureReasonId, failureId, computeFailure.getId()));
          }
          failedTargets.addAll(successfulTargets);
          successes.clear();
          successfulTargets.clear();
          targetProperties.clear();
        }
      }
      Object preTargetPropertiesFailureSavepoint = transactionStatus.createSavepoint();
      try {
        getJdbcTemplate().batchUpdate(getElSqlBundle().getSql("InsertTargetProperties"), targetProperties.toArray(new DbMapSqlParameterSource[targetProperties.size()]));
      } catch (Exception e) {
        s_logger.error("Failed to write target properties to batch database", e);
        transactionStatus.rollbackToSavepoint(preTargetPropertiesFailureSavepoint);
      }
      Object preFailureSavepoint = transactionStatus.createSavepoint();
      try {
        getJdbcTemplate().batchUpdate(getElSqlBundle().getSql("InsertRiskFailure"), failures.toArray(new DbMapSqlParameterSource[failures.size()]));
        getJdbcTemplate().batchUpdate(getElSqlBundle().getSql("InsertRiskFailureReason"), failureReasons.toArray(new DbMapSqlParameterSource[failureReasons.size()]));
      } catch (Exception e) {
        s_logger.error("Failed to write failures to batch database", e);
        transactionStatus.rollbackToSavepoint(preFailureSavepoint);
      }

      updateStatusEntries(riskRunId, statusCache, calcConfigName, StatusEntry.Status.SUCCESS, successfulTargets);
      updateStatusEntries(riskRunId, statusCache, calcConfigName, StatusEntry.Status.FAILURE, failedTargets);
    }
  }

  private long getFunctionId(SqlParameterSource args) {
    return (Long) args.getValue("function_unique_id");
  }
  
  private long getId(SqlParameterSource args) {
    return (Long) args.getValue("id");
  }

  private DbMapSqlParameterSource getFailureReasonArgs(long failureReasonId, long failureId, long computeFailureId) {
    final DbMapSqlParameterSource args = createParameterSource();
    args.addValue("id", failureReasonId);
    args.addValue("rsk_failure_id", failureId);
    args.addValue("compute_failure_id", computeFailureId);
    return args;
  }

  private SqlParameterSource getSuccessArgs(long successId, long riskRunId, Instant evalInstant, long calcConfId,
      long computationTargetId, long valueSpecificationId, long functionUniqueId, long computeNodeId, String valueName, Double doubleValue) {
    DbMapSqlParameterSource args = createParameterSource();
    args.addValue("id", successId);
    args.addValue("calculation_configuration_id", calcConfId);
    args.addValue("name", valueName);
    args.addValue("value_specification_id", valueSpecificationId);
    args.addValue("function_unique_id", functionUniqueId);
    args.addValue("computation_target_id", computationTargetId);
    args.addValue("run_id", riskRunId);
    args.addValue("value", doubleValue);
    args.addTimestamp("eval_instant", evalInstant);
    args.addValue("compute_node_id", computeNodeId);
    return args;
  }

  private SqlParameterSource getTargetPropertyArgs(long targetPropertyId, long computationTargetId, String propertyKey, String propertyValue) {
    DbMapSqlParameterSource args = createParameterSource();
    args.addValue("id", targetPropertyId);
    args.addValue("target_id", computationTargetId);
    args.addValue("property_key", propertyKey);
    args.addValue("property_value", propertyValue);
    return args;
  }

  private SqlParameterSource getFailureArgs(long failureId, long riskRunId, Instant evalInstant, long calcConfId,
      long computationTargetId, long valueSpecificationId, long functionUniqueId, long computeNodeId, String valueName) {
    DbMapSqlParameterSource args = createParameterSource();
    args.addValue("id", failureId);
    args.addValue("calculation_configuration_id", calcConfId);
    args.addValue("name", valueName);
    args.addValue("value_specification_id", valueSpecificationId);
    args.addValue("function_unique_id", functionUniqueId);
    args.addValue("computation_target_id", computationTargetId);
    args.addValue("run_id", riskRunId);
    args.addTimestamp("eval_instant", evalInstant);
    args.addValue("compute_node_id", computeNodeId);
    return args;
  }
  
  private SqlParameterSource convertSuccessToFailure(SqlParameterSource successArgs) {
    return getFailureArgs(
        (Long) successArgs.getValue("id"),
        (Long) successArgs.getValue("run_id"),
        DbDateUtils.fromSqlTimestamp((Timestamp) successArgs.getValue("eval_instant")),
        (Long) successArgs.getValue("calculation_configuration_id"),
        (Long) successArgs.getValue("computation_target_id"),
        (Long) successArgs.getValue("value_specification_id"),
        (Long) getFunctionId(successArgs),
        (Long) successArgs.getValue("compute_node_id"),
        (String) successArgs.getValue("name"));
  }

  /**
   * STAGE 1. Populate error information in the cache.
   * This is done for all items and will populate table rsk_compute_failure.
   * 
   * @param computeFailureCache  the cache
   * @param results  the results
   * @return the error cache, not null
   */
  protected Map<ValueSpecification, BatchResultWriterFailure> populateErrorCache(
      Map<ComputeFailureKey, ComputeFailure> computeFailureCache, Collection<ViewResultEntry> results) {
    Map<ValueSpecification, BatchResultWriterFailure> errorCache = Maps.newHashMap();
    for (ViewResultEntry result : results) {
      populateErrorCache(computeFailureCache, errorCache, result);
    }
    return errorCache;
  }

  @SuppressWarnings("unchecked")
  protected void updateStatusEntries(long runId,
                                     Map<Pair<Long, Long>, StatusEntry> statusCache,
                                     String calcConfName,
                                     StatusEntry.Status status,
                                     Collection<ComputationTargetSpecification> targets) {

    Long calcConfId = _calculationConfigurations.get(calcConfName);

    List<DbMapSqlParameterSource> inserts = newArrayList();
    List<DbMapSqlParameterSource> updates = newArrayList();

    for (ComputationTargetSpecification target : targets) {
      Long computationTargetId = _computationTargets.get(target);

      DbMapSqlParameterSource params = createParameterSource();

      // this assumes that _searchKey2StatusEntry has already been populated
      // in getStatus()
      Pair<Long, Long> key = Pairs.of(calcConfId, computationTargetId);
      StatusEntry statusEntry = statusCache.get(key);
      if (statusEntry != null) {
        statusEntry.setStatus(status);
        params.addValue("id", statusEntry.getId());
        params.addValue("run_id", runId);
        params.addValue("status", statusEntry.getStatus().ordinal());
        updates.add(params);
      } else {
        final long statusId = nextId(RSK_SEQUENCE_NAME);

        final DbMapSqlParameterSource insertArgs = createParameterSource();
        insertArgs.addValue("ID", statusId);
        statusEntry = new StatusEntry();
        statusEntry.setId(statusId);
        statusEntry.setRunId(runId);
        statusEntry.setStatus(status);
        statusEntry.setCalculationConfigurationId(calcConfId);
        statusEntry.setComputationTargetId(computationTargetId);
        statusCache.put(key, statusEntry);

        params.addValue("id", statusId);
        params.addValue("run_id", runId);
        params.addValue("calculation_configuration_id", calcConfId);
        params.addValue("computation_target_id", computationTargetId);
        params.addValue("status", statusEntry.getStatus().ordinal());
        inserts.add(params);
      }

    }

    s_logger.info("Inserting {} and updating {} {} status entries", (Object[]) newArray(inserts.size(), updates.size(), status));

    SqlParameterSource[] batchArgsArray = inserts.toArray(new DbMapSqlParameterSource[inserts.size()]);
    int[] counts = getJdbcTemplate().batchUpdate(getElSqlBundle().getSql("InsertFromRunStatus"), batchArgsArray);
    checkCount(status + " insert", batchArgsArray, counts);

    batchArgsArray = updates.toArray(new DbMapSqlParameterSource[updates.size()]);
    counts = getJdbcTemplate().batchUpdate(getElSqlBundle().getSql("UpdateFromRunStatus"), batchArgsArray);
    checkCount(status + " update", batchArgsArray, counts);

    s_logger.info("Inserted {} and updated {} {} status entries", (Object[]) newArray(inserts.size(), updates.size(), status));
  }

  private int checkCount(String rowType, SqlParameterSource[] batchArgsArray, int[] counts) {
    int totalCount = 0;
    for (int count : counts) {
      totalCount += count;
    }
    if (totalCount != batchArgsArray.length && totalCount != -2) {
      throw new RuntimeException(rowType + " insert count is wrong: expected = " +
        batchArgsArray.length + " actual = " + totalCount);
    }
    return totalCount;
  }
  
  private static Double ensureDatabasePrecision(Double value) {
    if (value == null) {
      return null;
    }
    // Java's smallest double is 4.9e-324, but most databases would underflow.
    // Postgres is 1e-307, Oracle is 2.2e-307, SQL Server is 2.2e-308.
    if (Math.abs(value) < 1e-300) {
      return 0d;
    }
    return value;
  }

  protected StatusEntry.Status getStatus(Map<Pair<Long, Long>, StatusEntry> statusCache, String calcConfName, ComputationTargetSpecification ct) {
    Long calcConfId = _calculationConfigurations.get(calcConfName);
    Long computationTargetId = _computationTargets.get(ct);

    // first check to see if this status has already been queried for
    // and if the answer could therefore be found in the cache

    Pair<Long, Long> key = Pairs.of(calcConfId, computationTargetId);
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
        getElSqlBundle().getSql("SelectStatusEntry"),
        args,
        DbBatchUtils.ROW_MAPPER);

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

    ExecutionLogWithContext rootLog = item.getComputedValue().getAggregatedExecutionLog().getRootLog();
    String exceptionClass = rootLog != null ? rootLog.getExecutionLog().getExceptionClass() : null;
    String exceptionMessage = rootLog != null ? rootLog.getExecutionLog().getExceptionMessage() : null;
    String exceptionStackTrace = rootLog != null ? rootLog.getExecutionLog().getExceptionStackTrace() : null;
    //ensure we don't end up with null going into the ComputeFailureKey for these strings.
    //this will probably be due to the fact that the rootLog was null.
    exceptionClass = defaultString(exceptionClass, "No logging information available");
    exceptionMessage = defaultString(exceptionMessage, "No logging information available");
    exceptionStackTrace = defaultString(exceptionStackTrace, "No logging information available");
    ComputeFailureKey computeFailureKey = new ComputeFailureKey(item.getComputedValue().getSpecification().getFunctionUniqueId(), exceptionClass, exceptionMessage, exceptionStackTrace);
    return getComputeFailureFromDb(computeFailureCache, computeFailureKey);
  }

  public ComputeFailure getComputeFailureFromDb(Map<ComputeFailureKey, ComputeFailure> computeFailureCache, ComputeFailureKey computeFailureKey) {
    ComputeFailure computeFailure = computeFailureCache.get(computeFailureKey);
    if (computeFailure != null) {
      return computeFailure;
    }
    try {
      int id = getJdbcTemplate().queryForObject(getElSqlBundle().getSql("SelectComputeFailureId"), DbBatchUtils.toSqlParameterSource(computeFailureKey), Integer.class);
      computeFailure = new ComputeFailure();
      computeFailure.setId(id);
      computeFailure.setFunctionId(computeFailureKey.getFunctionId());
      computeFailure.setExceptionClass(computeFailureKey.getExceptionClass());
      computeFailure.setExceptionMsg(computeFailureKey.getExceptionMsg());
      computeFailure.setStackTrace(computeFailureKey.getStackTrace());
      computeFailureCache.put(computeFailureKey, computeFailure);
      return computeFailure;
    } catch (IncorrectResultSizeDataAccessException e) {
      // Not seen a failure like this before - create and write to database
      try {
        computeFailure = saveComputeFailure(computeFailureCache, computeFailureKey);
        return computeFailure;
      } catch (DataAccessException e2) {
        s_logger.error("Failed to save compute failure", e2);
        throw new RuntimeException("Failed to save compute failure", e2);
      }
    }
  }

  public ComputeFailure saveComputeFailure(Map<ComputeFailureKey, ComputeFailure> computeFailureCache, ComputeFailureKey computeFailureKey) {
    ComputeFailure computeFailure;
    computeFailure = new ComputeFailure();
    final long computeFailureId = nextId(RSK_SEQUENCE_NAME);
    computeFailure.setId(computeFailureId);
    computeFailure.setFunctionId(computeFailureKey.getFunctionId());
    computeFailure.setExceptionClass(computeFailureKey.getExceptionClass());
    computeFailure.setExceptionMsg(computeFailureKey.getExceptionMsg());
    computeFailure.setStackTrace(computeFailureKey.getStackTrace());

    int rowCount = getJdbcTemplate().update(getElSqlBundle().getSql("InsertComputeFailure"), DbBatchUtils.toSqlParameterSource(computeFailure));
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
  protected static class BatchResultWriterFailure implements MissingValue, Serializable {
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
