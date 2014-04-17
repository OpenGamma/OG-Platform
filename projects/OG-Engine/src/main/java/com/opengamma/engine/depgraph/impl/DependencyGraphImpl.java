/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.impl;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link DependencyGraph}.
 */
public class DependencyGraphImpl implements DependencyGraph, Serializable {

  // TODO: Change DependencyGraph from an interface to an abstract class and put the static stuff into it rather than have static methods & instanceof checks here

  // TODO: Make ExecutionOrderNodeIterator package visible

  private static final long serialVersionUID = 1L;

  /**
   * The calculation configuration this is the graph for. A view definition may require multiple graphs, one for each configuration.
   */
  private final String _calculationConfigurationName;

  /**
   * The terminal outputs required of the graph. Each is associated with the original value requirements that caused their inclusion in the graph.
   */
  private final Map<ValueSpecification, Set<ValueRequirement>> _terminalOutputs;

  /**
   * The roots of the graph. The full set of nodes in the graph can be found by traversing these nodes.
   */
  private final DependencyNode[] _roots;

  /**
   * The cached size of the graph.
   */
  private final int _size;

  /**
   * The cached hash code.
   */
  private volatile int _hashCode;

  /**
   * Creates a new dependency graph for the named configuration with given roots and terminal outputs.
   * 
   * @param calcConfigName the configuration name, not null
   * @param roots the roots of the graph, not null and not containing null
   * @param size the size of the graph
   * @param terminalOutputs the terminal outputs from the graph, not null and not containing null
   */
  public DependencyGraphImpl(final String calcConfigName, final Collection<DependencyNode> roots, final int size, final Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs) {
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.noNulls(roots, "roots");
    ArgumentChecker.notNull(terminalOutputs, "terminalOutputs");
    _calculationConfigurationName = calcConfigName;
    _terminalOutputs = Maps.newHashMapWithExpectedSize(terminalOutputs.size());
    for (Map.Entry<ValueSpecification, Set<ValueRequirement>> terminalOutput : terminalOutputs.entrySet()) {
      ArgumentChecker.notNull(terminalOutput.getKey(), "terminalOutput.key");
      ArgumentChecker.notNull(terminalOutput.getValue(), "terminalOutput.value");
      _terminalOutputs.put(terminalOutput.getKey(), ImmutableSet.copyOf(terminalOutput.getValue()));
    }
    _roots = roots.toArray(new DependencyNode[roots.size()]);
    _size = size;
  }

  private DependencyGraphImpl(final String calcConfigName, final DependencyNode[] roots, final int size, final Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs) {
    _calculationConfigurationName = calcConfigName;
    _roots = roots;
    _size = size;
    _terminalOutputs = terminalOutputs;
  }

  @Override
  public String getCalculationConfigurationName() {
    return _calculationConfigurationName;
  }

  private static int calculateSize(final DependencyNode node, final Set<DependencyNode> nodes) {
    int count = 1;
    final int inputs = node.getInputCount();
    for (int i = 0; i < inputs; i++) {
      if (nodes.add(node.getInputNode(i))) {
        count += calculateSize(node.getInputNode(i), nodes);
      }
    }
    return count;
  }

  private static int calculateSize(final DependencyNode[] roots) {
    final Set<DependencyNode> nodes = new HashSet<DependencyNode>();
    int size = 0;
    for (DependencyNode root : roots) {
      size += calculateSize(root, nodes);
    }
    return size;
  }

  @Override
  public int getSize() {
    return _size;
  }

  @Override
  public int getRootCount() {
    return _roots.length;
  }

  @Override
  public DependencyNode getRootNode(final int index) {
    return _roots[index];
  }

  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputs() {
    return _terminalOutputs;
  }

  /**
   * Returns the set of terminal outputs produced by a graph.
   * 
   * @param graph the graph to query, not null
   * @return the terminal output set, not null
   */
  public static Set<ValueSpecification> getTerminalOutputSpecifications(final DependencyGraph graph) {
    return graph.getTerminalOutputs().keySet();
  }

