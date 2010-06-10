/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class DependencyGraph {
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraph.class);
  
  private final Set<DependencyNode> _rootNodes = new HashSet<DependencyNode>();
  
  /** Includes the root node(s) */
  private final Set<DependencyNode> _dependencyNodes = new HashSet<DependencyNode>();
  
  /** A map to speed up lookups. Contents are equal to _dependencyNodes. */
  private final Map<ComputationTargetType, List<DependencyNode>> _computationTarget2DependencyNode = 
    new HashMap<ComputationTargetType, List<DependencyNode>>();
  
  private final Set<ValueSpecification> _outputValues = new HashSet<ValueSpecification>();

  private final Set<ValueRequirement> _allRequiredLiveData = new HashSet<ValueRequirement>();
  
  public Set<DependencyNode> getRootNodes() {
    return Collections.unmodifiableSet(_rootNodes);
  }

  public Set<ValueSpecification> getOutputValues() {
    return Collections.unmodifiableSet(_outputValues);
  }
  
  public Set<ValueSpecification> getOutputValues(ComputationTargetType type) {
    Set<ValueSpecification> outputValues = new HashSet<ValueSpecification>();
    for (ValueSpecification spec : _outputValues) {
      if (spec.getRequirementSpecification().getTargetSpecification().getType() == type) {
        outputValues.add(spec);        
      }
    }
    return outputValues;
  }
  
  public Set<DependencyNode> getDependencyNodes() {
    return Collections.unmodifiableSet(_dependencyNodes);
  }
  
  public int getSize() {
    return _dependencyNodes.size();
  }
  
  public Collection<DependencyNode> getDependencyNodes(ComputationTargetType type) {
    Collection<DependencyNode> nodes = _computationTarget2DependencyNode.get(type);
    if (nodes == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableCollection(nodes);
  }
  
  public Set<ValueRequirement> getAllRequiredLiveData() {
    return Collections.unmodifiableSet(_allRequiredLiveData);
  }
  
  public Pair<DependencyNode, ValueSpecification> getNodeProducing(ValueRequirement requirement) {
    for (DependencyNode depNode : _dependencyNodes) {
      ValueSpecification satisfyingSpec = depNode.satisfiesRequirement(requirement);
      if (satisfyingSpec != null) {
        return Pair.of(depNode, satisfyingSpec);
      }
    }
    return null;
  }
  
  public void addDependencyNode(DependencyNode node) {
    ArgumentChecker.notNull(node, "Node");
    
    _dependencyNodes.add(node);
    _outputValues.addAll(node.getOutputValues());
    _allRequiredLiveData.addAll(node.getRequiredLiveData());
    
    List<DependencyNode> nodesByType = _computationTarget2DependencyNode.get(node.getComputationTarget().getType());
    if (nodesByType == null) {
      nodesByType = new ArrayList<DependencyNode>();
      _computationTarget2DependencyNode.put(node.getComputationTarget().getType(), nodesByType);
    }
    nodesByType.add(node);
  }
  
  public void addRootNode(DependencyNode node) {
    ArgumentChecker.notNull(node, "Node");
    _rootNodes.add(node);
  }
  
  /**
   * Go through the entire graph and remove any output values that
   * aren't actually consumed.
   * Because functions can possibly produce more than their minimal set of values,
   * we need to strip out the ones that aren't actually used after the whole graph
   * is bootstrapped.
   */
  public void removeUnnecessaryValues() {
    for (DependencyNode node : _dependencyNodes) {
      Set<ValueSpecification> unnecessaryValues = node.removeUnnecessaryOutputs();
      if (!unnecessaryValues.isEmpty()) {
        s_logger.info("{}: removed {} unnecessary potential results", this, unnecessaryValues.size());
        _outputValues.removeAll(unnecessaryValues);
      }
    }
  }

}
