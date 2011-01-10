/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.view.ViewCalculationConfiguration;

/**
 * 
 */
public class RiskRun {
  
  private int _id;
  private OpenGammaVersion _openGammaVersion;
  private ComputeHost _masterProcessHost;
  private ObservationDateTime _runTime;
  private LiveDataSnapshot _liveDataSnapshot;
  private Timestamp _createInstant;
  private Timestamp _startInstant;
  private Timestamp _endInstant;
  private int _numRestarts;
  private Set<CalculationConfiguration> _calculationConfigurations = new HashSet<CalculationConfiguration>();
  private Set<RiskRunProperty> _properties = new HashSet<RiskRunProperty>();
  private boolean _complete;
  
  public int getId() {
    return _id;
  }
  
  public void setId(int id) {
    _id = id;
  }
  
  public OpenGammaVersion getOpenGammaVersion() {
    return _openGammaVersion;
  }
  
  public void setOpenGammaVersion(OpenGammaVersion openGammaVersion) {
    _openGammaVersion = openGammaVersion;
  }
  
  public ComputeHost getMasterProcessHost() {
    return _masterProcessHost;
  }
  
  public void setMasterProcessHost(ComputeHost masterProcessHost) {
    _masterProcessHost = masterProcessHost;
  }
  
  public ObservationDateTime getRunTime() {
    return _runTime;
  }
  
  public void setRunTime(ObservationDateTime runTime) {
    _runTime = runTime;
  }
  
  public LiveDataSnapshot getLiveDataSnapshot() {
    return _liveDataSnapshot;
  }
  
  public void setLiveDataSnapshot(LiveDataSnapshot liveDataSnapshot) {
    _liveDataSnapshot = liveDataSnapshot;
  }
  
  public Timestamp getCreateInstant() {
    return _createInstant;
  }
  
  public void setCreateInstant(Timestamp createInstant) {
    _createInstant = createInstant;
  }
  
  public Timestamp getStartInstant() {
    return _startInstant;
  }
  
  public void setStartInstant(Timestamp startInstant) {
    _startInstant = startInstant;
  }
  
  public Timestamp getEndInstant() {
    return _endInstant;
  }
  
  public int getNumRestarts() {
    return _numRestarts;
  }

  public void setNumRestarts(int numRestarts) {
    _numRestarts = numRestarts;
  }
  
  public boolean isRestart() {
    return getNumRestarts() > 0;
  }

  public void setEndInstant(Timestamp endInstant) {
    _endInstant = endInstant;
  }
  
  public Set<CalculationConfiguration> getCalculationConfigurations() {
    return _calculationConfigurations;
  }

  public void setCalculationConfigurations(Set<CalculationConfiguration> calculationConfigurations) {
    _calculationConfigurations = calculationConfigurations;
  }
  
  public CalculationConfiguration getCalculationConfiguration(String calcConfName) {
    for (CalculationConfiguration conf : getCalculationConfigurations()) {
      if (conf.getName().equals(calcConfName)) {
        return conf; 
      }
    }
    return null;
  }
  
  public void addCalculationConfiguration(CalculationConfiguration calcConf) {
    _calculationConfigurations.add(calcConf);
  }
  
  public Set<RiskRunProperty> getProperties() {
    return _properties;
  }
  
  public Map<String, String> getPropertiesMap() {
    Map<String, String> returnValue = new HashMap<String, String>();
    
    for (RiskRunProperty property : getProperties()) {
      returnValue.put(property.getPropertyKey(), property.getPropertyValue());
    }
    
    return returnValue;
  }

  public void setProperties(Set<RiskRunProperty> properties) {
    _properties = properties;
  }
  
  public void addProperty(RiskRunProperty property) {
    _properties.add(property);
  }
  
  public void addProperty(String key, String value) {
    RiskRunProperty property = new RiskRunProperty();
    property.setRiskRun(this);
    property.setPropertyKey(key);
    property.setPropertyValue(value);
    addProperty(property);
  }

  public boolean isComplete() {
    return _complete;
  }
  
  public void setComplete(boolean complete) {
    _complete = complete;
  }
  
  // --------------------------------------------------------------------------
  
  public void addCalculationConfiguration(ViewCalculationConfiguration viewCalcConf) {
    CalculationConfiguration calcConf = getCalculationConfiguration(viewCalcConf.getName());
    if (calcConf != null) {
      throw new IllegalStateException("Already has calc conf " + viewCalcConf.getName());      
    }

    calcConf = new CalculationConfiguration();
    calcConf.setName(viewCalcConf.getName());
    calcConf.setRiskRun(this);
    _calculationConfigurations.add(calcConf);
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public String toString() {
    return "RiskRun[id=" + getId() + "]";
  }
  
}
