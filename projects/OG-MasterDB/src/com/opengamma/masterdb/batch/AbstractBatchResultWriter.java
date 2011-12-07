/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ValueProperties;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.financial.conversion.ResultConverterCache;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.tuple.Pair;

/**
 * Writes risk into the OpenGamma batch risk database.
 * <p>
 * For the database structure and tables, see {@code create-db-batch.sql}.
 */
public abstract class AbstractBatchResultWriter {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractBatchResultWriter.class);

  /**
   * DB connector.
   */
  private final DbConnector _dbConnector;
  
  /**
   * References rsk_run(id)
   */
  private final Integer _riskRunId;
  
  /**
   * -> references rsk_computation_target(id)   
   */
  private final Map<ComputationTargetSpecification, Integer> _computationTarget2Id = new HashMap<ComputationTargetSpecification, Integer>();
  
  /**
   * -> references rsk_calculation_configuration(id)
   */
  private final Map<String, Integer> _calculationConfiguration2Id = new HashMap<String, Integer>();
  
  /**
   * -> references rsk_value_name(id)
   */
  private final Map<String, Integer> _riskValueName2Id = new HashMap<String, Integer>();

  /**
   * -> references rsk_value_requirement(id)
   */
  private final Map<ValueProperties, Integer> _valueRequirement2Id = new HashMap<ValueProperties, Integer>();

  /**
   * -> references rsk_value_specification(id)
   */
  private final Map<ValueProperties, Integer> _valueSpecification2Id = new HashMap<ValueProperties, Integer>();


  /**
   * -> references rsk_function_unique_id(id)
   */
  private final Map<String, Integer> _functionUniqueId2Id = new HashMap<String, Integer>();
  
  /**
   * -> references rsk_compute_node(id)
   */
  private final Map<String, Integer> _computeNodeId2Id = 
    Collections.synchronizedMap(new HashMap<String, Integer>());
  
  /**
   * Key is {@link StatusEntry} {_calculationConfigurationId, _computationTargetId}.
   * 
   * null value is possible, it means no status entry in DB.
   */
  private final Map<Pair<Integer, Integer>, StatusEntry> _searchKey2StatusEntry = 
    Collections.synchronizedMap(new HashMap<Pair<Integer, Integer>, StatusEntry>());
  
  /**
   * We cache compute failures for performance, so that we 
   * don't always need to hit the database to find out the primary key
   * of a compute failure.
   */
  private final Map<ComputeFailureKey, ComputeFailure> _key2ComputeFailure = 
    Collections.synchronizedMap(new HashMap<ComputeFailureKey, ComputeFailure>());
  
  /**
   * Used to write non-Double results into database
   */
  private final ResultConverterCache _resultConverterCache;
  
  // Variables set in initialize()
  
  /**
   * We use Hibernate to generate unique IDs
   */
  private SequenceStyleGenerator _idGenerator;
  
  /**
   * We use Hibernate to generate unique IDs
   */
  private SessionImplementor _session;
  
  /**
   * Have DB connections been set up successfully?
   */
  private boolean _initialized; // = false;

  public AbstractBatchResultWriter(DbConnector dbConnector,
      RiskRun riskRun,
      ResultConverterCache resultConverterCache,
      Collection<ComputationTarget> computationTargets,
      Set<RiskValueName> valueNames,
      Set<RiskValueRequirement> valueRequirements,
      Set<RiskValueSpecification> valueSpecifications) {
    
    ArgumentChecker.notNull(dbConnector, "dbConnector");
    ArgumentChecker.notNull(computationTargets, "Computation targets");
    ArgumentChecker.notNull(riskRun, "Risk run");
    ArgumentChecker.notNull(valueNames, "Value names");
    ArgumentChecker.notNull(valueRequirements, "Value requirements");
    ArgumentChecker.notNull(valueSpecifications, "Value specifications");
    ArgumentChecker.notNull(resultConverterCache, "resultConverterCache");
    
    _dbConnector = dbConnector;
    _resultConverterCache = resultConverterCache;
    _riskRunId = riskRun.getId();
    
    for (CalculationConfiguration cc : riskRun.getCalculationConfigurations()) {
      int id = cc.getId();
      if (id == -1) {
        throw new IllegalArgumentException(cc + " is not initialized");
      }
      _calculationConfiguration2Id.put(cc.getName(), id);
    }

    for (ComputationTarget target : computationTargets) {
      int id = target.getId();
      if (id == -1) {
        throw new IllegalArgumentException(target + " is not initialized");
      }
      _computationTarget2Id.put(target.toComputationTargetSpec(), id);      
    }


    for (RiskValueName valueName : valueNames) {
      _riskValueName2Id.put(valueName.getName(), valueName.getId());
    }

    for (RiskValueRequirement valueRequirement : valueRequirements) {
      _valueRequirement2Id.put(valueRequirement.toProperties(), valueRequirement.getId());
    }

    for (RiskValueSpecification valueSpecification : valueSpecifications) {
      _valueSpecification2Id.put(valueSpecification.toProperties(), valueSpecification.getId());
    }
  }

  // --------------------------------------------------------------------------
  
  public void initialize() {
    SessionFactoryImplementor implementor = (SessionFactoryImplementor) getSessionFactory();
    IdentifierGenerator idGenerator = implementor.getIdentifierGenerator(RiskValue.class.getName());
    if (idGenerator == null || !(idGenerator instanceof SequenceStyleGenerator)) {
      throw new IllegalArgumentException("The given SessionFactory must contain a RiskValue mapping with a SequenceStyleGenerator");      
    }

    _idGenerator = (SequenceStyleGenerator) idGenerator;
    
    _initialized = true;
  }
  
  public boolean isInitialized() {
    return _initialized;
  }
  
  public void ensureInitialized() {
    if (!isInitialized()) {
      throw new IllegalStateException("Db context has not been initialized yet");
    }
  }
  
  // --------------------------------------------------------------------------
  
  public void openSession() {
    _session = (SessionImplementor) getSessionFactory().openStatelessSession();
  }
  
  public void joinSession() {
    _session = (SessionImplementor) getSessionFactory().getCurrentSession();
  }
  
  /**
   * This will close the session only if the caller previously created a new session with openSession.
   * joinSession will leave the session open.
   */
  public void closeSession() {
    if (_session instanceof StatelessSession) {
      ((StatelessSession) _session).close();
    }
    _session = null;
  }
  
  // --------------------------------------------------------------------------
  
  public SessionFactory getSessionFactory() {
    return _dbConnector.getHibernateSessionFactory();
  }

  public SimpleJdbcTemplate getJdbcTemplate() {
    return _dbConnector.getJdbcTemplate();
  }

  // --------------------------------------------------------------------------

  public long generateUniqueId() {
    Serializable generatedId = _idGenerator.generate(_session, null);
    if (!(generatedId instanceof Long)) {
      throw new IllegalStateException("Got ID of type " + generatedId.getClass());
    }
    return (Long) generatedId;
  }
  
  // --------------------------------------------------------------------------

  public int getCalculationConfigurationId(String calcConfName) {
    ArgumentChecker.notNull(calcConfName, "Calc conf name");
    
    Integer confId = _calculationConfiguration2Id.get(calcConfName);
    if (confId == null) {
      throw new IllegalArgumentException("Calculation configuration " + calcConfName + " is not in the database");
    }
    return confId;
  }

  public int getComputationTargetId(ComputationTargetSpecification spec) {
    ArgumentChecker.notNull(spec, "Computation target");
    
    Integer specId = _computationTarget2Id.get(spec);
    if (specId == null) {
      throw new IllegalArgumentException(spec + " is not in the database");
    }
    return specId;
  }

  public Integer getComputeNodeId(String nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");
    
    Integer dbId = _computeNodeId2Id.get(nodeId);
    if (dbId != null) {
      return dbId;
    }

    DbBatchMaster dbManager = new DbBatchMaster(_dbConnector);
    
    // try twice to handle situation where two threads contend to insert
    RuntimeException lastException = null;
    for (int i = 0; i < 2; i++) {
      try {
        dbId = dbManager.getComputeNode(nodeId).getId();
        lastException = null;
        break;
      } catch (RuntimeException e) {
        lastException = e;
      }
    }
    if (lastException != null) {
      throw lastException;
    }
    _computeNodeId2Id.put(nodeId, dbId);
    return dbId;
  }

  public int getValueNameId(String name) {
    ArgumentChecker.notNull(name, "Risk value name");

    Integer dbId = _riskValueName2Id.get(name);
    if (dbId != null) {
      return dbId;
    }

    DbBatchMaster dbManager = new DbBatchMaster(_dbConnector);
    // try twice to handle situation where two threads contend to insert
    RuntimeException lastException = null;
    for (int i = 0; i < 2; i++) {
      try {
        dbId = dbManager.getRiskValueName(name).getId();
        lastException = null;
        break;
      } catch (RuntimeException e) {
        lastException = e;
      }
    }
    if (lastException != null) {
      throw lastException;
    }
    _riskValueName2Id.put(name, dbId);
    return dbId;
  }

  public int getValueRequirementId(ValueProperties requirement) {
    ArgumentChecker.notNull(requirement, "Risk value requirement");
    
    Integer dbId = _valueRequirement2Id.get(requirement);
    if (dbId != null) {
      return dbId;
    }

    DbBatchMaster dbManager = new DbBatchMaster(_dbConnector);
    
    // try twice to handle situation where two threads contend to insert
    RuntimeException lastException = null;
    for (int i = 0; i < 2; i++) {
      try {
        dbId = dbManager.getRiskValueRequirement(requirement).getId();
        lastException = null;
        break;
      } catch (RuntimeException e) {
        lastException = e;
      }
    }
    if (lastException != null) {
      throw lastException;
    }
    _valueRequirement2Id.put(requirement, dbId);
    return dbId;
  }

  public int getValueSpecificationId(ValueProperties specification) {
    ArgumentChecker.notNull(specification, "Risk value specification");

    Integer dbId = _valueSpecification2Id.get(specification);
    if (dbId != null) {
      return dbId;
    }

    DbBatchMaster dbManager = new DbBatchMaster(_dbConnector);

    // try twice to handle situation where two threads contend to insert
    RuntimeException lastException = null;
    for (int i = 0; i < 2; i++) {
      try {
        dbId = dbManager.getRiskValueSpecification(specification).getId();
        lastException = null;
        break;
      } catch (RuntimeException e) {
        lastException = e;
      }
    }
    if (lastException != null) {
      throw lastException;
    }
    _valueSpecification2Id.put(specification, dbId);
    return dbId;
  }
  
  public int getFunctionUniqueId(String uniqueId) {
    ArgumentChecker.notNull(uniqueId, "Function unique ID");
    
    Integer dbId = _functionUniqueId2Id.get(uniqueId);
    if (dbId != null) {
      return dbId;
    }

    DbBatchMaster dbManager = new DbBatchMaster(_dbConnector);
    
    // try twice to handle situation where two threads contend to insert
    RuntimeException lastException = null;
    for (int i = 0; i < 2; i++) {
      try {
        dbId = dbManager.getFunctionUniqueId(uniqueId).getId();
        lastException = null;
        break;
      } catch (RuntimeException e) {
        lastException = e;
      }
    }
    if (lastException != null) {
      throw lastException;
    }
    _functionUniqueId2Id.put(uniqueId, dbId);
    return dbId;
  }

  public Integer getRiskRunId() {
    return _riskRunId;
  }
  
  // --------------------------------------------------------------------------
  
  public Map<ComputationTargetSpecification, Integer> getComputationTarget2Id() {
    return _computationTarget2Id;
  }

  public Map<String, Integer> getCalculationConfiguration2Id() {
    return _calculationConfiguration2Id;
  }

  public Map<String, Integer> getRiskValueName2Id() {
    return _riskValueName2Id;
  }

  public Map<ValueProperties, Integer> getValueRequirement2Id() {
    return _valueRequirement2Id;
  }

  public ResultConverterCache getResultConverterCache() {
    return _resultConverterCache;
  }
  
  //--------------------------------------------------------------------------
  
  public ComputeFailure getComputeFailureFromDb(ComputeFailureKey computeFailureKey) {
    ComputeFailure computeFailure = _key2ComputeFailure.get(computeFailureKey);
    if (computeFailure != null) {
      return computeFailure;
    }
    
    try {
      computeFailure = saveComputeFailure(computeFailureKey);
      return computeFailure;
      
    } catch (DataAccessException e) {
      // maybe the row was already there
      s_logger.debug("Failed to save compute failure", e);
    }
    
    try {
      int id = getJdbcTemplate().queryForInt(ComputeFailure.sqlGet(), computeFailureKey.toSqlParameterSource());
      
      computeFailure = new ComputeFailure();
      computeFailure.setId(id);
      computeFailure.setFunctionId(computeFailureKey.getFunctionId());
      computeFailure.setExceptionClass(computeFailureKey.getExceptionClass());
      computeFailure.setExceptionMsg(computeFailureKey.getExceptionMsg());
      computeFailure.setStackTrace(computeFailureKey.getStackTrace());

      _key2ComputeFailure.put(computeFailureKey, computeFailure);
      return computeFailure;

    } catch (IncorrectResultSizeDataAccessException e) {
      s_logger.error("Cannot get {} from db", computeFailureKey);
      throw new RuntimeException("Cannot get " + computeFailureKey + " from db", e);
    }
  }

  public ComputeFailure saveComputeFailure(ComputeFailureKey computeFailureKey) {
    ComputeFailure computeFailure;
    computeFailure = new ComputeFailure();
    computeFailure.setId(generateUniqueId());
    computeFailure.setFunctionId(computeFailureKey.getFunctionId());
    computeFailure.setExceptionClass(computeFailureKey.getExceptionClass());
    computeFailure.setExceptionMsg(computeFailureKey.getExceptionMsg());
    computeFailure.setStackTrace(computeFailureKey.getStackTrace());
    
    int rowCount = getJdbcTemplate().update(ComputeFailure.sqlInsert(), computeFailure.toSqlParameterSource());
    if (rowCount == 1) {
      _key2ComputeFailure.put(computeFailureKey, computeFailure);
      return computeFailure;
    }
    return computeFailure;
  }
  
  // --------------------------------------------------------------------------
  
  public void insertRows(String rowType, String sql, List<SqlParameterSource> rows) {
    if (rows.isEmpty()) {
      s_logger.info("No {} rows to insert", rowType);
      return;
    }
    
    s_logger.info("Inserting {} {} rows into DB", rows.size(), rowType);
    
    SqlParameterSource[] batchArgsArray = rows.toArray(new SqlParameterSource[0]);

    int[] counts = getJdbcTemplate().batchUpdate(sql, batchArgsArray);

    checkCount(rowType, batchArgsArray, counts);
    s_logger.info("Inserted {} {} rows into DB", rows.size(), rowType);
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
  
  // --------------------------------------------------------------------------
  
  public void upsertStatusEntries(
      CalculationJobSpecification job,
      StatusEntry.Status status, 
      Set<ComputationTargetSpecification> targets) {
    
    Integer calcConfId = getCalculationConfigurationId(job.getCalcConfigName());
    
    List<SqlParameterSource> inserts = new ArrayList<SqlParameterSource>();
    List<SqlParameterSource> updates = new ArrayList<SqlParameterSource>();
    
    for (ComputationTargetSpecification target : targets) {
      Integer computationTargetId = getComputationTargetId(target);
      
      MapSqlParameterSource params = new MapSqlParameterSource();
      
      // this assumes that _searchKey2StatusEntry has already been populated
      // in getStatus()
      Pair<Integer, Integer> key = Pair.of(calcConfId, computationTargetId);
      StatusEntry statusEntry = _searchKey2StatusEntry.get(key);
      if (statusEntry != null) {
        statusEntry.setStatus(status);
        params.addValue("id", statusEntry.getId());        
        params.addValue("status", statusEntry.getStatus().ordinal());
        updates.add(params);
      } else {
        long uniqueId = generateUniqueId();
        statusEntry = new StatusEntry();
        statusEntry.setId(uniqueId);
        statusEntry.setStatus(status);
        statusEntry.setCalculationConfigurationId(calcConfId);
        statusEntry.setComputationTargetId(computationTargetId);
        _searchKey2StatusEntry.put(key, statusEntry);
        
        params.addValue("id", uniqueId);        
        params.addValue("calculation_configuration_id", calcConfId);
        params.addValue("computation_target_id", computationTargetId);
        params.addValue("status", statusEntry.getStatus().ordinal());
        inserts.add(params);
      }
    }
    
    s_logger.info("Inserting {} and updating {} {} status entries", 
        new Object[] {inserts.size(), updates.size(), status});
    
    SqlParameterSource[] batchArgsArray = inserts.toArray(new SqlParameterSource[0]);
    int[] counts = getJdbcTemplate().batchUpdate(StatusEntry.sqlInsert(), batchArgsArray);
    checkCount(status + " insert", batchArgsArray, counts);
    
    batchArgsArray = updates.toArray(new SqlParameterSource[0]);
    counts = getJdbcTemplate().batchUpdate(StatusEntry.sqlUpdate(), batchArgsArray);
    checkCount(status + " update", batchArgsArray, counts);
    
    s_logger.info("Inserted {} and updated {} {} status entries", 
        new Object[] {inserts.size(), updates.size(), status});
  }
  
  public StatusEntry.Status getStatus(String calcConfName, ComputationTargetSpecification ct) {
    Integer calcConfId = getCalculationConfigurationId(calcConfName);
    Integer computationTargetId = getComputationTargetId(ct);
    
    // first check to see if this status has already been queried for
    // and if the answer could therefore be found in the cache
    
    Pair<Integer, Integer> key = Pair.of(calcConfId, computationTargetId);
    if (_searchKey2StatusEntry.containsKey(key)) {
      StatusEntry existingStatusEntryInDb = _searchKey2StatusEntry.get(key);
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
          StatusEntry.sqlGet(),
          StatusEntry.ROW_MAPPER,
          args);

      // status entry in db found.
      _searchKey2StatusEntry.put(key, statusEntry);
      
      return statusEntry.getStatus();

    } catch (IncorrectResultSizeDataAccessException e) {
      // no status entry in the db. 
      _searchKey2StatusEntry.put(key, null);
      return StatusEntry.Status.NOT_RUNNING;
    }
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * Useful in tests
   * @return Number of successful risk values in the database
   */
  public int getNumRiskRows() {
    return getJdbcTemplate().queryForInt(RiskValue.sqlCount());
  }
  
  /**
   * Useful in tests
   * @return Number of risk failures in the database
   */
  public int getNumRiskFailureRows() {
    return getJdbcTemplate().queryForInt(RiskFailure.sqlCount());
  }
  
  /**
   * Useful in tests
   * @return Number of risk failure reasons in the database
   */
  public int getNumRiskFailureReasonRows() {
    return getJdbcTemplate().queryForInt(FailureReason.sqlCount());
  }
  
  /**
   * Useful in tests
   * @return Number of risk compute failures in the database
   */
  public int getNumRiskComputeFailureRows() {
    return getJdbcTemplate().queryForInt(ComputeFailure.sqlCount());
  }
  
  /**
   * Useful in tests. Assumes there is only one value for the
   * given computation target with the given name
   * (i.e., that no two functions produce the same value).
   * 
   * @param calcConfName  the calculation config name
   * @param valueName  the value name
   * @param specification  the specification
   * @param ct  the computation target
   * @param requirement the requirement
   * @return the value for this target, null if does not exist
   */
  public RiskValue getValue(String calcConfName, String valueName, ValueProperties requirement,
      ValueProperties specification, ComputationTargetSpecification ct) {
    Integer calcConfId = getCalculationConfigurationId(calcConfName);
    Integer valueId = getValueNameId(valueName);
    Integer computationTargetId = getComputationTargetId(ct);
    Integer valueRequirementId = getValueRequirementId(requirement);
    Integer valueSpecificationId = getValueSpecificationId(specification);
    
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("calculation_configuration_id", calcConfId);
    params.addValue("value_name_id", valueId);
    params.addValue("computation_target_id", computationTargetId);
    params.addValue("value_requirement_id", valueRequirementId);
    params.addValue("value_specification_id", valueSpecificationId);
    
    try {
      return getJdbcTemplate().queryForObject(RiskValue.sqlGet(), RiskValue.ROW_MAPPER, params);
    } catch (IncorrectResultSizeDataAccessException e) {
      return null;
    }
  }

}