  private Map<ValueSpecification, DependencyNode> getAllOutputs() {
    final Map<ValueSpecification, DependencyNode> outputs = new HashMap<ValueSpecification, DependencyNode>();
    for (DependencyNode root : _roots) {
      DependencyNodeImpl.gatherOutputValues(root, outputs);
    }
    return outputs;
  }

  /**
   * Returns all outputs produced within a graph with the node that produces each; this is the total set of all terminal and non-terminal outputs.
   * 
   * @param graph the graph to query, not null
   * @return the full output set as a map of value specification to the node that produces it, not null
   */
  public static Map<ValueSpecification, DependencyNode> getAllOutputs(final DependencyGraph graph) {
    if (graph instanceof DependencyGraphImpl) {
      return ((DependencyGraphImpl) graph).getAllOutputs();
    } else {
      final Map<ValueSpecification, DependencyNode> outputs = new HashMap<ValueSpecification, DependencyNode>();
      final Iterator<DependencyNode> itr = graph.nodeIterator();
      while (itr.hasNext()) {
        final DependencyNode node = itr.next();
        final int count = node.getOutputCount();
        for (int i = 0; i < count; i++) {
          outputs.put(node.getOutputValue(i), node);
        }
      }
      return outputs;
    }
  }

  /**
   * Returns the set of all outputs produced by a graph; this is the total set of all terminal and non-terminal outputs.
   * 
   * @param graph the graph to query, not null
   * @return the full output set, not null
   */
  public static Set<ValueSpecification> getAllOutputSpecifications(final DependencyGraph graph) {
    return getAllOutputs(graph).keySet();
  }

  @Override
  public Iterator<DependencyNode> nodeIterator() {
    // TODO: How often does this get called, and how costly is it? Is it worth creating an array after the iteration so we can do it faster in the future?
    // Cheapest way to iterate is depth-first which happens to be execution order
    return new ExecutionOrderNodeIterator(this);
  }

  /**
   * Returns all of the nodes from a graph.
   * <p>
   * This is provided mainly for tests and compatibility with code prior to major changes made to the {@link DependencyGraph} class. It is unlikely to be efficient in terms of memory or the time taken
   * to construct the collection as the work may be performed twice; once building a set to ensure that each node is visited only once (see {@link #executionOrderIterator}) and again for the returned
   * set.
   * 
   * @param graph the graph to query, not null
   * @return all of the nodes in the graph, not null and not containing null
   */
  public static Collection<DependencyNode> getDependencyNodes(final DependencyGraph graph) {
    final Collection<DependencyNode> nodes = new ArrayList<DependencyNode>(graph.getSize());
    final Iterator<DependencyNode> itr = graph.nodeIterator();
    while (itr.hasNext()) {
      nodes.add(itr.next());
    }
    return nodes;
  }

  /**
   * Returns all of the root nodes from a graph.
   * 
   * @param graph the graph to query, not null
   * @return the root nodes from the graph, not null and not containing null
   */
  public static Set<DependencyNode> getRootNodes(final DependencyGraph graph) {
    final int count = graph.getRootCount();
    final Set<DependencyNode> roots = Sets.newHashSetWithExpectedSize(count);
    for (int i = 0; i < count; i++) {
      roots.add(graph.getRootNode(i));
    }
    return roots;
  }

  /**
   * Returns all of the value specifications that correspond to market-data-sourcing functions at the leaves of the graph.
   * 
   * @param graph the graph to query, not null
   * @return the value specifications, not null and not containing null
   */
  public static Set<ValueSpecification> getMarketData(final DependencyGraph graph) {
    // TODO: CompiledViewCalcConfig has a better signature and implementation. Swap to that instead?
    final Iterator<DependencyNode> itr = graph.nodeIterator();
    final Set<ValueSpecification> marketData = new HashSet<ValueSpecification>();
    while (itr.hasNext()) {
      final DependencyNode node = itr.next();
      if (MarketDataSourcingFunction.UNIQUE_ID.equals(node.getFunction().getFunctionId())) {
        final int count = node.getOutputCount();
        for (int i = 0; i < count; i++) {
          marketData.add(node.getOutputValue(i));
        }
      }
    }
    return marketData;
  }

