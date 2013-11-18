/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.DependencyGraphExplorerImpl;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Remote implementation of {@link DependencyGraphExplorer}.
 */
public class RemoteDependencyGraphExplorer implements DependencyGraphExplorer {

  private final URI _baseUri;
  private final FudgeRestClient _client;

  private volatile DependencyGraphExplorer _local;

  public RemoteDependencyGraphExplorer(URI baseUri) {
    _baseUri = baseUri;
    _client = FudgeRestClient.create();
  }

  private DependencyGraphExplorer getLocal() {
    DependencyGraphExplorer local = _local;
    if (local == null) {
      final URI uriWholeGraph = DataDependencyGraphExplorerResource.uriWholeGraph(_baseUri);
      local = new DependencyGraphExplorerImpl(_client.accessFudge(uriWholeGraph).get(DependencyGraph.class));
      _local = local;
    }
    return local;
  }

  @Override
  public String getCalculationConfigurationName() {
    final DependencyGraphExplorer local = _local;
    if (local != null) {
      return local.getCalculationConfigurationName();
    }
    // TODO: Make this call over the network
    return getWholeGraph().getCalculationConfigurationName();
  }

  @Override
  public DependencyGraph getWholeGraph() {
    return getLocal().getWholeGraph();
  }

  private DependencyGraph getSubgraphProducingImpl(final ValueSpecification output) {
    final URI uri = DataDependencyGraphExplorerResource.uriSubgraph(_baseUri, output);
    return _client.accessFudge(uri).get(DependencyGraph.class);
  }

  @Override
  public DependencyGraphExplorer getSubgraphProducing(final ValueSpecification output) {
    final DependencyGraphExplorer local = _local;
    if (local != null) {
      return local.getSubgraphProducing(output);
    }
    final DependencyGraph graph = getSubgraphProducingImpl(output);
    if (graph != null) {
      return new DependencyGraphExplorerImpl(graph);
    } else {
      return null;
    }
  }

  @Override
  public DependencyNode getNodeProducing(final ValueSpecification output) {
    final DependencyGraphExplorer local = _local;
    if (local != null) {
      return local.getNodeProducing(output);
    }
    // TODO: Make this call over the network
    final DependencyGraph graph = getSubgraphProducingImpl(output);
    if (graph != null) {
      if (graph.getRootCount() == 1) {
        return graph.getRootNode(0);
      }
    }
    return null;
  }

  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputs() {
    final DependencyGraphExplorer local = _local;
    if (local != null) {
      return local.getTerminalOutputs();
    }
    // TODO: Make this call over the network
    return getWholeGraph().getTerminalOutputs();
  }

  @Override
  public Set<ComputationTargetSpecification> getComputationTargets() {
    // TODO: Make this call over the network
    return getLocal().getComputationTargets();
  }

}
