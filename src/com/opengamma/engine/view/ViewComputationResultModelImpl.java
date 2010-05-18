/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.value.ComputedValue;

/**
 * A simple in-memory implementation of {@link ViewComputationResultModel}.
 * @author kirk
 */
public class ViewComputationResultModelImpl implements
    ViewComputationResultModel, Serializable {
  private long _inputDataTimestamp;
  private long _resultTimestamp;
  private final Map<String, ViewCalculationResultModelImpl> _resultModels =
    new TreeMap<String, ViewCalculationResultModelImpl>();
  
  @Override
  public long getInputDataTimestamp() {
    return _inputDataTimestamp;
  }

  /**
   * @param inputDataTimestamp the inputDataTimestamp to set
   */
  public void setInputDataTimestamp(long inputDataTimestamp) {
    _inputDataTimestamp = inputDataTimestamp;
  }

  @Override
  public long getResultTimestamp() {
    return _resultTimestamp;
  }

  /**
   * @param resultTimestamp the resultTimestamp to set
   */
  public void setResultTimestamp(long resultTimestamp) {
    _resultTimestamp = resultTimestamp;
  }
  
  public void setCalculationConfigurationNames(Collection<String> calcConfigNames) {
    for(String calcConfigName : calcConfigNames) {
      _resultModels.put(calcConfigName, new ViewCalculationResultModelImpl());
    }
  }

  public void setPortfolio(Portfolio portfolio) {
    final PortfolioNode rootNode = portfolio.getRootNode ();
    for(ViewCalculationResultModelImpl calcResultModel: _resultModels.values()) {
      calcResultModel.recursiveAddPortfolio(rootNode);
    }
  }
  
  @Override
  public Collection<ComputationTargetSpecification> getAllTargets() {
    Set<ComputationTargetSpecification> allTargetSpecs = new HashSet<ComputationTargetSpecification>();
    for(ViewCalculationResultModelImpl calcResultModel : _resultModels.values()) {
      allTargetSpecs.addAll(calcResultModel.getAllTargets());
    }
    return allTargetSpecs;
  }

  public void addValue(String calcConfigName, ComputedValue value) {
    ViewCalculationResultModelImpl resultModel = _resultModels.get(calcConfigName);
    resultModel.addValue(value);
  }

  @Override
  public Collection<String> getCalculationConfigurationNames() {
    return Collections.unmodifiableSet(_resultModels.keySet());
  }

  @Override
  public ViewCalculationResultModel getCalculationResult(
      String calcConfigurationName) {
    return _resultModels.get(calcConfigurationName);
  }

}
