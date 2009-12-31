/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.value.NewComputedValue;
import com.opengamma.engine.view.cache.ViewComputationCache;

/**
 * A simple in-memory implementation of {@link ViewComputationResultModel}.
 * @author kirk
 */
public class ViewComputationResultModelImpl implements
    ViewComputationResultModel, Serializable {
  private final Map<ComputationTargetSpecification, Set<NewComputedValue>> _values =
    new HashMap<ComputationTargetSpecification, Set<NewComputedValue>>();
  private long _inputDataTimestamp;
  private long _resultTimestamp;
  private ViewComputationCache _cache;
  private DependencyGraphModel _dependencyGraphModel;
  private Portfolio _portfolio;
  
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

  public void setPortfolio(Portfolio portfolio, PortfolioNode populatedRootNode) {
    _portfolio = portfolio; 
    recursiveAddPortfolio(populatedRootNode);
  }
  
  public Portfolio getPortfolio() {
    return _portfolio;
  }
  
  private void recursiveAddPortfolio(PortfolioNode node) {
    for (Position position : node.getPositions()) {
      ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.POSITION, position.getIdentityKey());
      if(!_values.containsKey(targetSpec)) {
        _values.put(targetSpec, new HashSet<NewComputedValue>());
      }
    }
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.MULTIPLE_POSITIONS, node.getIdentityKey());
    if(!_values.containsKey(targetSpec)) {
      _values.put(targetSpec, new HashSet<NewComputedValue>());
    }
    for (PortfolioNode subNode : node.getSubNodes()) {
      recursiveAddPortfolio(subNode);
    }
  }
  
  public void setComputationCache(ViewComputationCache cache) {
    _cache = cache;
  }  
  
  public ViewComputationCache getComputationCache() {
    return _cache;  
  }

  /**
   * @param dependencyGraphModel
   */
  public void setDependencyGraphModel(DependencyGraphModel dependencyGraphModel) {
    _dependencyGraphModel = dependencyGraphModel;
  }
  
  public DependencyGraphModel getDependencyGraphModel() {
    return _dependencyGraphModel;
  }

  @Override
  public Collection<ComputationTargetSpecification> getAllTargets() {
    return new ArrayList<ComputationTargetSpecification>(_values.keySet());
  }

  @Override
  public Collection<NewComputedValue> getValues(
      ComputationTargetSpecification target) {
    return new ArrayList<NewComputedValue>(_values.get(target));
  }
  
  public void addValue(NewComputedValue value) {
    ComputationTargetSpecification targetSpec = value.getSpecification().getRequirementSpecification().getTargetSpecification();
    _values.get(targetSpec).add(value);
  }

}
