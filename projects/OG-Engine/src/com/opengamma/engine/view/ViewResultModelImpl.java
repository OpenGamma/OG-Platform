/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.time.Instant;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;

/**
 * A simple in-memory implementation of {@link ViewResultModel}.
 */
public abstract class ViewResultModelImpl implements ViewResultModel, Serializable {
  private String _viewName;
  private Instant _valuationTime;
  private Instant _resultTimestamp;
  private final Map<String, ViewCalculationResultModelImpl> _resultsByConfiguration = new HashMap<String, ViewCalculationResultModelImpl>();
  private final Map<ComputationTargetSpecification, ViewTargetResultModelImpl> _resultsByTarget = new HashMap<ComputationTargetSpecification, ViewTargetResultModelImpl>();
  
  @Override
  public String getViewName() {
    return _viewName;
  }

  public void setViewName(String viewName) {
    _viewName = viewName;
  }

  @Override
  public Instant getValuationTime() {
    return _valuationTime;
  }

  public void setValuationTime(Instant inputDataTimestamp) {
    _valuationTime = inputDataTimestamp;
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
    _resultsByConfiguration.get(calcConfigurationName).addValue(target, value);
    ViewTargetResultModelImpl targetResult = _resultsByTarget.get(target);
    if (targetResult == null) {
      // TODO: is this necessary? do we ever add arbitrary targets?
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

}
