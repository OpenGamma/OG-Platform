/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.ViewTargetResultModel;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * A simple in-memory implementation of {@link ViewResultModel}.
 */
public abstract class InMemoryViewResultModel implements ViewResultModel, Serializable {

  private static final long serialVersionUID = 1L;

  private UniqueId _viewProcessId;
  private UniqueId _viewCycleId;
  private ViewCycleExecutionOptions _viewCycleExecutionOptions;
  private Instant _calculationTime;
  private Duration _calculationDuration;
  private VersionCorrection _versionCorrection;
  private final Map<String, ViewCalculationResultModelImpl> _resultsByConfiguration = new HashMap<String, ViewCalculationResultModelImpl>();
  private final Map<ComputationTargetSpecification, ViewTargetResultModelImpl> _resultsByTarget = new HashMap<ComputationTargetSpecification, ViewTargetResultModelImpl>();

  public InMemoryViewResultModel() {
  }

  public InMemoryViewResultModel(final ViewResultModel copyFrom) {
    update(copyFrom);
  }

  /**
   * Updates the data held in this model with data from (and about) a delta cycle.
   * 
   * @param delta the delta results, not null
   */
  public void update(final ViewResultModel delta) {
    setViewProcessId(delta.getViewProcessId());
    setViewCycleId(delta.getViewCycleId());
    setViewCycleExecutionOptions(delta.getViewCycleExecutionOptions());
    setCalculationTime(delta.getCalculationTime());
    setCalculationDuration(delta.getCalculationDuration());
    setVersionCorrection(delta.getVersionCorrection());
    for (String calculationConfiguration : delta.getCalculationConfigurationNames()) {
      final ViewCalculationResultModel deltaConfigResults = delta.getCalculationResult(calculationConfiguration);
      ViewCalculationResultModelImpl calcConfigResults = _resultsByConfiguration.get(calculationConfiguration);
      if (calcConfigResults == null) {
        calcConfigResults = new ViewCalculationResultModelImpl();
        _resultsByConfiguration.put(calculationConfiguration, calcConfigResults);
      }
      for (ComputationTargetSpecification target : deltaConfigResults.getAllTargets()) {
        for (ComputedValueResult value : deltaConfigResults.getAllValues(target)) {
          calcConfigResults.addValue(target, value);
          ViewTargetResultModelImpl targetResults = _resultsByTarget.get(target);
          if (targetResults == null) {
            targetResults = new ViewTargetResultModelImpl();
            _resultsByTarget.put(target, targetResults);
          }
          targetResults.addValue(calculationConfiguration, value);
        }
      }
    }
  }

  public boolean isEmpty() {
    // Adding any results makes the maps non-empty
    return _resultsByTarget.isEmpty();
  }

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
  public ViewCycleExecutionOptions getViewCycleExecutionOptions() {
    return _viewCycleExecutionOptions;
  }

  public void setViewCycleExecutionOptions(ViewCycleExecutionOptions viewCycleExecutionOptions) {
    _viewCycleExecutionOptions = viewCycleExecutionOptions;
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
  public Set<ComputationTargetSpecification> getAllTargets() {
    return Collections.unmodifiableSet(_resultsByTarget.keySet());
  }

  /**
   * For testing.
   */
  /* package */ViewCalculationResultModelImpl getCalculationResultModelImpl(final String calcConfigurationName) {
    return _resultsByConfiguration.get(calcConfigurationName);
  }

  public void addValue(final String calcConfigurationName, final ComputedValueResult value) {
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
    final ArrayList<ViewResultEntry> results = new ArrayList<ViewResultEntry>();
    for (Map.Entry<String, ViewCalculationResultModelImpl> config : _resultsByConfiguration.entrySet()) {
      final Collection<ComputationTargetSpecification> targets = config.getValue().getAllTargets();
      int numTargets = targets.size();
      int totalComputedValues = 0;
      int targetsSeen = 0;
      for (ComputationTargetSpecification target : targets) {
        final Collection<ComputedValueResult> computedValues = config.getValue().getAllValues(target);
        totalComputedValues += computedValues.size();
        targetsSeen++;
        results.ensureCapacity(numTargets * (totalComputedValues / targetsSeen));
        for (ComputedValueResult computedValue : computedValues) {
          results.add(new ViewResultEntry(config.getKey(), computedValue));
        }
      }
    }
    return results;
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
