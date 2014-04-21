/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Sub-graphing operation that only keeps nodes which:
 * <ul>
 * <li>Pass a filtering test themselves; and
 * <li>Consume only values from nodes that also pass the filtering test
 * <ul>
 */
public abstract class RootDiscardingSubgrapher {

  /**
   * The inclusion state of any given node.
   */
  public static enum NodeState {
    /**
     * The node is excluded from the graph. This means no nodes from the original graph that depended on this node can remain.
     */
    EXCLUDED,
    /**
     * The node is included in the graph as a root node.
     */
    INCLUDED,
    /**
     * The node is included in the graph, and is consumed by at least one other.
     */
    INCLUDED_NON_ROOT
  }

  /**
   * Tests whether the given node passes the filter.
   * <p>
   * This test is performed before any of the input nodes are considered for inclusion.
   * 
   * @param node the node to test, never null
   * @return true if the node passes the filter (and should have its input nodes tested), false otherwise
   */
  protected abstract boolean acceptNode(DependencyNode node);

  private NodeState acceptNode(final DependencyNode node, final Map<DependencyNode, NodeState> accepted) {
    NodeState state = accepted.get(node);
    if (state != null) {
      return state;
    }
    if (!acceptNode(node)) {
      accepted.put(node, NodeState.EXCLUDED);
      return NodeState.EXCLUDED;
    }
    final int count = node.getInputCount();
    boolean inputsAccepted = true;
    for (int i = 0; i < count; i++) {
      final DependencyNode input = node.getInputNode(i);
      if (acceptNode(input, accepted) == NodeState.EXCLUDED) {
        inputsAccepted = false;
      }
    }
    if (inputsAccepted) {
      accepted.put(node, NodeState.INCLUDED);
      for (int i = 0; i < count; i++) {
        accepted.put(node.getInputNode(i), NodeState.INCLUDED_NON_ROOT);
      }
      return NodeState.INCLUDED;
    }
    accepted.put(node, NodeState.EXCLUDED);
    return NodeState.EXCLUDED;
  }

  /**
   * Forms a subgraph of the given graph.
   * 
   * @param roots the root nodes to process, not null
   * @param terminals the terminal outputs to update, not null
   * @param missingRequirements the structure that should receive any requirements that are ejected from the sub-graph, not null
   * @param accepted the acceptance state buffer, not null
   * @return the new graph roots, or null for an empty graph
   */
  public Set<DependencyNode> subGraph(Collection<DependencyNode> roots, final Map<ValueSpecification, Set<ValueRequirement>> terminals, final Set<ValueRequirement> missingRequirements,
      final Map<DependencyNode, NodeState> accepted) {
    Set<DependencyNode> newRoots = new HashSet<DependencyNode>();
    do {
      Set<DependencyNode> possibleRoots = null;
      for (DependencyNode root : roots) {
        final NodeState state = acceptNode(root, accepted);
        if (state == NodeState.INCLUDED) {
          // This root is in the new graph
          newRoots.add(root);
        } else if (state == NodeState.EXCLUDED) {
          // This root isn't in the new graph, but one or more of its inputs might become roots
          if (possibleRoots == null) {
            possibleRoots = new HashSet<DependencyNode>();
          }
          final int inputs = root.getInputCount();
          for (int j = 0; j < inputs; j++) {
            possibleRoots.add(root.getInputNode(j));
          }
        }
      }
      if (possibleRoots != null) {
        roots = possibleRoots;
      } else {
        break;
      }
    } while (true);
    if (newRoots.isEmpty()) {
      return null;
    } else {
      for (Map.Entry<DependencyNode, NodeState> accept : accepted.entrySet()) {
        final DependencyNode node = accept.getKey();
        final int count = node.getOutputCount();
        if (accept.getValue() == NodeState.EXCLUDED) {
          for (int i = 0; i < count; i++) {
            final Set<ValueRequirement> requirements = terminals.remove(node.getOutputValue(i));
            if (requirements != null) {
              missingRequirements.addAll(requirements);
            }
          }
        }
      }
      return newRoots;
    }
  }

