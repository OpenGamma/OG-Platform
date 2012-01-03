/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import javax.time.Instant;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Hibernate bean.
 */
public class RiskRun {
  
  private long _id;
  //private ComputeHost _masterProcessHost;
  //private ObservationDateTime _runTime;
  private LiveDataSnapshot _liveDataSnapshot;
  private Instant _createInstant;
  private Instant _startInstant;
  private Instant _endInstant;
  private Instant _valuationTime;
  private int _numRestarts;
  private Set<CalculationConfiguration> _calculationConfigurations = new HashSet<CalculationConfiguration>();
  private Set<RiskRunProperty> _properties = new HashSet<RiskRunProperty>();
  private boolean _complete;
  private VersionCorrection _versionCorrection;
  private ViewDefinition _viewDefinition;


  public long getId() {
    return _id;
  }
  
  public void setId(long id) {
    _id = id;
  }
  
  public LiveDataSnapshot getLiveDataSnapshot() {
    return _liveDataSnapshot;
  }
  
  public void setLiveDataSnapshot(LiveDataSnapshot liveDataSnapshot) {
    _liveDataSnapshot = liveDataSnapshot;
  }

  public Instant getCreateInstant() {
    return _createInstant;
  }
  
  public void setCreateInstant(Instant createInstant) {
    _createInstant = createInstant;
  }
  
  public Instant getStartInstant() {
    return _startInstant;
  }
  
  public void setStartInstant(Instant startInstant) {
    _startInstant = startInstant;
  }
  
  public Instant getEndInstant() {
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

  public void setEndInstant(Instant endInstant) {
    _endInstant = endInstant;
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
  
  public Set<CalculationConfiguration> getCalculationConfigurations() {
    return _calculationConfigurations;
  }

  public void setCalculationConfigurations(Set<CalculationConfiguration> calculationConfigurations) {
    _calculationConfigurations = calculationConfigurations;
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

  public Instant getValuationTime() {
    return _valuationTime;
  }

  public void setValuationTime(Instant valuationTime) {
    this._valuationTime = valuationTime;
  }
  
  @Override
  public String toString() {
    return "RiskRun[id=" + getId() + "]";
  }

  public void setVersionCorrection(VersionCorrection versionCorrection) {
    _versionCorrection = versionCorrection;
  }

  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  public void setViewDefinition(ViewDefinition viewDefinition) {
    _viewDefinition = viewDefinition;
  }

  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }
}
