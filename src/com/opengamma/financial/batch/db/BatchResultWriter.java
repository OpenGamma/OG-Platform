/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.fudgemsg.mapping.FudgeTransient;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.impl.StatelessSessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.CalculationNode;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.engine.view.calcnode.MissingInput;
import com.opengamma.engine.view.calcnode.MissingInputException;
import com.opengamma.engine.view.calcnode.ResultWriter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.DBTool;
import com.opengamma.util.tuple.Pair;

/**
 * Writes risk into the OpenGamma batch risk database.
 * <p>
 * This result writer MUST be configured together with a dependency graph executor
 * that partitions the dependency graph by computation target and sends all
 * nodes related to a single target down to the grid in a single batch.
 * <p> 
 * For the database structure and tables, see create-db-batch.sql.   
 */
public class BatchResultWriter implements ResultWriter {
  
  private static final Logger s_logger = LoggerFactory.getLogger(BatchResultWriter.class);
  
  // Variables YOU must set before calling initialize()
  
  /**
   * E.g., jdbc:postgresql://postgresql.foo.com
   */
  private String _jdbcUrl;
  
  /**
   * Database username
   */
  private String _username;
  
  /**
   * Database password
   */
  private String _password;
  
  /**
   * References rsk_run(id)
   */
  private Integer _riskRunId;
 
  /**
   * Used to determine whether it's worth checking the status
   * table for already-executed entries. If this is the first
   * time we're running the batch, there won't be anything in 
   * the status table, so it's not necessary to make queries
   * against it.
   */
  private boolean _isRestart;
  
  /**
   * -> references rsk_computation_target(id)   
   */
  private Map<ComputationTargetSpecification, Integer> _computationTarget2Id;
  
  /**
   * -> references rsk_calculation_configuration(id)
   */
  private Map<String, Integer> _calculationConfiguration2Id;
  
  /**
   * -> references rsk_value_name(id)
   */
  private Map<String, Integer> _riskValueName2Id;
  
  /**
   * References rsk_compute_node(id)
   */
  private Integer _computeNodeId;
  
  /**
   * Key is {@link StatusEntry} {_calculationConfigurationId, _computationTargetId}.
   * 
   * null value is possible, it means no status entry in DB.
   */
  private Map<Pair<Integer, Integer>, StatusEntry> _searchKey2StatusEntry;
  
  /**
   * We cache compute failures for performance, so that we 
   * don't always need to hit the database to find out the primary key
   * of a compute failure.
   */
  private Map<ComputeFailureKey, ComputeFailure> _key2ComputeFailure;
  
  /**
   * It is possible to disable writing errors into
   * 
   * rsk_compute_failure  
   * rsk_failure 
   * rsk_failure_reason
   * 
   * by setting this to false.
   * 
   */
  private boolean _writeErrors = true;
  
  // Variables set in initialize()
  
  /**
   * Writing into the risk tables and into the status table must happen
   * in a single transaction for consistency reasons. Therefore we need
   * a transaction manager.
   */
  private transient DataSourceTransactionManager _transactionManager;
  
  /**
   * Executes batch inserts into the risk tables
   */
  private transient SimpleJdbcTemplate _jdbcTemplate;

  /**
   * We use Hibernate to generate unique IDs
   */
  private transient SessionFactory _sessionFactory;
  
  /**
   * We use Hibernate to generate unique IDs
   */
  private transient SequenceStyleGenerator _idGenerator;
  
  /**
   * We use Hibernate to generate unique IDs
   */
  private transient StatelessSessionImpl _session;
  
  /**
   * Have DB connections been set up successfully?
   */
  private transient boolean _initialized; // = false;
  
  public void initialize(CalculationNode computeNode) {
    DBTool tool = new DBTool();
    tool.setJdbcUrl(_jdbcUrl);
    tool.setUser(_username);
    tool.setPassword(_password);
    tool.initialize();
    
    initialize(computeNode, tool);
  }
  
