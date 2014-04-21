/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Default engine-side implementation of {@link DependencyGraphExplorer}
 */
public class DependencyGraphExplorerImpl implements DependencyGraphExplorer {

  private final DependencyGraph _graph;
  private volatile Map<ValueSpecification, DependencyNode> _nodesBySpecification;
  private volatile Set<ComputationTargetSpecification> _allTargets;

  public DependencyGraphExplorerImpl(DependencyGraph graph) {
    ArgumentChecker.notNull(graph, "graph");
    _graph = graph;
  }

  private Map<ValueSpecification, DependencyNode> getNodesBySpecification() {
    if (_nodesBySpecification == null) {
      _nodesBySpecification = DependencyGraphImpl.getAllOutputs(_graph);
    }
    return _nodesBySpecification;
  }

  @Override
  public String getCalculationConfigurationName() {
    return _graph.getCalculationConfigurationName();
  }

  @Override
  public DependencyGraph getWholeGraph() {
    return _graph;
  }

  private static int terminalOutputSubset(final Map<ValueSpecification, Set<ValueRequirement>> allTerminals, final DependencyNode node, final Set<DependencyNode> visited,
      final Map<ValueSpecification, Set<ValueRequirement>> subsetTerminals) {
    if (visited.add(node)) {
      int count = node.getInputCount();
      int size = 1;
      for (int i = 0; i < count; i++) {
        size += terminalOutputSubset(allTerminals, node.getInputNode(i), visited, subsetTerminals);
      }
      count = node.getOutputCount();
      for (int i = 0; i < count; i++) {
        final Set<ValueRequirement> terminals = allTerminals.get(node.getOutputValue(i));
        if (terminals != null) {
          subsetTerminals.put(node.getOutputValue(i), terminals);
        }
      }
      return size;
    } else {
      return 0;
    }
  }

  @Override
  public DependencyGraphExplorer getSubgraphProducing(final ValueSpecification output) {
    final DependencyNode terminalNode = getNodeProducing(output);
    if (terminalNode == null) {
      return null;
    }
    final Set<DependencyNode> visited = new HashSet<DependencyNode>();
    final Map<ValueSpecification, Set<ValueRequirement>> terminals = new HashMap<ValueSpecification, Set<ValueRequirement>>();
    final int nodes = terminalOutputSubset(_graph.getTerminalOutputs(), terminalNode, visited, terminals);
    return new DependencyGraphExplorerImpl(new DependencyGraphImpl(_graph.getCalculationConfigurationName(), Collections.singleton(terminalNode), nodes, terminals));
  }

  @Override
  public DependencyNode getNodeProducing(final ValueSpecification output) {
    return getNodesBySpecification().get(output);
  }

  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputs() {
    return _graph.getTerminalOutputs();
  }

  @Override
  public Set<ComputationTargetSpecification> getComputationTargets() {
    Set<ComputationTargetSpecification> targets = _allTargets;
    if (targets == null) {
      targets = new HashSet<ComputationTargetSpecification>();
      final Iterator<DependencyNode> itr = _graph.nodeIterator();
      while (itr.hasNext()) {
        targets.add(itr.next().getTarget());
      }
      _allTargets = targets;
    }
    return targets;
  }

}