  @Override
  public String toString() {
    return "DependencyGraph[calcConf=" + getCalculationConfigurationName() + ",nodes=" + getSize() + ",terminals=" + getTerminalOutputs().size() + "]";
  }

  private static DependencyNode[] reverseExecution(final DependencyGraph graph) {
    final Iterator<DependencyNode> forward = new ExecutionOrderNodeIterator(graph);
    final DependencyNode[] backward = new DependencyNode[graph.getSize()];
    for (int i = backward.length - 1; i >= 0; i--) {
      backward[i] = forward.next();
    }
    return backward;
  }

  private static Map<ValueSpecification, DependencyNode> findRoots(final DependencyGraph graph, final Map<ValueSpecification, ?> terminals, final Collection<DependencyNode> roots) {
    final DependencyNode[] nodes = reverseExecution(graph);
    final Map<ValueSpecification, DependencyNode> necessary = Maps.newHashMapWithExpectedSize(nodes.length);
    findNodes: for (DependencyNode node : nodes) { //CSIGNORE
      int count = node.getOutputCount();
      boolean isTerminal = false;
      for (int i = 0; i < count; i++) {
        final ValueSpecification output = node.getOutputValue(i);
        if (necessary.containsKey(output)) {
          // Node produces at least one necessary value; make all of its inputs necessary
          count = node.getInputCount();
          for (i = 0; i < count; i++) {
            necessary.put(node.getInputValue(i), null);
          }
          continue findNodes;
        }
        if (!isTerminal && terminals.containsKey(output)) {
          isTerminal = true;
        }
      }
      if (isTerminal) {
        // Node produces terminal outputs; nothing else is consumed. This is a root with all inputs necessary
        roots.add(node);
        count = node.getInputCount();
        for (int i = 0; i < count; i++) {
          necessary.put(node.getInputValue(i), null);
        }
      }
    }
    for (Map.Entry<ValueSpecification, ?> terminal : terminals.entrySet()) {
      necessary.put(terminal.getKey(), null);
    }
    return necessary;
  }

  /**
   * Removes any unnecessary values from a graph, returning the new graph. A necessary output is one that is marked as terminal or is consumed by a node that produces a necessary value.
   * <p>
   * Graph construction may be based on functions which can produce multiple outputs which may not then be needed. Removing them may make execution more efficient. Similarly an incremental graph build
   * may have fragments of graph from an earlier iteration producing values which are no longer terminal and no longer need to be calculated, making execution more efficient.
   * 
   * @param graph the graph to remove values from, not null
   * @return the new graph object, or the previous graph instance if it only contains necessary values
   */
  public static DependencyGraph removeUnnecessaryValues(final DependencyGraph graph) {
    final int rootCount = graph.getRootCount();
    final Collection<DependencyNode> roots = new ArrayList<DependencyNode>(rootCount);
    final Map<ValueSpecification, DependencyNode> necessary = findRoots(graph, graph.getTerminalOutputs(), roots);
    final DependencyNode[] newRoots = new DependencyNode[roots.size()];
    // Note: this is tightly coupled to how the ExecutionOrderNodeIterator works
    int i = newRoots.length;
    for (DependencyNode root : roots) {
      final DependencyNode newRoot = DependencyNodeImpl.removeUnnecessaryValues(root, necessary);
      assert newRoot != null;
      newRoots[--i] = newRoot;
    }
    if (newRoots.length == rootCount) {
      // Possibly no changes
      boolean same = true;
      for (i = 0; i < rootCount; i++) {
        if (newRoots[i] != graph.getRootNode(i)) {
          same = false;
        }
      }
      if (same) {
        return graph;
      }
    }
    return new DependencyGraphImpl(graph.getCalculationConfigurationName(), newRoots, calculateSize(newRoots), graph.getTerminalOutputs());
  }

