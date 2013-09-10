/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static com.opengamma.lambdava.streams.Lambdava.submapByKeySet;

import java.io.PrintStream;
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

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Represents a directed, acyclic, graph of nodes describing how to execute a view to produce the required terminal outputs.
 * <p>
 * Code using a graph instance may assume that it is well formed - that is, the nodes are linked to each other in a consistent fashion and no cycles are present. It is the responsibility of code that
 * creates (or modifies) graphs to ensure that invalid graphs are not produced as the presence of such a graph would break other components.
 */
@PublicAPI
public class DependencyGraph {

  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraph.class);

  private final String _calculationConfigurationName;

  /**
   * All nodes in the graph, including the root nodes.
   */
  private final Set<DependencyNode> _dependencyNodes = new HashSet<>();

  /**
   * The root nodes in the graph.
   */
  private final Set<DependencyNode> _rootNodes = new HashSet<>();

  /**
   * A cache of terminal output values from this graph's nodes. Each output may be associated with one or more original value requirements that triggered the outputs inclusion in the graph.
   */
  private final Map<ValueSpecification, Set<ValueRequirement>> _terminalOutputs = new HashMap<>();

  /**
   * A cache of output values from this graph's nodes. Each output is associated with the node that produces it.
   */
  private final Map<ValueSpecification, DependencyNode> _outputValues = new HashMap<>();

  private final Set<ValueSpecification> _allRequiredMarketData = new HashSet<>();

  private final Set<ComputationTargetSpecification> _allComputationTargets = new HashSet<>();

  /**
   * Creates a new, initially empty, dependency graph for the named configuration.
   * 
   * @param calcConfName configuration name, not null
   */
  public DependencyGraph(final String calcConfName) {
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
   * 
   * <pre>
   *  A->B->C
   * </pre>
   * 
   * and your subgraph contains only nodes A and C, then according to the above definition (no dependent nodes), both A and C are considered to be root. Of course unconnected sub-graphs should not
   * occur in practice.
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
  public boolean isRootNode(final DependencyNode node) {
    return _rootNodes.contains(node);
  }

  /**
   * Tests if the given node is contained in this graph or sub-graph.
   * 
   * @param node node to test, not null
   * @return true if the node is in his graph or sub-graph
   */
  public boolean containsNode(final DependencyNode node) {
    return _dependencyNodes.contains(node);
  }

  /**
   * Returns the set of <strong>all</strong> output values within the graph.
   * 
   * @return the set of output values
   */
  public Set<ValueSpecification> getOutputSpecifications() {
    return Collections.unmodifiableSet(_outputValues.keySet());
  }

  /**
   * Returns the set of output values from the graph that are marked as terminal outputs. These are the requested values that drove the graph construction and will not be pruned. Any other output
   * values in the graph are intermediate values required by the functions used to deliver the requested terminal outputs.
   * 
   * @return the set of terminal output values
   */
  public Set<ValueSpecification> getTerminalOutputSpecifications() {
    return Collections.unmodifiableSet(_terminalOutputs.keySet());
  }

  /**
   * Returns the set of output values from the graph that are marked as terminal outputs. These are the requested values that drove the graph construction and will not be pruned. Any other output
   * values in the graph are intermediate values required by the functions used to deliver the requested terminal outputs.
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
  public Set<ComputationTargetSpecification> getAllComputationTargets() {
    return Collections.unmodifiableSet(_allComputationTargets);
  }

  /**
   * Returns the set of all output values for computation targets of a specific type. For example the output values for all {@link PortfolioNode} targets.
   * 
   * @param type computation target type, not null
   * @return the set of output values
   */
  public Set<ValueSpecification> getOutputSpecifications(final ComputationTargetType type) {
    // REVIEW 2012-05-24 aiwg -- Do we really need this? It's only used by some of the unit tests.
    final Set<ValueSpecification> outputValues = new HashSet<>();
    for (final ValueSpecification spec : _outputValues.keySet()) {
      if (spec.getTargetSpecification().getType() == type) {
        outputValues.add(spec);
      }
    }
    return outputValues;
  }

  /**
   * Returns an immutable set of all nodes in the graph. The set is backed by the graph, so structural changes to the graph will be reflected in the returned set.
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
   * Returns the set of market data required for successful execution of the graph. These correspond to the leaf nodes of the graph and can be queried from a market data provider.
   * 
   * @return the set of market data requirements.
   */
  public Set<ValueSpecification> getAllRequiredMarketData() {
    return Collections.unmodifiableSet(_allRequiredMarketData);
  }

  /**
   * Finds a node which has an output value of the given specification.
   * 
   * @param specification specification to search for
   * @return the node, null if there is none
   */
  public DependencyNode getNodeProducing(final ValueSpecification specification) {
    return _outputValues.get(specification);
  }

  /**
   * Adds a node to the graph. A node will be rejected if there is already one in the graph that produces the same output value - indicating a fault in the graph construction algorithm.
   * 
   * @param node node to add, not null
   */
  public void addDependencyNode(final DependencyNode node) {
    ArgumentChecker.notNull(node, "Node");
    if (!_dependencyNodes.add(node)) {
      throw new IllegalStateException("Node " + node + " already in the graph");
    }
    node.gatherTerminalOutputValues(_terminalOutputs);
    node.addMarketDataRequirementsInto(_allRequiredMarketData);
    _allComputationTargets.add(node.getComputationTarget());
    for (final ValueSpecification output : node.getOutputValues()) {
      final DependencyNode previous = _outputValues.put(output, node);
      if (previous != null) {
        throw new IllegalStateException("Node producing " + output + " already in the graph (previus = " + previous + ", this node = " + node + ")");
      }
    }
    // is this node root at the moment?
    boolean isRoot = true;
    for (final DependencyNode dependentNode : node.getDependentNodes()) {
      if (_dependencyNodes.contains(dependentNode)) {
        isRoot = false;
        break;
      }
    }
    if (isRoot) {
      _rootNodes.add(node);
    }
    // might be that some children became non-root as a result of adding this node
    for (final DependencyNode childNode : node.getInputNodes()) {
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
    node.removeMarketDataRequirementsFrom(_allRequiredMarketData);
    for (final ValueSpecification output : node.getOutputValues()) {
      _outputValues.remove(output);
      _terminalOutputs.remove(output);
    }
    if (_rootNodes.remove(node)) {
      // Some children might become root as a result of removing this node
      for (final DependencyNode childNode : node.getInputNodes()) {
        final Set<DependencyNode> dependentNodes = childNode.getDependentNodes();
        boolean isRoot = true;
        for (final DependencyNode dependentNode : dependentNodes) {
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
   * Do not call directly; used by {@link DependencyNode#replaceWithinGraph}.
   */
  /* package */void replaceValueSpecification(final ValueSpecification oldSpec, final ValueSpecification newSpec) {
    _outputValues.put(newSpec, _outputValues.remove(oldSpec));
    final Set<ValueRequirement> reqs = _terminalOutputs.remove(oldSpec);
    if (reqs != null) {
      _terminalOutputs.put(newSpec, reqs);
    }
  }

  /**
   * Replaces a node in the dependency graph with a new node based on the replacement target. The new target must be compatible with the original.
   * 
   * @param node the node to replace
   * @param newTarget the target for the new node
   * @return the new node
   */
  public DependencyNode replaceNode(final DependencyNode node, final ComputationTargetSpecification newTarget) {
    ArgumentChecker.notNull(node, "node");
    ArgumentChecker.notNull(newTarget, "newTarget");
    if (!_dependencyNodes.remove(node)) {
      throw new IllegalStateException("Node " + node + " is not in graph");
    }
    final DependencyNode newNode = new DependencyNode(newTarget);
    newNode.setFunction(node.getFunction());
    node.replaceWithinGraph(newNode, this);
    _allComputationTargets.add(newTarget);
    if (_rootNodes.remove(node)) {
      _rootNodes.add(newNode);
    }
    _dependencyNodes.add(newNode);
    return newNode;
  }

  /**
   * Creates a new node with the same definition as the specified node, which is automatically placed in the dependency graph such that it effectively proxies the original node. It will takes its
   * inputs from the outputs of the original node and it will expose the same output specifications along with the same set of dependents. Meanwhile the original node will be adjusted such that its
   * only dependent is the new node. As each node must produce a unique ValueSpecification, additional properties are added to the value spec produced by the new node to maintain this uniqueness.
   * 
   * @param original the node to be proxied, not equal and not this node
   * @param function the function for the new node, not null
   * @param discriminatorProperties properties added to the value spec of the original node, such that the new node produces a unique value spec, not null
   * @return the newly created proxy node, not null
   */
  public DependencyNode appendInput(final DependencyNode original, final CompiledFunctionDefinition function, final Map<String, String> discriminatorProperties) {
    ArgumentChecker.notNull(original, "node");
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notEmpty(discriminatorProperties, "discriminatorProperties");
    // Create the new proxy node based on the original
    final DependencyNode proxyNode = new DependencyNode(original.getComputationTarget());
    proxyNode.setFunction(function);
    // TODO - this implementation is naive as it proxies all output specs - we should actually only proxy the spec we are interested in
    // However, in most cases there will only be one output anyway
    final Map<ValueSpecification, ValueSpecification> newValueSpecifications = copyValueSpecifications(original, discriminatorProperties);
    proxyNode.addOutputValues(newValueSpecifications.values());
    // Note the dependents of the original
    Set<DependencyNode> originalDependents = new HashSet<>(original.getDependentNodes());
    // Now switch the inputs for each of the dependents
    for (DependencyNode dependent : originalDependents) {
      for (Map.Entry<ValueSpecification, ValueSpecification> entry : newValueSpecifications.entrySet()) {
        dependent.replaceInput(entry.getKey(), entry.getValue(), original, proxyNode);
      }
    }
    // Now the input values
    proxyNode.addInputNode(original);
    for (ValueSpecification specification : original.getOutputValues()) {
      proxyNode.addInputValue(specification);
    }
    addDependencyNode(proxyNode);
    return proxyNode;
  }

  private Map<ValueSpecification, ValueSpecification> copyValueSpecifications(final DependencyNode node, final Map<String, String> discriminatorProperties) {
    Map<ValueSpecification, ValueSpecification> converted = new HashMap<>();
    for (ValueSpecification original : node.getOutputValues()) {
      ValueProperties.Builder builder = original.getProperties().copy();
      for (Map.Entry<String, String> entry : discriminatorProperties.entrySet()) {
        builder = builder.with(entry.getKey(), entry.getValue());
      }
      converted.put(original, new ValueSpecification(original.getValueName(), original.getTargetSpecification(), builder.get()));
    }
    return converted;
  }

  private Set<ValueRequirement> getTerminalOutputValueRequirements(final ValueSpecification specification) {
    final DependencyNode node = _outputValues.get(specification);
    if (node == null) {
      throw new IllegalArgumentException("No node produces " + specification);
    }
    node.addTerminalOutputValue(specification);
    Set<ValueRequirement> requirements = _terminalOutputs.get(specification);
    if (requirements == null) {
      requirements = new HashSet<>();
      _terminalOutputs.put(specification, requirements);
    }
    return requirements;
  }

  /**
   * Marks an output as terminal, meaning that it cannot be pruned.
   * <p>
   * Terminal outputs are marked on each node, and a cache is held at the graph level for the specifications against the original requirements that requested them.
   * 
   * @param requirement the output requirement to mark as terminal
   * @param specification the output specification to mark as terminal
   */
  public void addTerminalOutput(final ValueRequirement requirement, final ValueSpecification specification) {
    getTerminalOutputValueRequirements(specification).add(requirement);
  }

  /**
   * Marks an outputs as terminals, meaning that it cannot be pruned.
   * <p>
   * Terminal outputs are marked on each node, and a cache is held at the graph level for the specifications against the original requirements that requested them.
   * 
   * @param specifications the outputs to mark as terminals
   */
  public void addTerminalOutputs(final Map<ValueSpecification, Set<ValueRequirement>> specifications) {
    for (Map.Entry<ValueSpecification, Set<ValueRequirement>> specification : specifications.entrySet()) {
      getTerminalOutputValueRequirements(specification.getKey()).addAll(specification.getValue());
    }
  }

  /**
   * Unmarks an output as terminal. The output and node producing it are kept in the graph but can be removed if needed by calling {@link #removeUnnecessaryValues}. If the output was terminal because
   * it satisfies other requirements only the stated mappings are removed, it will remain terminal with the remaining requirements.
   * 
   * @param requirements the requirements associated with the output, not null
   * @param specification the specification of the output value, not null
   */
  public void removeTerminalOutputs(final Collection<ValueRequirement> requirements, final ValueSpecification specification) {
    final DependencyNode node = _outputValues.get(specification);
    if (node == null) {
      throw new IllegalArgumentException("No node produces " + specification);
    }
    node.removeTerminalOutputValue(specification);
    // Maintain the cache of all terminal outputs
    final Set<ValueRequirement> terminalRequirements = _terminalOutputs.get(specification);
    if (terminalRequirements != null) {
      terminalRequirements.removeAll(requirements);
      if (terminalRequirements.isEmpty()) {
        _terminalOutputs.remove(specification);
      }
    }
  }

  /**
   * Go through the entire graph and remove any output values that aren't actually consumed.
   * <p>
   * Functions can possibly produce more than their minimal set of values, so we need to strip out the ones that aren't actually used after the whole graph is constructed.
   * <p>
   * When a backtracking algorithm is used for graph building nodes may remain which generate no terminal output. These nodes are also removed.
   */
  public void removeUnnecessaryValues() {
    final List<DependencyNode> unnecessaryNodes = new LinkedList<>();
    do {
      for (final DependencyNode node : _dependencyNodes) {
        final Set<ValueSpecification> unnecessaryValues = node.removeUnnecessaryOutputs();
        if (unnecessaryValues == null) {
          continue;
        }
        if (!unnecessaryValues.isEmpty()) {
          s_logger.info("{}: removed {} unnecessary potential result(s)", this, unnecessaryValues.size());
          for (final ValueSpecification unnecessaryValue : unnecessaryValues) {
            final DependencyNode removed = _outputValues.remove(unnecessaryValue);
            if (removed == null) {
              throw new IllegalStateException("A value specification " + unnecessaryValue + " wasn't mapped to a node");
            }
            _allRequiredMarketData.remove(unnecessaryValue);
          }
        }
        if (node.getOutputValues().isEmpty()) {
          unnecessaryNodes.add(node);
        }
      }
      if (unnecessaryNodes.isEmpty()) {
        return;
      }
      s_logger.info("{}: removed {} unnecessary node(s)", this, unnecessaryNodes.size());
      _dependencyNodes.removeAll(unnecessaryNodes);
      _rootNodes.removeAll(unnecessaryNodes);
      for (final DependencyNode node : unnecessaryNodes) {
        node.clearInputs();
      }
      unnecessaryNodes.clear();
    } while (true);
  }

  /**
   * Orders the nodes into a valid execution sequence suitable for a single thread executor.
   * 
   * @return Nodes in an executable order. E.g., if there are two nodes, A and B, and A depends on B, then list [B, A] is returned (and not [A, B]).
   */
  public List<DependencyNode> getExecutionOrder() {
    final ArrayList<DependencyNode> executionOrder = new ArrayList<>();
    final HashSet<DependencyNode> alreadyEvaluated = new HashSet<>();
    for (final DependencyNode root : getRootNodes()) {
      getExecutionOrder(root, executionOrder, alreadyEvaluated);
    }
    return executionOrder;
  }

  private void getExecutionOrder(final DependencyNode currentNode, final List<DependencyNode> executionOrder, final Set<DependencyNode> alreadyEvaluated) {
    if (!containsNode(currentNode)) { // this check is necessary because of sub-graphing
      return;
    }
    for (final DependencyNode child : currentNode.getInputNodes()) {
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
  public DependencyGraph subGraph(final DependencyNodeFilter filter) {
    final DependencyGraph subGraph = new DependencyGraph(getCalculationConfigurationName());
    for (final DependencyNode node : getDependencyNodes()) {
      if (filter.accept(node)) {
        subGraph.addDependencyNode(node);
        for (ValueSpecification terminalOutput : node.getTerminalOutputValues()) {
          if (_terminalOutputs.containsKey(terminalOutput)) {
            if (_terminalOutputs.get(terminalOutput) == null) {
              System.err.println("This is bad");
            }
          } else {
            System.err.println("This is very bad");
          }
        }
      }
    }
    subGraph.addTerminalOutputs(submapByKeySet(_terminalOutputs, subGraph.getTerminalOutputSpecifications()));
    return subGraph;
  }

  /**
   * Creates a sub-graph containing the given nodes.
   * 
   * @param subNodes Each node must belong to this graph - this is not checked in the method for performance reasons
   * @return Sub-graph of the given nodes
   */
  public DependencyGraph subGraph(final Collection<DependencyNode> subNodes) {
    final DependencyGraph subGraph = new DependencyGraph(getCalculationConfigurationName());
    for (final DependencyNode node : subNodes) {
      subGraph.addDependencyNode(node);
    }
    subGraph.addTerminalOutputs(submapByKeySet(_terminalOutputs, subGraph.getTerminalOutputSpecifications()));
    return subGraph;
  }

  @Override
  public String toString() {
    return "DependencyGraph[calcConf=" + getCalculationConfigurationName() + ",nodes=" + getSize() + ",terminals=" + getTerminalOutputs().size() + "]";
  }

  public void dumpStructureLGL(final PrintStream out) {
    final Map<DependencyNode, Integer> uid = new HashMap<>();
    int nextId = 1;
    for (final DependencyNode node : getDependencyNodes()) {
      uid.put(node, nextId++);
    }
    for (final DependencyNode node : getDependencyNodes()) {
      out.println("# " + (uid.get(node) + " [" + node.getFunction().getFunction().getFunctionDefinition().getUniqueId() + "]").replace(' ', '_'));
      for (final DependencyNode input : node.getDependentNodes()) {
        out.println((uid.get(input) + " [" + input.getFunction().getFunction().getFunctionDefinition().getUniqueId() + "]").replace(' ', '_'));
      }
    }
  }

  private void dumpNodeASCII(final PrintStream out, String indent, final DependencyNode node, final Map<DependencyNode, Integer> uidMap, final Set<DependencyNode> visited) {
    out.println(indent + uidMap.get(node) + " " + node);
    if (visited.add(node)) {
      indent = indent + "  ";
      for (final ValueSpecification input : node.getInputValues()) {
        out.println(indent + "Iv=" + input);
      }
      for (final ValueSpecification output : node.getOutputValues()) {
        out.println(indent + "Ov=" + output);
      }
      for (final DependencyNode input : node.getInputNodes()) {
        if (!input.getDependentNodes().contains(node)) {
          out.println(indent + "** " + input);
        }
        dumpNodeASCII(out, indent, input, uidMap, visited);
      }
    } else {
      out.println(indent + "  ...");
    }
  }

  public void dumpStructureASCII(final PrintStream out) {
    final Map<DependencyNode, Integer> uid = new HashMap<>();
    int nextId = 1;
    for (final DependencyNode node : getDependencyNodes()) {
      uid.put(node, nextId++);
    }
    final Set<DependencyNode> visited = new HashSet<>();
    for (final DependencyNode root : _rootNodes) {
      dumpNodeASCII(out, "", root, uid, visited);
    }
    // Nodes disjoint from the tree
    for (final DependencyNode node : getDependencyNodes()) {
      if (!visited.contains(node)) {
        dumpNodeASCII(out, "- ", node, uid, visited);
      }
    }
    // Nodes in tree but not in graph
    for (final DependencyNode node : visited) {
      if (!containsNode(node)) {
        dumpNodeASCII(out, "+ ", node, uid, visited);
      }
    }
  }

}