  void initialize(CalculationNode computeNode, DBTool tool) {
    ArgumentChecker.notNull(computeNode, "Compute node");
    ArgumentChecker.notNull(computeNode, "DB tool");
    
    if (_riskRunId == null ||
        _computationTarget2Id == null ||
        _calculationConfiguration2Id == null ||
        _riskValueName2Id == null ||
        _jdbcUrl == null ||
        _username == null ||
        _password == null) {
      throw new IllegalStateException("Not all required arguments are set");
    }
    
    if (_sessionFactory == null) {
      Configuration configuration = tool.getHibernateConfiguration();
      for (Class<?> clazz : BatchDbManagerImpl.getHibernateMappingClasses()) {
        configuration.addClass(clazz);
      }
  
      SessionFactory sessionFactory = configuration.buildSessionFactory();
      setSessionFactory(sessionFactory);
    }
    
    if (_transactionManager == null) {
      setTransactionManager(tool.getTransactionManager());
    }
    
    if (_computeNodeId == null) {
      BatchDbManagerImpl dbManager = new BatchDbManagerImpl();
      dbManager.setSessionFactory(getSessionFactory());
      try {
        getSessionFactory().getCurrentSession().beginTransaction();
        _computeNodeId = dbManager.getComputeNode(computeNode.getNodeId()).getId();
        getSessionFactory().getCurrentSession().getTransaction().commit();
      } catch (RuntimeException e) {
        getSessionFactory().getCurrentSession().getTransaction().rollback();
        throw e;
      }
    }
    
    SessionFactoryImplementor implementor = (SessionFactoryImplementor) _sessionFactory;
    IdentifierGenerator idGenerator = implementor.getIdentifierGenerator(RiskValue.class.getName());
    if (idGenerator == null || !(idGenerator instanceof SequenceStyleGenerator)) {
      throw new IllegalArgumentException("The given SessionFactory must contain a RiskValue mapping with a SequenceStyleGenerator");      
    }

    _idGenerator = (SequenceStyleGenerator) idGenerator;
    
    _searchKey2StatusEntry = new HashMap<Pair<Integer, Integer>, StatusEntry>();
    _key2ComputeFailure = new HashMap<ComputeFailureKey, ComputeFailure>();
    
    _initialized = true;
  }
  
  public boolean isInitialized() {
    return _initialized;
  }
  
  /*package*/ void openSession() {
    _session = (StatelessSessionImpl) _sessionFactory.openStatelessSession();
  }
  
  /*package*/ void closeSession() {
    _session.close();
    _session = null;
  }
  
  public void setSessionFactory(SessionFactory sessionFactory) {
    ArgumentChecker.notNull(sessionFactory, "Session factory");
    if (_sessionFactory != null) {
      throw new IllegalStateException("Already set");
    }
    
    _sessionFactory = sessionFactory;
  }
  
  @FudgeTransient
  public SessionFactory getSessionFactory() {
    return _sessionFactory;
  }

  @FudgeTransient
  public DataSourceTransactionManager getTransactionManager() {
    return _transactionManager;
  }

  public void setTransactionManager(DataSourceTransactionManager transactionManager) {
    ArgumentChecker.notNull(transactionManager, "Transaction manager");
    
    _transactionManager = transactionManager;
    DataSource dataSource = _transactionManager.getDataSource();
    _jdbcTemplate = new SimpleJdbcTemplate(dataSource);
  }

  public String getJdbcUrl() {
    return _jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl) {
    ArgumentChecker.notNull(jdbcUrl, "jdbcUrl");
    _jdbcUrl = jdbcUrl;
  }

  public String getUsername() {
    return _username;
  }

  public void setUsername(String username) {
    ArgumentChecker.notNull(username, "username");
    _username = username;
  }

  public String getPassword() {
    return _password;
  }

  public void setPassword(String password) {
    ArgumentChecker.notNull(password, "password");
    _password = password;
  }
  
  public boolean isWriteErrors() {
    return _writeErrors;
  }

  public void setWriteErrors(boolean writeErrors) {
    _writeErrors = writeErrors;
  }

  public void setRiskRun(RiskRun riskRun) {
    ArgumentChecker.notNull(riskRun, "Risk run");
    if (_riskRunId != null) {
      throw new IllegalStateException("Already set");
    }
    _riskRunId = riskRun.getId();
    
    _calculationConfiguration2Id = new HashMap<String, Integer>();
    for (CalculationConfiguration cc : riskRun.getCalculationConfigurations()) {
      int id = cc.getId();
      if (id == -1) {
        throw new IllegalArgumentException(cc + " is not initialized");
      }
      _calculationConfiguration2Id.put(cc.getName(), id);
    }
    
    setIsRestart(riskRun.isRestart());
  }

