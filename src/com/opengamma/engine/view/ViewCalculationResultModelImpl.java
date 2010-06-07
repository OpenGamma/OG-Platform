/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.value.ComputedValue;

/**
 * A simple implementation of the calculation result model.
 */
public class ViewCalculationResultModelImpl implements Serializable,
    ViewCalculationResultModel {
  private DependencyGraphBuilder _dependencyGraphModel;
  private final Map<ComputationTargetSpecification, Map<String, ComputedValue>> _values =
    new HashMap<ComputationTargetSpecification, Map<String, ComputedValue>>();

  public void setDependencyGraphModel(DependencyGraphBuilder dependencyGraphModel) {
    _dependencyGraphModel = dependencyGraphModel;
  }
  
  public DependencyGraphBuilder getDependencyGraphModel() {
    return _dependencyGraphModel;
  }

  @Override
  public Collection<ComputationTargetSpecification> getAllTargets() {
    return Collections.unmodifiableSet(_values.keySet());
  }
  
  @Override
  public Map<String, ComputedValue> getValues(
      ComputationTargetSpecification target) {
    return _values.get(target);
  }

  protected void recursiveAddPortfolio(PortfolioNode node) {
    for (Position position : node.getPositions()) {
      ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(position);
      if (!_values.containsKey(targetSpec)) {
        _values.put(targetSpec, new HashMap<String, ComputedValue>());
      }
    }
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(node);
    if (!_values.containsKey(targetSpec)) {
      _values.put(targetSpec, new HashMap<String, ComputedValue>());
    }
    for (PortfolioNode child : node.getChildNodes()) {
      recursiveAddPortfolio(child);
    }
  }

  public void addValue(ComputedValue value) {
    ComputationTargetSpecification targetSpec = value.getSpecification().getRequirementSpecification().getTargetSpecification();
    if (!(_values.containsKey(targetSpec))) {
      throw new IllegalArgumentException("Target spec " + targetSpec + " not reachable from initialization in recursiveAddPortfolio");
    }
    _values.get(targetSpec).put(value.getSpecification().getRequirementSpecification().getValueName(), value);
  }

}
