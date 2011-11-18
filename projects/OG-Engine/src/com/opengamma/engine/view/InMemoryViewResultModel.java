/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.Duration;
import javax.time.Instant;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * A simple in-memory implementation of {@link ViewResultModel}.
 */
public abstract class InMemoryViewResultModel implements ViewResultModel, Serializable {

  private static final long serialVersionUID = 1L;
  
  private UniqueId _viewProcessId;
  private UniqueId _viewCycleId;
  private Instant _valuationTime;
  private Instant _calculationTime;
  private Duration _calculationDuration;
  private VersionCorrection _versionCorrection;
  private final Map<String, ViewCalculationResultModelImpl> _resultsByConfiguration = new HashMap<String, ViewCalculationResultModelImpl>();
  private final Map<ComputationTargetSpecification, ViewTargetResultModelImpl> _resultsByTarget = new HashMap<ComputationTargetSpecification, ViewTargetResultModelImpl>();
  private final List<ViewResultEntry> _allResults = new ArrayList<ViewResultEntry>();
  
  @Override
  public UniqueId getViewProcessId() {
    return _viewProcessId;
  }

  public void setViewProcessId(UniqueId viewProcessId) {
    _viewProcessId = viewProcessId;
  }
  
  @Override
  public UniqueId getViewCycleId() {
    return _viewCycleId;
  }
  
  public void setViewCycleId(UniqueId viewCycleId) {
    _viewCycleId = viewCycleId;
  }

  @Override
  public Instant getValuationTime() {
    return _valuationTime;
  }

  public void setValuationTime(Instant valuationTime) {
    _valuationTime = valuationTime;
  }

  @Override
  public Instant getCalculationTime() {
    return _calculationTime;
  }

  public void setCalculationTime(Instant calculationTime) {
    _calculationTime = calculationTime;
  }
  
  @Override
  public Duration getCalculationDuration() {
    return _calculationDuration;
  }
  
  public void setCalculationDuration(Duration calculationDuration) {
    _calculationDuration = calculationDuration;
  }
  
  @Override
  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }
  
  public void setVersionCorrection(VersionCorrection versionCorrection) {
    _versionCorrection = versionCorrection;
  }

  @Override
  public Collection<ComputationTargetSpecification> getAllTargets() {
    return Collections.unmodifiableSet(_resultsByTarget.keySet());
  }

  /**
   * For testing.
   */
  /* package */ViewCalculationResultModelImpl getCalculationResultModelImpl(final String calcConfigurationName) {
    return _resultsByConfiguration.get(calcConfigurationName);
  }

  public void addValue(final String calcConfigurationName, final ComputedValue value) {
    final ComputationTargetSpecification target = value.getSpecification().getTargetSpecification();

    ViewCalculationResultModelImpl result = _resultsByConfiguration.get(calcConfigurationName);
    if (result == null) {
      result = new ViewCalculationResultModelImpl();
      _resultsByConfiguration.put(calcConfigurationName, result);
    }
    result.addValue(target, value);
    
    ViewTargetResultModelImpl targetResult = _resultsByTarget.get(target);
    if (targetResult == null) {
      targetResult = new ViewTargetResultModelImpl();
      _resultsByTarget.put(target, targetResult);
    }
    
    targetResult.addValue(calcConfigurationName, value);
    
    _allResults.add(new ViewResultEntry(calcConfigurationName, value));
  }

  @Override
  public Collection<String> getCalculationConfigurationNames() {
    return Collections.unmodifiableSet(_resultsByConfiguration.keySet());
  }

  @Override
  public ViewCalculationResultModel getCalculationResult(String calcConfigurationName) {
    return _resultsByConfiguration.get(calcConfigurationName);
  }

  @Override
  public ViewTargetResultModel getTargetResult(ComputationTargetSpecification targetSpecification) {
    return _resultsByTarget.get(targetSpecification);
  }

  @Override
  public List<ViewResultEntry> getAllResults() {
    return Collections.unmodifiableList(_allResults);
  }

  @Override
  public Set<String> getAllOutputValueNames() {
    Set<String> outputValueNames = new HashSet<String>();
    for (ViewResultEntry result : getAllResults()) {
      outputValueNames.add(result.getComputedValue().getSpecification().getValueName());      
    }
    return outputValueNames;
  }
  
}