  /**
   * Forms a subgraph of the given graph.
   * 
   * @param graph the graph to process, not null
   * @param missingRequirements the structure that should receive any requirements that are ejected from the sub-graph, not null
   * @param accepted the acceptance state buffer, not null
   * @return the subgraph, or null if it would be empty
   */
  public DependencyGraph subGraph(final DependencyGraph graph, final Set<ValueRequirement> missingRequirements, final Map<DependencyNode, NodeState> accepted) {
    final int rootCount = graph.getRootCount();
    Set<DependencyNode> newRoots = new HashSet<DependencyNode>();
    Set<DependencyNode> possibleRoots = null;
    for (int i = 0; i < rootCount; i++) {
      final DependencyNode root = graph.getRootNode(i);
      final NodeState state = acceptNode(root, accepted);
      if (state == NodeState.INCLUDED) {
        // This root is in the new graph
        newRoots.add(root);
      } else {
        assert state == NodeState.EXCLUDED;
        // This root isn't in the new graph, but one or more of its inputs might become roots
        if (possibleRoots == null) {
          possibleRoots = new HashSet<DependencyNode>();
        }
        final int inputs = root.getInputCount();
        for (int j = 0; j < inputs; j++) {
          possibleRoots.add(root.getInputNode(j));
        }
      }
    }
    if (possibleRoots == null) {
      // This gets allocated if any of the roots are absent from the filter, so if unallocated there are no changes to the graph
      return graph;
    }
    while (possibleRoots != null) {
      final Set<DependencyNode> roots = possibleRoots;
      possibleRoots = null;
      for (DependencyNode root : roots) {
        final NodeState state = acceptNode(root, accepted);
        if (state == NodeState.INCLUDED) {
          // This root is in the new graph
          newRoots.add(root);
        } else if (state == NodeState.EXCLUDED) {
          // This root isn't in the new graph, but one or more of its inputs might become roots
          if (possibleRoots == null) {
            possibleRoots = new HashSet<DependencyNode>();
          }
          final int inputs = root.getInputCount();
          for (int j = 0; j < inputs; j++) {
            possibleRoots.add(root.getInputNode(j));
          }
        }
      }
    }
    if (newRoots.isEmpty()) {
      return null;
    } else {
      final Map<ValueSpecification, Set<ValueRequirement>> oldTerminals = graph.getTerminalOutputs();
      final Map<ValueSpecification, Set<ValueRequirement>> terminals = Maps.newHashMapWithExpectedSize(oldTerminals.size());
      int size = 0;
      for (Map.Entry<DependencyNode, NodeState> accept : accepted.entrySet()) {
        final DependencyNode node = accept.getKey();
        final int count = node.getOutputCount();
        if (accept.getValue() == NodeState.EXCLUDED) {
          for (int i = 0; i < count; i++) {
            final ValueSpecification value = node.getOutputValue(i);
            final Set<ValueRequirement> terminal = oldTerminals.get(value);
            if (terminal != null) {
              missingRequirements.addAll(terminal);
            }
          }
        } else {
          for (int i = 0; i < count; i++) {
            final ValueSpecification value = node.getOutputValue(i);
            final Set<ValueRequirement> terminal = oldTerminals.get(value);
            if (terminal != null) {
              terminals.put(value, terminal);
            }
          }
          size++;
        }
      }
      return new DependencyGraphImpl(graph.getCalculationConfigurationName(), newRoots, size, terminals);
    }
  }

  /**
   * Forms a subgraph of the given graph.
   * 
   * @param roots the root nodes to process, not null
   * @param terminals the terminal outputs to update, not null
   * @param missingRequirements the structure that should receive any requirements that are ejected from the sub-graph, not null
   * @return the new graph roots, or null for an empty graph
   */
  public Set<DependencyNode> subGraph(final Collection<DependencyNode> roots, final Map<ValueSpecification, Set<ValueRequirement>> terminals, final Set<ValueRequirement> missingRequirements) {
    return subGraph(roots, terminals, missingRequirements, new HashMap<DependencyNode, NodeState>());
  }

  /**
   * Forms a subgraph of the given graph.
   * 
   * @param graph the graph to process, not null
   * @param missingRequirements the structure that should receive any requirements that are ejected from the sub-graph, not null
   * @return the subgraph, or null if it would be empty
   */
  public DependencyGraph subGraph(final DependencyGraph graph, final Set<ValueRequirement> missingRequirements) {
    return subGraph(graph, missingRequirements, Maps.<DependencyNode, NodeState>newHashMapWithExpectedSize(graph.getSize()));
  }

}
