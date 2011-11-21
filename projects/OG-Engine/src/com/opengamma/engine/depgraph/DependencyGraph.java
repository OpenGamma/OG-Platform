/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static com.opengamma.util.functional.Functional.submapByKeySet;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.tuple.Pair;

/**
 * Represents a directed graph of nodes describing how to execute a view to produce the required terminal outputs.
 */
@PublicAPI
public class DependencyGraph {
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraph.class);

  private final String _calculationConfigurationName;

  /** Includes the root node(s) */
  private final Set<DependencyNode> _dependencyNodes = new HashSet<DependencyNode>();

  private final Set<DependencyNode> _rootNodes = new HashSet<DependencyNode>();

  /**
   * A cache of output values from this graph's nodes
   */
  private final Set<ValueSpecification> _outputSpecifications = new HashSet<ValueSpecification>();

  /**
   * A cache of terminal output values from this graph's nodes
   */
  private final Set<ValueSpecification> _terminalOutputValues = new HashSet<ValueSpecification>();

  private final Map<ValueSpecification, Set<ValueRequirement>> _terminalOutputs = new HashMap<ValueSpecification, Set<ValueRequirement>>();

  /** A map to speed up lookups. Contents are equal to _dependencyNodes. */
  private final Map<ComputationTargetType, Set<DependencyNode>> _computationTargetType2DependencyNode = new HashMap<ComputationTargetType, Set<DependencyNode>>();

  /** A map to speed up lookups. Contents are equal to _dependencyNodes. */
  private final Map<ValueSpecification, DependencyNode> _specification2DependencyNode = new HashMap<ValueSpecification, DependencyNode>();

  private final Map<String, Map<ComputationTargetSpecification, List<Pair<DependencyNode, ValueSpecification>>>> _valueRequirement2Specifications = Maps.newHashMap(); // TODO

  private final Set<Pair<ValueRequirement, ValueSpecification>> _allRequiredMarketData = new HashSet<Pair<ValueRequirement, ValueSpecification>>();
  private final Set<ComputationTarget> _allComputationTargets = new HashSet<ComputationTarget>();

  /**
   * Creates a new, initially empty, dependency graph for the named configuration.
   * 
   * @param calcConfName configuration name, not null
   */
  public DependencyGraph(String calcConfName) {
    ArgumentChecker.notNull(calcConfName, "Calculation configuration name");
    _calculationConfigurationName = calcConfName;
  }

  /**
   * Returns the name of the configuration this graph has been built for.
   * 
   * @return the configuration name
   */
  public String getCalculationConfigurationName() {
    return _calculationConfigurationName;
  }

  /**
   * Returns nodes that have no dependent nodes in this graph.
   * <p>
   * The case of unconnected sub-graphs: say your original graph is
   * <pre>
   *  A->B->C
   * </pre>
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

  /**
   * Tests if the given node is a root node in this graph or sub-graph.
   * 
   * @param node node to test, not null
   * @return true if the node is a root node
   */
  public boolean isRootNode(DependencyNode node) {
    return _rootNodes.contains(node);
  }

  /**
   * Tests if the given node is contained in this graph or sub-graph.
   * 
   * @param node node to test, not null
   * @return true if the node is in his graph or sub-graph
   */
  public boolean containsNode(DependencyNode node) {
    return _dependencyNodes.contains(node);
  }

  /**
   * Returns the set of <strong>all</strong> output values within the graph.
   * 
   * @return the set of output values
   */
  public Set<ValueSpecification> getOutputSpecifications() {
    return Collections.unmodifiableSet(_outputSpecifications);
  }

  /**
   * Returns the set of output values from the graph that are marked as terminal outputs.
   * These are the requested values that drove the graph construction and will not be pruned.
   * Any other output values in the graph are intermediate values required by the functions
   * used to deliver the requested terminal outputs.
   * 
   * @return the set of terminal output values
   */
  public Set<ValueSpecification> getTerminalOutputSpecifications() {
    return Collections.unmodifiableSet(_terminalOutputValues);
  }

  /**
   * Returns the set of output values from the graph that are marked as terminal outputs.
   * These are the requested values that drove the graph construction and will not be pruned.
   * Any other output values in the graph are intermediate values required by the functions
   * used to deliver the requested terminal outputs.
   *
   * @return the set of terminal output values
   */
  public Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputs() {
    return Collections.unmodifiableMap(_terminalOutputs);
  }

  /**
   * Returns the set of all computation targets referenced by nodes in the graph or sub-graph.
   * 
   * @return the set of all computation targets
   */
  public Set<ComputationTarget> getAllComputationTargets() {
    return Collections.unmodifiableSet(_allComputationTargets);
  }

  /**
   * Returns the set of all output values for computation targets of a specific type. For example
   * the output values for all {@link PortfolioNode} targets.
   * 
   * @param type computation target type, not null
   * @return the set of output values
   */
  public Set<ValueSpecification> getOutputSpecifications(ComputationTargetType type) {
    Set<ValueSpecification> outputValues = new HashSet<ValueSpecification>();
    for (ValueSpecification spec : _outputSpecifications) {
      if (spec.getTargetSpecification().getType() == type) {
        outputValues.add(spec);
      }
    }
    return outputValues;
  }

  /**
   * Returns an immutable set of all nodes in the graph. The set is backed by the graph,
   * so structural changes to the graph will be reflected in the returned set.
   * 
   * @return the set of nodes
   */
  public Set<DependencyNode> getDependencyNodes() {
    return Collections.unmodifiableSet(_dependencyNodes);
  }

  /**
   * Returns the number of nodes in the graph or sub-graph.
   * 
   * @return the number of nodes
   */
  public int getSize() {
    return _dependencyNodes.size();
  }

  /**
   * Returns an immutable set of all nodes in the graph for the given target type. The
   * set is backed by the graph so structural changes to the graph will be reflected in the returned
   * set.
   * 
   * @param type computation target type, not null
   * @return the set of nodes
   */
  public Set<DependencyNode> getDependencyNodes(ComputationTargetType type) {
    Set<DependencyNode> nodes = _computationTargetType2DependencyNode.get(type);
    if (nodes == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(nodes);
  }

  /**
   * Returns the set of market data required for successful execution of the graph. Each market data entry is given as
   * a requirement/specification pair. The requirement may be passed to a market data provider. The corresponding
   * specification may be used to add the market data to a computation value cache.
   * 
   * @return the set of market data requirements. 
   */
  public Set<Pair<ValueRequirement, ValueSpecification>> getAllRequiredMarketData() {
    return Collections.unmodifiableSet(_allRequiredMarketData);
  }

  /**
   * Finds a node which has an output value that can satisfy the given input requirement. If there are
   * multiple nodes producing an output which satisfies the requirement, the first one added to the graph
   * is returned.
   * 
   * @param requirement requirement to search for
   * @return the node and exact value specification, null if there is none
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
   * @return the node, null if there is none
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
   * @return the nodes and exact value specifications, null if there are none
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

  /**
   * Adds a node to the graph. A node will be rejected if there is already one in the graph that produces the same
   * output value - indicating a fault in the graph construction algorithm.
   * 
   * @param node node to add, not null
   */
  public void addDependencyNode(DependencyNode node) {
    ArgumentChecker.notNull(node, "Node");

    if (!_dependencyNodes.add(node)) {
      throw new IllegalStateException("Node " + node + " already in the graph");
    }
    _outputSpecifications.addAll(node.getOutputValues());
    _terminalOutputValues.addAll(node.getTerminalOutputValues());
    Pair<ValueRequirement, ValueSpecification> marketData = node.getRequiredMarketData();
    if (marketData != null) {
      _allRequiredMarketData.add(marketData);
    }
    _allComputationTargets.add(node.getComputationTarget());

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
   * Removes a node from the graph.
   * 
   * @param node the node to remove
   */
  public void removeDependencyNode(final DependencyNode node) {
    ArgumentChecker.notNull(node, "node");
    if (!_dependencyNodes.remove(node)) {
      return;
    }
    _outputSpecifications.removeAll(node.getOutputValues());
    _terminalOutputValues.removeAll(node.getTerminalOutputValues());
    final Pair<ValueRequirement, ValueSpecification> marketData = node.getRequiredMarketData();
    if (marketData != null) {
      _allRequiredMarketData.remove(marketData);
    }
    // Note: a target may be shared by multiple nodes so don't remove target from _allComputationTargets - this is wrong in some cases
    for (ValueSpecification output : node.getOutputValues()) {
      _specification2DependencyNode.remove(output);
      final Map<ComputationTargetSpecification, List<Pair<DependencyNode, ValueSpecification>>> targets = _valueRequirement2Specifications.get(output.getValueName());
      final List<Pair<DependencyNode, ValueSpecification>> values = targets.get(output.getTargetSpecification());
      final Iterator<Pair<DependencyNode, ValueSpecification>> valueIterator = values.iterator();
      while (valueIterator.hasNext()) {
        final Pair<DependencyNode, ValueSpecification> value = valueIterator.next();
        if (node.equals(value.getFirst())) {
          valueIterator.remove();
        }
      }
    }
    final Set<DependencyNode> nodesByType = _computationTargetType2DependencyNode.get(node.getComputationTarget().getType());
    nodesByType.remove(node);
    if (_rootNodes.remove(node)) {
      // Some children might become root as a result of removing this node
      for (DependencyNode childNode : node.getInputNodes()) {
        final Set<DependencyNode> dependentNodes = childNode.getDependentNodes();
        boolean isRoot = true;
        for (DependencyNode dependentNode : dependentNodes) {
          if (_dependencyNodes.contains(dependentNode)) {
            isRoot = false;
            break;
          }
        }
        if (isRoot) {
          _rootNodes.add(childNode);
        }
      }
    }
  }

  /**
   * Marks an output as terminal, meaning that it cannot be pruned.
   * @param requirement the output requirement to mark as terminal
   * @param specification the output specification to mark as terminal
   */
  public void addTerminalOutput(ValueRequirement requirement, ValueSpecification specification) {
    // Register it with the node responsible for producing it - informs the node that the output is required
    final DependencyNode node = _specification2DependencyNode.get(specification);
    if (node == null) {
      throw new IllegalArgumentException("No node produces " + specification);
    }
    node.addTerminalOutputValue(specification);
    // Maintain a cache of all terminal outputs at the graph level
    _terminalOutputValues.add(specification);
    Set<ValueRequirement> requirements = _terminalOutputs.get(specification);
    if (requirements == null) {
      requirements = new HashSet<ValueRequirement>();
      _terminalOutputs.put(specification, requirements);
    }
    requirements.add(requirement);
  }

  /**
   * Marks an outputs as terminals, meaning that it cannot be pruned.
   *
   * @param specifications the outputs to mark as terminals
   */
  public void addTerminalOutputs(Map<ValueSpecification, Set<ValueRequirement>> specifications) {
    for (ValueSpecification specification : specifications.keySet()) {
      // Register it with the node responsible for producing it - informs the node that the output is required
      final DependencyNode node = _specification2DependencyNode.get(specification);
      if (node == null) {
        throw new IllegalArgumentException("No node produces " + specification);
      }
      node.addTerminalOutputValue(specification);
      // Maintain a cache of all terminal outputs at the graph level
      _terminalOutputValues.add(specification);
      Set<ValueRequirement> requirements = _terminalOutputs.get(specification);
      if (requirements == null) {
        requirements = new HashSet<ValueRequirement>();
        _terminalOutputs.put(specification, requirements);
      }
      requirements.addAll(specifications.get(specification));
    }
  }

  /**
   * Go through the entire graph and remove any output values that
   * aren't actually consumed.
   * <p>
   * Functions can possibly produce more than their minimal set of values, so
   * we need to strip out the ones that aren't actually used after the whole graph
   * is constructed.
   * <p>
   * When a backtracking algorithm is used for graph building nodes may remain
   * which generate no terminal output. These nodes are also removed.
   */
  public void removeUnnecessaryValues() {
    final List<DependencyNode> unnecessaryNodes = new LinkedList<DependencyNode>();
    do {
      for (DependencyNode node : _dependencyNodes) {
        Set<ValueSpecification> unnecessaryValues = node.removeUnnecessaryOutputs();
        if (!unnecessaryValues.isEmpty()) {
          s_logger.info("{}: removed {} unnecessary potential result(s)", this, unnecessaryValues.size());
          _outputSpecifications.removeAll(unnecessaryValues);
          if (node.getOutputValues().isEmpty()) {
            unnecessaryNodes.add(node);
          }
          for (ValueSpecification unnecessaryValue : unnecessaryValues) {
            DependencyNode removed = _specification2DependencyNode.remove(unnecessaryValue);
            if (removed == null) {
              throw new IllegalStateException("A value specification " + unnecessaryValue + " wasn't mapped");
            }
          }
        }
      }
      if (unnecessaryNodes.isEmpty()) {
        return;
      }
      s_logger.info("{}: removed {} unnecessary node(s)", this, unnecessaryNodes.size());
      _dependencyNodes.removeAll(unnecessaryNodes);
      _rootNodes.removeAll(unnecessaryNodes);
      for (DependencyNode node : unnecessaryNodes) {
        _allRequiredMarketData.remove(node.getRequiredMarketData());
        _computationTargetType2DependencyNode.get(node.getComputationTarget().getType()).remove(node);
        node.clearInputs();
      }
      unnecessaryNodes.clear();
    } while (true);
  }

  /**
   * Orders the nodes into a valid execution sequence suitable for a single thread executor.
   * 
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
   * Applies a filter to the graph to create a sub-graph.
   * 
   * @param filter Tells whether to include node or not
   * @return A sub-graph consisting of all nodes accepted by the filter.
   */
  public DependencyGraph subGraph(DependencyNodeFilter filter) {
    DependencyGraph subGraph = new DependencyGraph(getCalculationConfigurationName());
    for (DependencyNode node : getDependencyNodes()) {
      if (filter.accept(node)) {
        subGraph.addDependencyNode(node);
      }
    }
    subGraph.addTerminalOutputs(submapByKeySet(_terminalOutputs, subGraph.getOutputSpecifications()));
    return subGraph;
  }

  /**
   * Creates a sub-graph containing the given nodes.
   * 
   * @param subNodes Each node must belong to this graph - 
   * this is not checked in the method for performance reasons
   * @return Sub-graph of the given nodes
   */
  public DependencyGraph subGraph(Collection<DependencyNode> subNodes) {
    DependencyGraph subGraph = new DependencyGraph(getCalculationConfigurationName());
    for (DependencyNode node : subNodes) {
      subGraph.addDependencyNode(node);
    }
    subGraph.addTerminalOutputs(submapByKeySet(_terminalOutputs, subGraph.getOutputSpecifications()));
    return subGraph;
  }

  @Override
  public String toString() {
    return "DependencyGraph[calcConf=" + getCalculationConfigurationName() + ",size=" + getSize() + "]";
  }

  public void dumpStructureLGL(final PrintStream out) {
    final Map<DependencyNode, Integer> uid = new HashMap<DependencyNode, Integer>();
    int nextId = 1;
    for (DependencyNode node : getDependencyNodes()) {
      uid.put(node, nextId++);
    }
    for (DependencyNode node : getDependencyNodes()) {
      out.println("# " + (uid.get(node) + " [" + node.getFunction().getFunction().getFunctionDefinition().getUniqueId() + "]").replace(' ', '_'));
      for (DependencyNode input : node.getDependentNodes()) {
        out.println((uid.get(input) + " [" + input.getFunction().getFunction().getFunctionDefinition().getUniqueId() + "]").replace(' ', '_'));
      }
    }
  }

  private void dumpNodeASCII(final PrintStream out, String indent, final DependencyNode node, final Map<DependencyNode, Integer> uidMap, final Set<DependencyNode> visited) {
    out.println(indent + uidMap.get(node) + " " + node);
    visited.add(node);
    indent = indent + "  ";
    for (ValueSpecification input : node.getInputValues()) {
      out.println(indent + "Iv=" + input);
    }
    for (ValueSpecification output : node.getOutputValues()) {
      out.println(indent + "Ov=" + output);
    }
    for (DependencyNode input : node.getInputNodes()) {
      if (!input.getDependentNodes().contains(node)) {
        out.println(indent + "** " + input);
      }
      dumpNodeASCII(out, indent, input, uidMap, visited);
    }
  }

  public void dumpStructureASCII(final PrintStream out) {
    final Map<DependencyNode, Integer> uid = new HashMap<DependencyNode, Integer>();
    int nextId = 1;
    for (DependencyNode node : getDependencyNodes()) {
      uid.put(node, nextId++);
    }
    final Set<DependencyNode> visited = new HashSet<DependencyNode>();
    for (DependencyNode root : _rootNodes) {
      dumpNodeASCII(out, "", root, uid, visited);
    }
    // Nodes disjoint from the tree
    for (DependencyNode node : getDependencyNodes()) {
      if (!visited.contains(node)) {
        dumpNodeASCII(out, "- ", node, uid, visited);
      }
    }
    // Nodes in tree but not in graph
    for (DependencyNode node : visited) {
      if (!containsNode(node)) {
        dumpNodeASCII(out, "+ ", node, uid, visited);
      }
    }
  }

}
