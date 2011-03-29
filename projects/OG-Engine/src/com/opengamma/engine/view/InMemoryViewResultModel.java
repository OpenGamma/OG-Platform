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

import javax.time.Instant;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.id.UniqueIdentifier;

/**
 * A simple in-memory implementation of {@link ViewResultModel}.
 */
public abstract class InMemoryViewResultModel implements ViewResultModel, Serializable {
  private UniqueIdentifier _viewProcessId;
  private UniqueIdentifier _viewCycleId;
  private Instant _evaluationTime;
  private Instant _resultTimestamp;
  private final Map<String, ViewCalculationResultModelImpl> _resultsByConfiguration = new HashMap<String, ViewCalculationResultModelImpl>();
  private final Map<ComputationTargetSpecification, ViewTargetResultModelImpl> _resultsByTarget = new HashMap<ComputationTargetSpecification, ViewTargetResultModelImpl>();
  private final List<ViewResultEntry> _allResults = new ArrayList<ViewResultEntry>();
  
  @Override
  public UniqueIdentifier getViewProcessId() {
    return _viewProcessId;
  }

  public void setViewProcessId(UniqueIdentifier viewProcessId) {
    _viewProcessId = viewProcessId;
  }
  
  @Override
  public UniqueIdentifier getViewCycleId() {
    return _viewCycleId;
  }
  
  public void setViewCycleId(UniqueIdentifier viewCycleId) {
    _viewCycleId = viewCycleId;
  }

  @Override
  public Instant getEvaluationTime() {
    return _evaluationTime;
  }

  public void setEvaluationTime(Instant evaluationTime) {
    _evaluationTime = evaluationTime;
  }

  @Override
  public synchronized Instant getResultTimestamp() {
    return _resultTimestamp;
  }

  public synchronized void setResultTimestamp(Instant resultTimestamp) {
    _resultTimestamp = resultTimestamp;
  }

  @Override
  public Collection<ComputationTargetSpecification> getAllTargets() {
    return Collections.unmodifiableSet(_resultsByTarget.keySet());
  }

  public void setCalculationConfigurationNames(final Collection<String> calcConfigurationNames) {
    for (String calcConfigurationName : calcConfigurationNames) {
      _resultsByConfiguration.put(calcConfigurationName, new ViewCalculationResultModelImpl());
    }
  }
  
  public void ensureCalculationConfigurationName(String calcConfigurationName) {
    ensureCalculationConfigurationNames(Collections.singleton(calcConfigurationName));
  }

  public void ensureCalculationConfigurationNames(final Collection<String> calcConfigurationNames) {
    for (String calcConfigurationName : calcConfigurationNames) {
      if (!_resultsByConfiguration.containsKey(calcConfigurationName)) {
        _resultsByConfiguration.put(calcConfigurationName, new ViewCalculationResultModelImpl());
      }
    }
  }

  private void ensureTargetSpec(final ComputationTargetSpecification targetSpec) {
    for (ViewCalculationResultModelImpl model : _resultsByConfiguration.values()) {
      model.addValue(targetSpec, null);
    }
    if (!_resultsByTarget.containsKey(targetSpec)) {
      final ViewTargetResultModelImpl model = new ViewTargetResultModelImpl();
      _resultsByTarget.put(targetSpec, model);
      for (String calcConfigurationName : _resultsByConfiguration.keySet()) {
        model.addValue(calcConfigurationName, null);
      }
    }
  }

  /**
   * For testing.
   */
  /* package */ViewCalculationResultModelImpl getCalculationResultModelImpl(final String calcConfigurationName) {
    return _resultsByConfiguration.get(calcConfigurationName);
  }

  public void setPortfolio(final Portfolio portfolio) {
    recursePortfolio(portfolio.getRootNode());
  }

  private void recursePortfolio(final PortfolioNode node) {
    for (Position position : node.getPositions()) {
      ensureTargetSpec(new ComputationTargetSpecification(position));
    }
    ensureTargetSpec(new ComputationTargetSpecification(node));
    for (PortfolioNode child : node.getChildNodes()) {
      recursePortfolio(child);
    }
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
