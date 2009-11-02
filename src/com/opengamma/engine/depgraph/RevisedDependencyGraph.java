/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2009-11-02 -- This will eventually replace DependencyGraph.
/**
 * 
 *
 * @author kirk
 */
public class RevisedDependencyGraph {
  private final ComputationTargetType _computationTargetType;
  private final Object _computationTarget;
  private final Set<AnalyticValueDefinition<?>> _outputValues = new HashSet<AnalyticValueDefinition<?>>();
  private final Set<DependencyNode> _nodes = new HashSet<DependencyNode>();
  private final Set<AnalyticValueDefinition<?>> _requiredLiveData = new HashSet<AnalyticValueDefinition<?>>();
  private final Map<AnalyticValueDefinition<?>, AnalyticValueDefinition<?>> _resolvedRequirements =
    new HashMap<AnalyticValueDefinition<?>, AnalyticValueDefinition<?>>();
  
  public RevisedDependencyGraph(ComputationTargetType computationTargetType, Object computationTarget) {
    ArgumentChecker.checkNotNull(computationTargetType, "Computation Target Type");
    if(!ComputationTargetType.isCompatible(computationTargetType, computationTarget)) {
      throw new IllegalArgumentException("Target not compatible with type " + computationTargetType);
    }
    _computationTargetType = computationTargetType;
    _computationTarget = computationTarget;
  }

  /**
   * @return the computationTargetType
   */
  public ComputationTargetType getComputationTargetType() {
    return _computationTargetType;
  }

  /**
   * @return the computationTarget
   */
  public Object getComputationTarget() {
    return _computationTarget;
  }

  /**
   * @return the outputValues
   */
  public Set<AnalyticValueDefinition<?>> getOutputValues() {
    return Collections.unmodifiableSet(_outputValues);
  }
  
  public void addOutputValue(AnalyticValueDefinition<?> outputValue) {
    _outputValues.add(outputValue);
  }

  /**
   * @return the nodes
   */
  public Set<DependencyNode> getNodes() {
    return Collections.unmodifiableSet(_nodes);
  }
  
  public DependencyNode getNodeWhichProduces(AnalyticValueDefinition<?> outputValue) {
    for(DependencyNode node : _nodes) {
      if(node.getOutputValues().contains(outputValue)) {
        return node;
      }
    }
    return null;
  }
  
  public void addNode(DependencyNode node) {
    if(node.getComputationTargetType() != getComputationTargetType()) {
      throw new IllegalArgumentException("Node has target type of " + node.getComputationTargetType() + " which doesn't match graph " + getComputationTargetType());
    }
    if(!ObjectUtils.equals(getComputationTarget(), node.getComputationTarget())) {
      throw new IllegalArgumentException("Node matches target type, but graph operates on " + getComputationTarget() + " and node on " + node.getComputationTarget());
    }
    _nodes.add(node);
  }

  /**
   * @return the requiredLiveData
   */
  public Set<AnalyticValueDefinition<?>> getRequiredLiveData() {
    return Collections.unmodifiableSet(_requiredLiveData);
  }
  
  public void addRequiredLiveData(AnalyticValueDefinition<?> liveDataSpec) {
    _requiredLiveData.add(liveDataSpec);
  }
  
  public void setResolvedRequirement(AnalyticValueDefinition<?> requiredOutput, AnalyticValueDefinition<?> actualValueDefinition) {
    _resolvedRequirements.put(requiredOutput, actualValueDefinition);
  }
  
  public AnalyticValueDefinition<?> getResolvedRequirement(AnalyticValueDefinition<?> requiredOutput) {
    return _resolvedRequirements.get(requiredOutput);
  }

}
