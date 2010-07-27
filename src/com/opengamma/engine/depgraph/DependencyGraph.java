/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
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
  
  private final String _calcConfName;
  
  /** Includes the root node(s) */
  private final Set<DependencyNode> _dependencyNodes = new HashSet<DependencyNode>();
  
  private final Set<DependencyNode> _rootNodes = new HashSet<DependencyNode>();
  
  private final Set<ValueSpecification> _outputValues = new HashSet<ValueSpecification>();
  
  /** A map to speed up lookups. Contents are equal to _dependencyNodes. */
  private final Map<ComputationTargetType, Set<DependencyNode>> _computationTarget2DependencyNode = 
    new HashMap<ComputationTargetType, Set<DependencyNode>>();
  
  private final Map<ValueRequirement, DependencyNode> _valueRequirement2DependencyNode = 
    new HashMap<ValueRequirement, DependencyNode>();    

  private final Set<ValueRequirement> _allRequiredLiveData = new HashSet<ValueRequirement>();
  private final Set<ComputationTargetSpecification> _allComputationTargets = new HashSet<ComputationTargetSpecification>();
  
  public DependencyGraph(String calcConfName) {
    ArgumentChecker.notNull(calcConfName, "Calculation configuration name");
    _calcConfName = calcConfName;
  }
  
  public String getCalcConfName() {
    return _calcConfName;
  }

  /**
   * Returns nodes that have no dependent nodes in this graph.
   * <p>
   * The case of unconnected sub-graphs: say your original graph is
   * 
   *  A->B->C
   *  
   * and your subgraph contains only nodes A and C,
   * then according to the above definition (no dependent nodes),
   * both A and C are considered to be root.
   * Of course unconnected sub-graphs should not occur in practice.
   *  
   * @return Nodes that have no dependent nodes in this graph.
   */
  public Set<DependencyNode> getRootNodes() {
    return Collections.unmodifiableSet(_rootNodes);
  }
  
  public boolean isRootNode(DependencyNode node) {
    return _rootNodes.contains(node);         
  }

  public Set<ValueSpecification> getOutputValues() {
    return Collections.unmodifiableSet(_outputValues);
  }
  
  public Set<ComputationTargetSpecification> getAllComputationTargets() {
    return  Collections.unmodifiableSet(_allComputationTargets);
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
  
  public Set<DependencyNode> getDependencyNodes(ComputationTargetType type) {
    Set<DependencyNode> nodes = _computationTarget2DependencyNode.get(type);
    if (nodes == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(nodes);
  }
  
  public Set<ValueRequirement> getAllRequiredLiveData() {
    return Collections.unmodifiableSet(_allRequiredLiveData);
  }
  
  public Pair<DependencyNode, ValueSpecification> getNodeProducing(ValueRequirement requirement) {
    DependencyNode node = _valueRequirement2DependencyNode.get(requirement);
    if (node == null) {
      return null;
    }
    ValueSpecification resolvedOutput = node.resolveOutput(requirement);
    if (resolvedOutput == null) {
      throw new IllegalStateException(requirement + " was in value requirements map," +
          " but node " + node + " did not produce the expected requirement");
    }
    return Pair.of(node, resolvedOutput);
  }
  
  public void addDependencyNode(DependencyNode node) {
    ArgumentChecker.notNull(node, "Node");
    
    _dependencyNodes.add(node);
    _outputValues.addAll(node.getOutputValues());
    _allRequiredLiveData.addAll(node.getRequiredLiveData());
    _allComputationTargets.add(node.getComputationTarget().toSpecification());
    
    for (ValueSpecification output : node.getOutputValues()) {
      DependencyNode previousValue = _valueRequirement2DependencyNode.put(output.getRequirementSpecification(), node);
      if (previousValue != null) {
        throw new IllegalStateException(output.getRequirementSpecification() + " should map to one dependency node only");
      }
    }
    
    Set<DependencyNode> nodesByType = _computationTarget2DependencyNode.get(node.getComputationTarget().getType());
    if (nodesByType == null) {
      nodesByType = new HashSet<DependencyNode>();
      _computationTarget2DependencyNode.put(node.getComputationTarget().getType(), nodesByType);
    }
    nodesByType.add(node);
    
    // is this node root at the moment?
    boolean isRoot = true; 
    for (DependencyNode dependentNode : node.getDependentNodes()) {
      if (_dependencyNodes.contains(dependentNode)) {
        isRoot = false;
        break;
      }
    }
    
    if (isRoot) {
      _rootNodes.add(node);
    }
    
    // might be that some children became non-root as a result of adding this node 
    for (DependencyNode childNode : node.getInputNodes()) {
      _rootNodes.remove(childNode);
    }
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
      for (ValueSpecification unnecessaryValue : unnecessaryValues) {
        DependencyNode removed = _valueRequirement2DependencyNode.remove(unnecessaryValue.getRequirementSpecification());
        if (removed == null) {
          throw new IllegalStateException("A value requirement " + unnecessaryValue.getRequirementSpecification() + " wasn't mapped");
        }
      }
    }
  }
  
  /**
   * @return Nodes in an executable order. E.g., if there are two nodes, A and B, and A
   * depends on B, then list [B, A] is returned (and not [A, B]).
   */
  public List<DependencyNode> getExecutionOrder() {
    ArrayList<DependencyNode> executionOrder = new ArrayList<DependencyNode>();
    HashSet<DependencyNode> alreadyEvaluated = new HashSet<DependencyNode>();
    
    for (DependencyNode root : getRootNodes()) {
      getExecutionOrder(root, executionOrder, alreadyEvaluated);
    }
        
    return executionOrder;
  }
  
  private void getExecutionOrder(DependencyNode currentNode, List<DependencyNode> executionOrder, Set<DependencyNode> alreadyEvaluated) {
    for (DependencyNode child : currentNode.getInputNodes()) {
      getExecutionOrder(child, executionOrder, alreadyEvaluated);
    }
    
    if (!alreadyEvaluated.contains(currentNode)) {
      executionOrder.add(currentNode);      
      alreadyEvaluated.add(currentNode);
    }
  }
  
  /**
   * @param filter Tells whether to include node or not
   * @return A sub-graph consisting of all nodes accepted by the filter.
   */
  public DependencyGraph subGraph(DependencyNodeFilter filter) {
    DependencyGraph subGraph = new DependencyGraph(getCalcConfName());
    for (DependencyNode node : getDependencyNodes()) {
      if (filter.accept(node)) {
        subGraph.addDependencyNode(node);
      }
    }
    return subGraph;
  }

}
