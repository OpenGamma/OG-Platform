/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch.db;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.impl.StatelessSessionImpl;

import com.opengamma.engine.ComputationTargetSpecification;

/**
 * Context information needed to write risk into the OpenGamma
 * batch risk database. The class is designed to be Serializable
 * so it can be sent down to compute nodes on the grid.
 */
public class BatchDbRiskContextImpl implements BatchDbRiskContext, Serializable {
  
  // Variables YOU must set before calling initialize()
  
  private Integer _riskRunId;
  private Map<ComputationTargetSpecification, Integer> _computationTarget2Id;
  private Map<String, Integer> _calculationConfiguration2Id;
  private Map<String, Integer> _riskValueName2Id;
  private Integer _computeNodeId;
  
  private transient SessionFactory _sessionFactory;
  
  // Variables set in initialize()
  
  private transient SequenceStyleGenerator _idGenerator;
  private transient StatelessSessionImpl _session;
  private transient boolean _initialized; // = false;
  
  public void initialize() {
    if (_riskRunId == null ||
        _computationTarget2Id == null ||
        _calculationConfiguration2Id == null ||
        _riskValueName2Id == null ||
        _computeNodeId == null) {
      throw new IllegalStateException("Not all required arguments are set");
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
    ensureInitialized();
    
    Serializable generatedId = _idGenerator.generate(_session, null);
    if (!(generatedId instanceof Long)) {
      throw new IllegalStateException("Got ID of type " + generatedId.getClass());
    }
    return ((Long) generatedId).longValue();
  }

  public int getCalculationConfigurationId(String calcConfName) {
    ensureInitialized();
    
    Integer confId = _calculationConfiguration2Id.get(calcConfName);
    if (confId == null) {
      throw new IllegalArgumentException("Calculation configuration " + calcConfName + " is not in the database");
    }
    return confId.intValue();
  }

  public int getComputationTargetId(ComputationTargetSpecification spec) {
    ensureInitialized();
    
    Integer specId = _computationTarget2Id.get(spec);
    if (specId == null) {
      throw new IllegalArgumentException(spec + " is not in the database");
    }
    return specId.intValue();
  }

  public int getComputeNodeId() {
    ensureInitialized();
    return _computeNodeId;
  }

  public int getRiskRunId() {
    ensureInitialized();
    return _riskRunId;
  }

  public int getValueNameId(String name) {
    ensureInitialized();
    
    Integer valueNameId = _riskValueName2Id.get(name);
    if (valueNameId == null) {
      throw new IllegalArgumentException("Value name " + name + " is not in the database");
    }
    return valueNameId.intValue();
  }
  
}