  public void setComputationTargets(Set<ComputationTarget> computationTargets) {
    ArgumentChecker.notNull(computationTargets, "Computation targets");
    if (_computationTarget2Id != null) {
      throw new IllegalStateException("Already set");
    }
    
    _computationTarget2Id = new HashMap<ComputationTargetSpecification, Integer>();
    for (ComputationTarget target : computationTargets) {
      int id = target.getId();
      if (id == -1) {
        throw new IllegalArgumentException(target + " is not initialized");
      }
      _computationTarget2Id.put(target.toSpec(), id);      
    }
  }

  public void setRiskValueNames(Set<RiskValueName> valueNames) {
    ArgumentChecker.notNull(valueNames, "Value names");
    if (_riskValueName2Id != null) {
      throw new IllegalStateException("Already set");
    }
    
    _riskValueName2Id = new HashMap<String, Integer>();
    for (RiskValueName valueName : valueNames) {
      _riskValueName2Id.put(valueName.getName(), valueName.getId());
    }
  }

  public void setComputeNode(ComputeNode computeNode) {
    ArgumentChecker.notNull(computeNode, "Compute node");
    if (_computeNodeId  != null) {
      throw new IllegalStateException("Already set");
    }
    
    _computeNodeId = computeNode.getId();
  }

  public void ensureInitialized() {
    if (!isInitialized()) {
      throw new IllegalStateException("Db context has not been initialized yet");
    }
  }

  public long generateUniqueId() {
    Serializable generatedId = _idGenerator.generate(_session, null);
    if (!(generatedId instanceof Long)) {
      throw new IllegalStateException("Got ID of type " + generatedId.getClass());
    }
    return ((Long) generatedId).longValue();
  }

  public int getCalculationConfigurationId(String calcConfName) {
    ArgumentChecker.notNull(calcConfName, "Calc conf name");
    
    Number confId = _calculationConfiguration2Id.get(calcConfName);
    if (confId == null) {
      throw new IllegalArgumentException("Calculation configuration " + calcConfName + " is not in the database");
    }
    return confId.intValue();
  }

  public int getComputationTargetId(ComputationTargetSpecification spec) {
    ArgumentChecker.notNull(spec, "Computation target");
    
    Integer specId = _computationTarget2Id.get(spec);
    if (specId == null) {
      throw new IllegalArgumentException(spec + " is not in the database");
    }
    return specId.intValue();
  }

  public Integer getComputeNodeId() {
    return _computeNodeId;
  }

  public Integer getRiskRunId() {
    return _riskRunId;
  }
  
  public boolean isRestart() {
    return _isRestart;
  }

  public void setIsRestart(boolean isRestart) {
    _isRestart = isRestart;
  }
  
  // --------------------------------------------------------------------------
  
  public Map<ComputationTargetSpecification, Integer> getComputationTarget2Id() {
    return _computationTarget2Id;
  }

  public void setComputationTarget2Id(Map<ComputationTargetSpecification, Integer> computationTarget2Id) {
    ArgumentChecker.notNull(computationTarget2Id, "Computation target -> database primary key map");
    _computationTarget2Id = computationTarget2Id;
  }

  public Map<String, Integer> getCalculationConfiguration2Id() {
    return _calculationConfiguration2Id;
  }

  public void setCalculationConfiguration2Id(Map<String, Integer> calculationConfiguration2Id) {
    ArgumentChecker.notNull(calculationConfiguration2Id, "Calculation configuration name -> database primary key map");
    _calculationConfiguration2Id = calculationConfiguration2Id;
  }

  public Map<String, Integer> getRiskValueName2Id() {
    return _riskValueName2Id;
  }

  public void setRiskValueName2Id(Map<String, Integer> riskValueName2Id) {
    ArgumentChecker.notNull(riskValueName2Id, "Risk value name -> database primary key map");
    _riskValueName2Id = riskValueName2Id;
  }

