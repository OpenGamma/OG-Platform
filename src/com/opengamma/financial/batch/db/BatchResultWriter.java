/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.calcnode.AbstractCalculationNode;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.engine.view.calcnode.ResultWriter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.DBTool;

/**
 * Writes risk into the OpenGamma batch risk database. 
 */
public class BatchResultWriter implements ResultWriter, Serializable {
  
  private static final Logger s_logger = LoggerFactory.getLogger(BatchResultWriter.class);
  
  // Variables YOU must set before calling initialize()
  
  private String _jdbcUrl;
  private String _username;
  private String _password;
  
  private Integer _riskRunId;
  private Map<ComputationTargetSpecification, Integer> _computationTarget2Id;
  private Map<String, Integer> _calculationConfiguration2Id;
  private Map<String, Integer> _riskValueName2Id;
  private Integer _computeNodeId;
  
  // Variables set in initialize()
  
  /**
   * The template for direct JDBC operations.
   */
  private transient SimpleJdbcTemplate _jdbcTemplate;

  /**
   * The factory for Hibernate operations
   */
  private transient SessionFactory _sessionFactory;

  private transient SequenceStyleGenerator _idGenerator;
  private transient StatelessSessionImpl _session;
  private transient boolean _initialized; // = false;
  
  public void initialize(AbstractCalculationNode computeNode) {
    if (_riskRunId == null ||
        _computationTarget2Id == null ||
        _calculationConfiguration2Id == null ||
        _riskValueName2Id == null ||
        _jdbcUrl == null ||
        _username == null ||
        _password == null) {
      throw new IllegalStateException("Not all required arguments are set");
    }
    
    DBTool tool = new DBTool();
    tool.setJdbcUrl(_jdbcUrl);
    tool.setUser(_username);
    tool.setPassword(_password);
    tool.initialize();
    
    if (_sessionFactory == null) {
      Configuration configuration = tool.getHibernateConfiguration();
      for (Class<?> clazz : BatchDbManagerImpl.getHibernateMappingClasses()) {
        configuration.addClass(clazz);
      }
  
      SessionFactory sessionFactory = configuration.buildSessionFactory();
      setSessionFactory(sessionFactory);
    }
    
    if (_jdbcTemplate == null) {
      DataSourceTransactionManager transactionManager = tool.getTransactionManager();
      DataSource dataSource = transactionManager.getDataSource();
      _jdbcTemplate = new SimpleJdbcTemplate(dataSource);
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
    _session = (StatelessSessionImpl) _sessionFactory.openStatelessSession();
    
    _initialized = true;
  }
  
  public boolean isInitialized() {
    return _initialized;
  }
  
  public void setSessionFactory(SessionFactory sessionFactory) {
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
  public SimpleJdbcTemplate getJdbcTemplate() {
    return _jdbcTemplate;
  }

  public void setJdbcTemplate(SimpleJdbcTemplate jdbcTemplate) {
    _jdbcTemplate = jdbcTemplate;
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

  public void setRiskRun(RiskRun riskRun) {
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
  }

  public void setComputationTargets(Set<ComputationTarget> computationTargets) {
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
    if (_riskValueName2Id != null) {
      throw new IllegalStateException("Already set");
    }
    
    _riskValueName2Id = new HashMap<String, Integer>();
    for (RiskValueName valueName : valueNames) {
      _riskValueName2Id.put(valueName.getName(), valueName.getId());
    }
  }

  public void setComputeNode(ComputeNode computeNode) {
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
    Number confId = _calculationConfiguration2Id.get(calcConfName);
    if (confId == null) {
      throw new IllegalArgumentException("Calculation configuration " + calcConfName + " is not in the database");
    }
    return confId.intValue();
  }

  public int getComputationTargetId(ComputationTargetSpecification spec) {
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
  
  // --------------------------------------------------------------------------
  
  public Map<ComputationTargetSpecification, Integer> getComputationTarget2Id() {
    return _computationTarget2Id;
  }

  public void setComputationTarget2Id(Map<ComputationTargetSpecification, Integer> computationTarget2Id) {
    _computationTarget2Id = computationTarget2Id;
  }

  public Map<String, Integer> getCalculationConfiguration2Id() {
    return _calculationConfiguration2Id;
  }

  public void setCalculationConfiguration2Id(Map<String, Integer> calculationConfiguration2Id) {
    _calculationConfiguration2Id = calculationConfiguration2Id;
  }

  public Map<String, Integer> getRiskValueName2Id() {
    return _riskValueName2Id;
  }

  public void setRiskValueName2Id(Map<String, Integer> riskValueName2Id) {
    _riskValueName2Id = riskValueName2Id;
  }

  public void setRiskRunId(Integer riskRunId) {
    _riskRunId = riskRunId;
  }

  public void setComputeNodeId(Integer computeNodeId) {
    _computeNodeId = computeNodeId;
  }
  
  // --------------------------------------------------------------------------

  public int getValueNameId(String name) {
    Number valueNameId = _riskValueName2Id.get(name);
    if (valueNameId == null) {
      throw new IllegalArgumentException("Value name " + name + " is not in the database");
    }
    return valueNameId.intValue();
  }

  @Override
  public void write(AbstractCalculationNode node, CalculationJobResult result) {
    List<MapSqlParameterSource> successes = new ArrayList<MapSqlParameterSource>();
    List<MapSqlParameterSource> failures = new ArrayList<MapSqlParameterSource>();
    
    int riskRunId = getRiskRunId();
    int calcConfId = getCalculationConfigurationId(result.getSpecification().getCalcConfigName());
    
    Date evalInstant = new Date();
    
    for (CalculationJobResultItem item : result.getResultItems()) {
      for (ComputedValue value : item.getResults()) {
        
        // TODO -- need to transmit what to write out to the node and not hard-code it
        ComputationTargetType type = 
          value.getSpecification().getRequirementSpecification().getTargetSpecification().getType();
        if (type == ComputationTargetType.PRIMITIVE || type == ComputationTargetType.SECURITY) { 
          continue;
        }

        if (!(value.getValue() instanceof Double)) {
          throw new IllegalArgumentException("Can only insert Double values, got " + 
              value.getValue().getClass() + " for " + item);
        }
        Double valueAsDouble = (Double) value.getValue();

        int valueNameId = getValueNameId(value.getSpecification().getRequirementSpecification().getValueName());
        int computationTargetId = getComputationTargetId(value.getSpecification().getRequirementSpecification().getTargetSpecification());
        
        if (item.getResult() == InvocationResult.SUCCESS) {

          MapSqlParameterSource args = new MapSqlParameterSource()
            .addValue("calculation_configuration_id", calcConfId)
            .addValue("value_name_id", valueNameId)
            .addValue("computation_target_id", computationTargetId)
            .addValue("run_id", riskRunId)
            .addValue("value", valueAsDouble)
            .addValue("eval_instant", evalInstant);
          successes.add(args);
        } else {
          
        }
      }
    }
    
    if (successes.isEmpty() && failures.isEmpty()) {
      return;
    }
    
    synchronized (this) {
      if (!isInitialized()) {
        initialize(node);
      }
    }
    
    int computeNodeId = getComputeNodeId(); 
    for (MapSqlParameterSource args : successes) {
      long id = generateUniqueId();
      args.addValue("id", id);
      args.addValue("compute_node_id", computeNodeId);
    }
    
    SqlParameterSource[] batchArgsArray = successes.toArray(new SqlParameterSource[0]);
    s_logger.info("{}: Inserting {} risk rows into DB", result, batchArgsArray.length);
    int[] counts = _jdbcTemplate.batchUpdate(sqlInsertRisk(), batchArgsArray);

    int totalCount = 0;
    for (int count : counts) {
      totalCount += count;
    }
    s_logger.info("{}: Inserted {} risk rows into DB", result, totalCount);
    if (totalCount != batchArgsArray.length) {
      s_logger.warn("{}: Risk insert count is wrong", result);      
    }
  }
  
  /**
   * Gets the SQL for inserting risk into the database.
   * @return the SQL, not null
   */
  private String sqlInsertRisk() {
    return "INSERT INTO rsk_value " +
              "(id, calculation_configuration_id, value_name_id, computation_target_id, run_id, value, " +
              "eval_instant, compute_node_id) " +
            "VALUES " +
              "(:id, :calculation_configuration_id, :value_name_id, :computation_target_id, :run_id, :value," +
              ":eval_instant, :compute_node_id)";
  }
  
}