  private static void dumpNodeASCII(final PrintStream out, String indent, final DependencyNode node, final Map<DependencyNode, Integer> uidMap) {
    Integer uid = uidMap.get(node);
    if (uid == null) {
      uid = uidMap.size() + 1;
      uidMap.put(node, uid);
      out.println(indent + uid + " " + node);
    } else {
      out.println(indent + uid + " " + node + " ...");
      return;
    }
    indent = indent + "  ";
    int inputs = node.getInputCount();
    for (int i = 0; i < inputs; i++) {
      out.println(indent + "Iv=" + node.getInputValue(i));
    }
    int outputs = node.getOutputCount();
    for (int i = 0; i < outputs; i++) {
      out.println(indent + "Ov=" + node.getOutputValue(i));
    }
    for (int i = 0; i < inputs; i++) {
      dumpNodeASCII(out, indent, node.getInputNode(i), uidMap);
    }
  }

  /**
   * Produces an ASCII representation of a dependency graph.
   * <p>
   * This is provided for diagnostic/debugging purposes only. It must not be used for any form of data interchange or persistence as the formatting may change in future releases without prior notice.
   * 
   * @param graph the graph to dump out, not null
   * @param out the stream to write to, not null
   */
  public static void dumpStructureASCII(final DependencyGraph graph, final PrintStream out) {
    final Map<DependencyNode, Integer> uid = new HashMap<DependencyNode, Integer>(graph.getSize());
    final int count = graph.getRootCount();
    for (int i = 0; i < count; i++) {
      dumpNodeASCII(out, "", graph.getRootNode(i), uid);
    }
  }

  @Override
  public int hashCode() {
    int hc = _hashCode;
    if (hc == 0) {
      hc = ((_calculationConfigurationName.hashCode() * 31) + _terminalOutputs.hashCode()) * 31;
      for (DependencyNode root : _roots) {
        hc += DependencyNode.HASHING_STRATEGY.hashCode(root);
      }
      if (hc == 0) {
        hc = 1;
      }
      _hashCode = hc;
    }
    return hc;
  }

  /**
   * Tests for equality with another graph. Two graphs are equal if they:
   * <ul>
   * <li>Are for the same calculation configuration name;
   * <li>Produce the same set of terminal outputs from the given value requirements; and
   * <li>Have the same set of root nodes, as per {@link DependencyNode#HASHING_STRATEGY}
   * </ul>
   * <p>
   * Note that comparing two graphs in this way can be an expensive operation.
   * 
   * @param o the object to compare to
   * @return true if the object is a graph and equal to this one, false otherwise
   */
  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof DependencyGraph)) {
      return false;
    }
    final DependencyGraph other = (DependencyGraph) o;
    final int roots = _roots.length;
    if (roots != other.getRootCount()) {
      return false;
    }
    if (!_calculationConfigurationName.equals(other.getCalculationConfigurationName())) {
      return false;
    }
    if (!_terminalOutputs.equals(other.getTerminalOutputs())) {
      return false;
    }
    final int[] hashCodesOther = new int[roots];
    for (int i = 0; i < roots; i++) {
      hashCodesOther[i] = DependencyNode.HASHING_STRATEGY.hashCode(other.getRootNode(i));
    }
    final boolean[] matched = new boolean[roots];
    loopThis: for (int i = 0; i < roots; i++) { //CSIGNORE
      final DependencyNode nodeThis = _roots[i];
      final int hashCodeThis = DependencyNode.HASHING_STRATEGY.hashCode(nodeThis);
      for (int j = i; j < roots; j++) {
        if (!matched[j] && (hashCodeThis == hashCodesOther[j]) && DependencyNode.HASHING_STRATEGY.equals(nodeThis, other.getRootNode(j))) {
          matched[j] = true;
          continue loopThis;
        }
      }
      // No match found for one of our entries
      return false;
    }
    // All root nodes were matched
    return true;
  }

}