  public void setRiskRunId(Integer riskRunId) {
    _riskRunId = riskRunId;
  }

  public void setComputeNodeId(Integer computeNodeId) {
    ArgumentChecker.notNull(computeNodeId, "Compute node database primary key");
    _computeNodeId = computeNodeId;
  }
  
  // --------------------------------------------------------------------------

  public int getValueNameId(String name) {
    ArgumentChecker.notNull(name, "Risk value name");
    
    Number valueNameId = _riskValueName2Id.get(name);
    if (valueNameId == null) {
      throw new IllegalArgumentException("Value name " + name + " is not in the database");
    }
    return valueNameId.intValue();
  }

  @Override
  public void write(CalculationNode node, CalculationJobResult result) {
    ArgumentChecker.notNull(node, "Calculation node the writing happens on");
    ArgumentChecker.notNull(result, "The result to write");
    
    if (result.getResultItems().isEmpty()) {
      s_logger.info("{}: Nothing to insert into DB", result);
      return;
    }
    
    synchronized (this) {
      if (!isInitialized()) {
        initialize(node);
      }
    }
    
    openSession();
    try {
      doWrite(node, result);
    } finally {
      closeSession();
    }
  }
  
  private void doWrite(CalculationNode node, CalculationJobResult result) {
    
    Set<ComputationTargetSpecification> successfulTargets = new HashSet<ComputationTargetSpecification>();
    Set<ComputationTargetSpecification> failedTargets = new HashSet<ComputationTargetSpecification>();
    
    for (CalculationJobResultItem item : result.getResultItems()) {
      ComputationTargetSpecification target = item.getComputationTargetSpecification();
      
      boolean success; 
      
      if (item.getResult() == InvocationResult.SUCCESS) {
        success = !failedTargets.contains(target);
        
        if (success) {
          // also check output types
          for (ComputedValue value : item.getResults()) {
            if (!(value.getValue() instanceof Double)) {
              s_logger.error("Can only insert Double values, got " + 
                  value.getValue().getClass() + " for " + item);
              success = false;
              break;
            }
          }
        }
      } else {
        success = false;
      }
      
      if (success) {
        successfulTargets.add(target);        
      } else {
        successfulTargets.remove(target);
        failedTargets.add(target);
      }
    }
    
    List<SqlParameterSource> successes = new ArrayList<SqlParameterSource>();
    List<SqlParameterSource> failures = new ArrayList<SqlParameterSource>();
    List<SqlParameterSource> failureReasons = new ArrayList<SqlParameterSource>();
    
    int riskRunId = getRiskRunId();
    int calcConfId = getCalculationConfigurationId(result.getSpecification().getCalcConfigName());
    int computeNodeId = getComputeNodeId();
    
    Date evalInstant = new Date();
    
    for (CalculationJobResultItem item : result.getResultItems()) {
      if (!item.getItem().isWriteResults()) {
        continue;
      }
      
      if (successfulTargets.contains(item.getComputationTargetSpecification())) {
      
        for (ComputedValue value : item.getResults()) {
          Double valueAsDouble = (Double) value.getValue(); // output type was already checked above
  
          int valueNameId = getValueNameId(value.getSpecification().getRequirementSpecification().getValueName());
          int computationTargetId = getComputationTargetId(value.getSpecification().getRequirementSpecification().getTargetSpecification());
          
          RiskValue riskValue = new RiskValue();
          riskValue.setId(generateUniqueId());
          riskValue.setCalculationConfigurationId(calcConfId);
          riskValue.setValueNameId(valueNameId);
          riskValue.setComputationTargetId(computationTargetId);
          riskValue.setRunId(riskRunId);
          riskValue.setValue(valueAsDouble);
          riskValue.setEvalInstant(evalInstant);
          riskValue.setComputeNodeId(computeNodeId);
          successes.add(riskValue.toSqlParameterSource());
        }
        
      // the check below ensures that
      // if there is a partial failure (some successes, some failures) for a target, 
      // only the failures will be written out in the database
      } else if (failedTargets.contains(item.getComputationTargetSpecification()))  {
          
        if (!isWriteErrors()) {
          continue;
        }
        
        for (ValueRequirement value : item.getItem().getDesiredValues()) {
          
          int valueNameId = getValueNameId(value.getValueName());
          int computationTargetId = getComputationTargetId(value.getTargetSpecification());
        
          ViewComputationCache cache = node.getCache(result.getSpecification());
          BatchResultWriterFailure cachedFailure = new BatchResultWriterFailure();
          
          RiskFailure failure = new RiskFailure();
          failure.setId(generateUniqueId());
          failure.setCalculationConfigurationId(calcConfId);
          failure.setValueNameId(valueNameId);
          failure.setComputationTargetId(computationTargetId);
          failure.setRunId(riskRunId);
          failure.setEvalInstant(evalInstant);
          failure.setComputeNodeId(computeNodeId);
          failures.add(failure.toSqlParameterSource());
          
          if (item.getResult() != InvocationResult.SUCCESS) {
            Exception exception = item.getException();

            if (exception instanceof MissingInputException) {
              // There may be 1-N failure reasons - one for each failed
              // function in the subtree below this node. (This
              // only includes "original", i.e., lowest-level, failures.)
              
              for (ValueSpecification missingInput : ((MissingInputException) exception).getMissingInputs()) {
                BatchResultWriterFailure inputFailure = (BatchResultWriterFailure) cache.getValue(missingInput);
                if (inputFailure == null) {
                  s_logger.warn("No failure information available for {}", missingInput);
                  continue;
                }
                
                cachedFailure.addComputeFailureIds(inputFailure.getComputeFailureIds());
              }
              
              for (Long computeFailureId : cachedFailure.getComputeFailureIds()) {
                FailureReason reason = new FailureReason();
                reason.setId(generateUniqueId());
                reason.setRiskFailure(failure);
                reason.setComputeFailureId(computeFailureId);
                failureReasons.add(reason.toSqlParameterSource());
              }
              
            } else {
              // an "original" failure
              //
              // There will only be 1 failure reason.
              
              ComputeFailureKey computeFailureKey = new ComputeFailureKey(
                  item.getItem().getFunctionUniqueIdentifier(),
                  exception.getClass().getName(),
                  exception.getMessage(),
                  exception.getStackTrace());
              
              ComputeFailure computeFailure = getComputeFailureFromDb(computeFailureKey);
              cachedFailure.addComputeFailureIds(Collections.singleton(computeFailure.getId()));
              
              FailureReason reason = new FailureReason();
              reason.setId(generateUniqueId());
              reason.setRiskFailure(failure);
              reason.setComputeFailureId(computeFailure.getId());
              failureReasons.add(reason.toSqlParameterSource());
            }
            
            // failures are propagated up from children via the computation cache
            for (ValueSpecification output : item.getOutputs()) {
              cache.putValue(new ComputedValue(output, cachedFailure));
            }
          } else {
            // partial failure for this target / unsupported (non-Double) output from a function
            s_logger.debug("Not writing any failure reasons for partial failures / unsupported outputs for now");
          }
        }
      }
    }
    
    TransactionStatus transaction = _transactionManager.getTransaction(new DefaultTransactionDefinition());
    try {
      
      insertRows("risk", RiskValue.sqlInsertRisk(), successes);
      insertRows("risk failure", RiskFailure.sqlInsertRiskFailure(), failures);
      insertRows("risk failure reason", FailureReason.sqlInsertRiskFailureReason(), failureReasons);
      
      upsertStatusEntries(result.getSpecification(), StatusEntry.Status.SUCCESS, successfulTargets);
      upsertStatusEntries(result.getSpecification(), StatusEntry.Status.FAILURE, failedTargets);
      
      _transactionManager.commit(transaction);
    } catch (RuntimeException e) {
      _transactionManager.rollback(transaction);
      throw e;
    }
  }

