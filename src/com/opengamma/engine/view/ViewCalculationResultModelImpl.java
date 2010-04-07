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
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.value.ComputedValue;

/**
 * 
 *
 * @author kirk
 */
public class ViewCalculationResultModelImpl implements Serializable,
    ViewCalculationResultModel {
  private DependencyGraphModel _dependencyGraphModel;
  private final Map<ComputationTargetSpecification, Map<String, ComputedValue>> _values =
    new HashMap<ComputationTargetSpecification, Map<String, ComputedValue>>();

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
      if(!_values.containsKey(targetSpec)) {
        _values.put(targetSpec, new HashMap<String, ComputedValue>());
      }
    }
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(node);
    if(!_values.containsKey(targetSpec)) {
      _values.put(targetSpec, new HashMap<String, ComputedValue>());
    }
    for (PortfolioNode subNode : node.getSubNodes()) {
      recursiveAddPortfolio(subNode);
    }
  }

  public void addValue(ComputedValue value) {
    ComputationTargetSpecification targetSpec = value.getSpecification().getRequirementSpecification().getTargetSpecification();
    if(!(_values.containsKey(targetSpec))) {
      throw new IllegalArgumentException("Target spec " + targetSpec + " not reachable from initialization in recursiveAddPortfolio");
    }
    _values.get(targetSpec).put(value.getSpecification().getRequirementSpecification().getValueName(), value);
  }

}
