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
import java.util.LinkedList;
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
import com.opengamma.util.PublicAPI;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@PublicAPI
public class DependencyGraph {
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraph.class);

  private final String _calcConfName;

  /** Includes the root node(s) */
  private final Set<DependencyNode> _dependencyNodes = new HashSet<DependencyNode>();

  private final Set<DependencyNode> _rootNodes = new HashSet<DependencyNode>();

  /**
   * A cache of output values from this graph's nodes
   */
  private final Set<ValueSpecification> _outputValues = new HashSet<ValueSpecification>();

  /**
   * A cache of terminal output values from this graph's nodes
   */
  private final Set<ValueSpecification> _terminalOutputValues = new HashSet<ValueSpecification>();

  /** A map to speed up lookups. Contents are equal to _dependencyNodes. */
  private final Map<ComputationTargetType, Set<DependencyNode>> _computationTargetType2DependencyNode = new HashMap<ComputationTargetType, Set<DependencyNode>>();

  /** A map to speed up lookups. Contents are equal to _dependencyNodes. */
  private final Map<ValueSpecification, DependencyNode> _specification2DependencyNode = new HashMap<ValueSpecification, DependencyNode>();

  private final Map<String, Map<ComputationTargetSpecification, List<Pair<DependencyNode, ValueSpecification>>>> _valueRequirement2Specifications = new HashMap<String, Map<ComputationTargetSpecification, List<Pair<DependencyNode, ValueSpecification>>>>(); // TODO

  private final Set<Pair<ValueRequirement, ValueSpecification>> _allRequiredLiveData = new HashSet<Pair<ValueRequirement, ValueSpecification>>();
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

  public boolean containsNode(DependencyNode node) {
    return _dependencyNodes.contains(node);
  }

  public Set<ValueSpecification> getOutputValues() {
    return Collections.unmodifiableSet(_outputValues);
  }

  public Set<ValueSpecification> getTerminalOutputValues() {
    return Collections.unmodifiableSet(_terminalOutputValues);
  }

  public Set<ComputationTargetSpecification> getAllComputationTargets() {
    return Collections.unmodifiableSet(_allComputationTargets);
  }

  public Set<ValueSpecification> getOutputValues(ComputationTargetType type) {
    Set<ValueSpecification> outputValues = new HashSet<ValueSpecification>();
    for (ValueSpecification spec : _outputValues) {
      if (spec.getTargetSpecification().getType() == type) {
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
    Set<DependencyNode> nodes = _computationTargetType2DependencyNode.get(type);
    if (nodes == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(nodes);
  }

  public Set<Pair<ValueRequirement, ValueSpecification>> getAllRequiredLiveData() {
    return Collections.unmodifiableSet(_allRequiredLiveData);
  }

  /**
   * Finds a node which has an output value that can satisfy the given input requirement.
   * 
   * @param requirement requirement to search for
   * @return the node and exact value specification, or {@code null} if there is none
   */
  public Pair<DependencyNode, ValueSpecification> getNodeSatisfying(final ValueRequirement requirement) {
    final Map<ComputationTargetSpecification, List<Pair<DependencyNode, ValueSpecification>>> targets = _valueRequirement2Specifications.get(requirement.getValueName());
    if (targets == null) {
      return null;
    }
    final List<Pair<DependencyNode, ValueSpecification>> nodes = targets.get(requirement.getTargetSpecification());
    if (nodes == null) {
      return null;
    }
    for (Pair<DependencyNode, ValueSpecification> node : nodes) {
      // Only compare constraints and properties as we know the target and value name match
      if (requirement.getConstraints().isSatisfiedBy(node.getValue().getProperties())) {
        return node;
      }
    }
    return null;
  }

  /**
   * Finds a node which has an output value of the given specification.
   * 
   * @param specification specification to search for
   * @return the node, or {@code null} if there is none
   */
  public DependencyNode getNodeProducing(final ValueSpecification specification) {
    return _specification2DependencyNode.get(specification);
  }

  /**
   * Finds the nodes which have an output value that can satisfy the given input requirement. The nodes are
   * returned in the order they were added to the dependency graph - see [ENG-259] for the implications of
   * handling multiple node returns.
   * 
   * @param requirement requirement to search for
   * @return the nodes and exact value specifications, or {@code null} if there are none
   */
  public Collection<Pair<DependencyNode, ValueSpecification>> getNodesSatisfying(final ValueRequirement requirement) {
    final Map<ComputationTargetSpecification, List<Pair<DependencyNode, ValueSpecification>>> targets = _valueRequirement2Specifications.get(requirement.getValueName());
    if (targets == null) {
      return null;
    }
    final List<Pair<DependencyNode, ValueSpecification>> nodes = targets.get(requirement.getTargetSpecification());
    if (nodes == null) {
      return null;
    }
    List<Pair<DependencyNode, ValueSpecification>> found = null;
    for (Pair<DependencyNode, ValueSpecification> node : nodes) {
      // Only compare constraints and properties as we know the target and value name match
      if (requirement.getConstraints().isSatisfiedBy(node.getValue().getProperties())) {
        if (found == null) {
          found = new LinkedList<Pair<DependencyNode, ValueSpecification>>();
        }
        found.add(node);
      }
    }
    return found;
  }

  public void addDependencyNode(DependencyNode node) {
    ArgumentChecker.notNull(node, "Node");

    _dependencyNodes.add(node);
    _outputValues.addAll(node.getOutputValues());
    _terminalOutputValues.addAll(node.getTerminalOutputValues());
    Pair<ValueRequirement, ValueSpecification> liveData = node.getRequiredLiveData();
    if (liveData != null) {
      _allRequiredLiveData.add(liveData);
    }
    _allComputationTargets.add(node.getComputationTarget().toSpecification());

    for (ValueSpecification output : node.getOutputValues()) {
      final DependencyNode previous = _specification2DependencyNode.put(output, node);
      if (previous != null) {
        throw new IllegalStateException("Node producing " + output + " already in the graph (" + previous + ")");
      }
      Map<ComputationTargetSpecification, List<Pair<DependencyNode, ValueSpecification>>> targets = _valueRequirement2Specifications.get(output.getValueName());
      if (targets == null) {
        targets = new HashMap<ComputationTargetSpecification, List<Pair<DependencyNode, ValueSpecification>>>();
        _valueRequirement2Specifications.put(output.getValueName(), targets);
      }
      List<Pair<DependencyNode, ValueSpecification>> values = targets.get(output.getTargetSpecification());
      if (values == null) {
        values = new LinkedList<Pair<DependencyNode, ValueSpecification>>();
        targets.put(output.getTargetSpecification(), values);
      }
      values.add(Pair.of(node, output));
    }

    Set<DependencyNode> nodesByType = _computationTargetType2DependencyNode.get(node.getComputationTarget().getType());
    if (nodesByType == null) {
      nodesByType = new HashSet<DependencyNode>();
      _computationTargetType2DependencyNode.put(node.getComputationTarget().getType(), nodesByType);
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
   * Marks an output as terminal, meaning that it cannot be pruned.
   * 
   * @param terminalOutput  the output to mark as terminal
   */
  public void addTerminalOutputValue(ValueSpecification terminalOutput) {
    // Register it with the node responsible for producing it - informs the node that the output is required
    _specification2DependencyNode.get(terminalOutput).addTerminalOutputValue(terminalOutput);
    // Maintain a cache of all terminal outputs at the graph level
    _terminalOutputValues.add(terminalOutput);
  }

  /**
   * Go through the entire graph and remove any output values that
   * aren't actually consumed.
   * 
   * Because functions can possibly produce more than their minimal set of values,
   * we need to strip out the ones that aren't actually used after the whole graph
   * is bootstrapped.
   * 
   * Because a backtracking algorithm is used for graph building nodes may remain
   * which generate no output which is used.
   */
  public void removeUnnecessaryValues() {
    final List<DependencyNode> unnecessaryNodes = new LinkedList<DependencyNode>();
    do {
      for (DependencyNode node : _dependencyNodes) {
        Set<ValueSpecification> unnecessaryValues = node.removeUnnecessaryOutputs();
        if (!unnecessaryValues.isEmpty()) {
          s_logger.info("{}: removed {} unnecessary potential result(s)", this, unnecessaryValues.size());
          _outputValues.removeAll(unnecessaryValues);
          if (node.getOutputValues().isEmpty()) {
            unnecessaryNodes.add(node);
          }
        }
        for (ValueSpecification unnecessaryValue : unnecessaryValues) {
          DependencyNode removed = _specification2DependencyNode.remove(unnecessaryValue);
          if (removed == null) {
            throw new IllegalStateException("A value specification " + unnecessaryValue + " wasn't mapped");
          }
        }
      }
      if (unnecessaryNodes.isEmpty()) {
        return;
      }
      s_logger.info("{}: removed {} unnecessary node(s)", this, unnecessaryNodes.size());
      _dependencyNodes.removeAll(unnecessaryNodes);
      for (DependencyNode node : unnecessaryNodes) {
        _allRequiredLiveData.remove(node.getRequiredLiveData());
        _computationTargetType2DependencyNode.get(node.getComputationTarget().getType()).remove(node);
        node.clearInputs();
      }
      unnecessaryNodes.clear();
    } while (true);
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
    if (!containsNode(currentNode)) { // this check is necessary because of sub-graphing
      return;
    }

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

  /**
   * @param subNodes Each node must belong to this graph - 
   * this is not checked in the method for performance reasons
   * @return Sub-graph of the given nodes
   */
  public DependencyGraph subGraph(Collection<DependencyNode> subNodes) {
    DependencyGraph subGraph = new DependencyGraph(getCalcConfName());
    for (DependencyNode node : subNodes) {
      subGraph.addDependencyNode(node);
    }
    return subGraph;
  }

  @Override
  public String toString() {
    return "DependencyGraph[calcConf=" + getCalcConfName() + ",size=" + getSize() + "]";
  }

}