  private ComputeFailure getComputeFailureFromDb(ComputeFailureKey computeFailureKey) {
    ComputeFailure computeFailure = _key2ComputeFailure.get(computeFailureKey);
    if (computeFailure != null) {
      return computeFailure;
    }
    
    try {
      computeFailure = saveComputeFailure(computeFailureKey);
      return computeFailure;
      
    } catch (DataAccessException e) {
      // maybe the row was already there
    }
    
    try {
      int id = _jdbcTemplate.queryForInt(ComputeFailure.sqlGet(), computeFailure.toSqlParameterSource());
      
      computeFailure = new ComputeFailure();
      computeFailure.setId(id);
      computeFailure.setFunctionId(computeFailureKey.getFunctionId());
      computeFailure.setExceptionClass(computeFailureKey.getExceptionClass());
      computeFailure.setExceptionMsg(computeFailureKey.getExceptionMsg());
      computeFailure.setStackTrace(computeFailureKey.getStackTrace());

      _key2ComputeFailure.put(computeFailureKey, computeFailure);
      return computeFailure;

    } catch (IncorrectResultSizeDataAccessException e) {
      s_logger.error("Cannot get {} from db", computeFailure);
      throw new RuntimeException("Cannot get " + computeFailure + " from db", e);
    }
  }

