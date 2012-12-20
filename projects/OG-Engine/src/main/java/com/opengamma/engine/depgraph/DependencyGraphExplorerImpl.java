/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Default engine-side implementation of {@link DependencyGraphExplorer}
 */
public class DependencyGraphExplorerImpl implements DependencyGraphExplorer {

  private final DependencyGraph _graph;
  
  public DependencyGraphExplorerImpl(DependencyGraph graph) {
    ArgumentChecker.notNull(graph, "graph");
    _graph = graph;
  }
  
  @Override
  public DependencyGraph getWholeGraph() {
    return _graph;
  }
  
  @Override
  public DependencyGraph getSubgraphProducing(ValueSpecification output) {
    DependencyNode terminalNode = _graph.getNodeProducing(output);
    if (terminalNode == null) {
      return null;
    }
    Set<DependencyNode> subgraphNodes = new HashSet<DependencyNode>();
    addInputNodes(terminalNode, subgraphNodes);
    return _graph.subGraph(subgraphNodes);
  }
  
  private void addInputNodes(DependencyNode node, Collection<DependencyNode> nodes) {
    nodes.add(node);
    for (DependencyNode inputNode : node.getInputNodes()) {
      addInputNodes(inputNode, nodes);
    }
  }
  
}