  /*package*/ ComputeFailure saveComputeFailure(ComputeFailureKey computeFailureKey) {
    ComputeFailure computeFailure;
    computeFailure = new ComputeFailure();
    computeFailure.setId(generateUniqueId());
    computeFailure.setFunctionId(computeFailureKey.getFunctionId());
    computeFailure.setExceptionClass(computeFailureKey.getExceptionClass());
    computeFailure.setExceptionMsg(computeFailureKey.getExceptionMsg());
    computeFailure.setStackTrace(computeFailureKey.getStackTrace());
    
    int rowCount = _jdbcTemplate.update(ComputeFailure.sqlInsert(), computeFailure.toSqlParameterSource());
    if (rowCount == 1) {
      _key2ComputeFailure.put(computeFailureKey, computeFailure);
      return computeFailure;
    }
    return computeFailure;
  }
  
  private void insertRows(String rowType, String sql, List<SqlParameterSource> rows) {
    if (rows.isEmpty()) {
      s_logger.info("No {} rows to insert", rowType);
      return;
    }
    
    s_logger.info("Inserting {} {} rows into DB", rows.size(), rowType);
    
    SqlParameterSource[] batchArgsArray = rows.toArray(new SqlParameterSource[0]);

    int[] counts = _jdbcTemplate.batchUpdate(sql, batchArgsArray);

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
  
  /*package*/ void upsertStatusEntries(
      CalculationJobSpecification job,
      StatusEntry.Status status, 
      Set<ComputationTargetSpecification> targets) {
    
    Integer calcConfId = getCalculationConfigurationId(job.getCalcConfigName());
    
    List<SqlParameterSource> inserts = new ArrayList<SqlParameterSource>();
    List<SqlParameterSource> updates = new ArrayList<SqlParameterSource>();
    
    for (ComputationTargetSpecification target : targets) {
      Integer computationTargetId = getComputationTargetId(target);
      
      MapSqlParameterSource params = new MapSqlParameterSource();
      
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
    int[] counts = _jdbcTemplate.batchUpdate(StatusEntry.sqlInsert(), batchArgsArray);
    checkCount(status + " insert", batchArgsArray, counts);
    
    batchArgsArray = updates.toArray(new SqlParameterSource[0]);
    counts = _jdbcTemplate.batchUpdate(StatusEntry.sqlUpdate(), batchArgsArray);
    checkCount(status + " update", batchArgsArray, counts);
    
    s_logger.info("Inserted {} and updated {} {} status entries", 
        new Object[] {inserts.size(), updates.size(), status});
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
  static class BatchResultWriterFailure implements MissingInput, Serializable {
    private Set<Long> _computeFailureIds = new HashSet<Long>();

    public Set<Long> getComputeFailureIds() {
      return Collections.unmodifiableSet(_computeFailureIds);
    }

    public void addComputeFailureIds(Set<Long> computeFailureIds) {
      _computeFailureIds.addAll(computeFailureIds);
    }
  }

  @Override
  public List<CalculationJobItem> getItemsToExecute(CalculationNode node, CalculationJob job) {
    ArgumentChecker.notNull(node, "Calculation node the execution of the job happens on");
    ArgumentChecker.notNull(job, "Calculation job to execute");
    
    if (!isRestart()) {
      // First time around, always execute everything.
      return job.getJobItems();      
    }
    
    // The batch has been restarted. Figure out from the status table and the computation
    // cache what needs to be recomputed.
    ViewComputationCache cache = node.getCache(job.getSpecification());
    
    ArrayList<CalculationJobItem> itemsToExecute = new ArrayList<CalculationJobItem>();

    for (CalculationJobItem item : job.getJobItems()) {
      
      if (item.isWriteResults()) {
        StatusEntry.Status status = getStatus(job.getSpecification(), item.getComputationTargetSpecification());
        switch (status) {
          case SUCCESS:
            // All values for this computation target are already in the database.
            // However, if the computation cache has been re-started along with the 
            // batch, it is necessary to re-evaluate the item, write the results
            // to the computation cache, but not write anything to the database.
            if (!allOutputsInCache(item, cache)) {
              itemsToExecute.add(new CalculationJobItem(
                  item.getFunctionUniqueIdentifier(),
                  item.getComputationTargetSpecification(),
                  item.getInputs(),
                  item.getDesiredValues(),
                  false)); // do not write into DB
            }
            
            break;
  
          case NOT_RUNNING:
          case RUNNING:
          case FAILURE:
            itemsToExecute.add(item);
            break;
          
          default:
            throw new RuntimeException("Unexpected status " + status);
        }
      } else {
        // e.g., PRIMITIVES. If the computation cache has been re-started along with the 
        // batch, it is necessary to re-evaluate the item, but not otherwise.
        if (!allOutputsInCache(item, cache)) {
          itemsToExecute.add(item); 
        }
      }
        
    }
    
    return itemsToExecute;    
  }
  
  private boolean allOutputsInCache(CalculationJobItem item, ViewComputationCache cache) {
    boolean allOutputsInCache = true;
    
    for (ValueSpecification output : item.getOutputs()) {
      if (cache.getValue(output) == null) {
        allOutputsInCache = false;
        break;
      }
    }
    
    return allOutputsInCache;
  }
  
  private StatusEntry.Status getStatus(CalculationJobSpecification job, ComputationTargetSpecification ct) {
    Integer calcConfId = getCalculationConfigurationId(job.getCalcConfigName());
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
      StatusEntry statusEntry = _jdbcTemplate.queryForObject(
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
  
  /**
   * Useful in tests
   * @return Number of successful risk values in the database
   */
  public int getNumRiskRows() {
    return _jdbcTemplate.queryForInt(RiskValue.sqlCount(), Collections.EMPTY_MAP);
  }
  
  /**
   * Useful in tests
   * @return Number of risk failures in the database
   */
  public int getNumRiskFailureRows() {
    return _jdbcTemplate.queryForInt(RiskFailure.sqlCount(), Collections.EMPTY_MAP);
  }
  
  /**
   * Useful in tests
   * @return Number of risk failure reasons in the database
   */
  public int getNumRiskFailureReasonRows() {
    return _jdbcTemplate.queryForInt(FailureReason.sqlCount(), Collections.EMPTY_MAP);
  }
  
  /**
   * Useful in tests
   * @return Number of risk compute failures in the database
   */
  public int getNumRiskComputeFailureRows() {
    return _jdbcTemplate.queryForInt(ComputeFailure.sqlCount(), Collections.EMPTY_MAP);
  }
  
  
  /**
   * Useful in tests
   * 
   * @param calcConfName Calc conf name
   * @param valueName Value name
   * @param ct Computation target
   * @return Value for this target, null if does not exist
   */
  public RiskValue getValue(String calcConfName, String valueName, ComputationTargetSpecification ct) {
    Integer calcConfId = getCalculationConfigurationId(calcConfName);
    Integer valueId = getValueNameId(valueName);
    Integer computationTargetId = getComputationTargetId(ct);
    
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("calculation_configuration_id", calcConfId);
    params.addValue("value_name_id", valueId);
    params.addValue("computation_target_id", computationTargetId);
    
    try {
      return (RiskValue) _jdbcTemplate.queryForObject(RiskValue.sqlGet(),
          RiskValue.ROW_MAPPER,
          params);
    } catch (IncorrectResultSizeDataAccessException e) {
      return null;
    }
  }
  
}
