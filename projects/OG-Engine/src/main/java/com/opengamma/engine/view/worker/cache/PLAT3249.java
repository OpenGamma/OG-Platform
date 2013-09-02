/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.Profiler;

/**
 * Temporary measure to allow a dependency graph to be deep-cloned until it either supports it directly, or the structure revised so that it is suitably immutable for sharing between view processes.
 * <p>
 * This should be used whenever a cached compilation has been retrieved so that the calling thread has it's own copy of the data structures which it may then modify.
 */
public final class PLAT3249 {

  private static final Logger s_logger = LoggerFactory.getLogger(PLAT3249.class);
  private static final Profiler s_profiler = Profiler.create(PLAT3249.class);

  private final Map<DependencyNode, DependencyNode> _originalToNew = new HashMap<DependencyNode, DependencyNode>();

  private PLAT3249() {
  }

  private DependencyNode getOrCreateCopy(DependencyNode node) {
    DependencyNode newNode = _originalToNew.get(node);
    if (newNode == null) {
      newNode = new DependencyNode(node);
      _originalToNew.put(node, newNode);
      for (DependencyNode inputNode : node.getInputNodes()) {
        newNode.addInputNode(getOrCreateCopy(inputNode));
      }
    }
    return newNode;
  }

  private DependencyGraph copy(final DependencyGraph copyFrom) {
    final DependencyGraph copyTo = new DependencyGraph(copyFrom.getCalculationConfigurationName());
    for (DependencyNode node : copyFrom.getDependencyNodes()) {
      copyTo.addDependencyNode(getOrCreateCopy(node));
    }
    copyTo.addTerminalOutputs(copyFrom.getTerminalOutputs());
    return copyTo;
  }

  private Collection<DependencyGraph> copyGraphs(final Collection<DependencyGraphExplorer> copyFrom) {
    final Collection<DependencyGraph> copyTo = new ArrayList<DependencyGraph>(copyFrom.size());
    for (DependencyGraphExplorer graph : copyFrom) {
      copyTo.add(copy(graph.getWholeGraph()));
    }
    return copyTo;
  }

  private CompiledViewDefinitionWithGraphs copy(final CompiledViewDefinitionWithGraphs copyFrom) {
    return new CompiledViewDefinitionWithGraphsImpl(copyFrom.getResolverVersionCorrection(), copyFrom.getCompilationIdentifier(), copyFrom.getViewDefinition(),
        copyGraphs(copyFrom.getDependencyGraphExplorers()), new HashMap<ComputationTargetReference, UniqueId>(copyFrom.getResolvedIdentifiers()), copyFrom.getPortfolio(),
        ((CompiledViewDefinitionWithGraphsImpl) copyFrom).getFunctionInitId(), copyFrom.getCompiledCalculationConfigurations());
  }

  public static CompiledViewDefinitionWithGraphs deepClone(final CompiledViewDefinitionWithGraphs copyFrom) {
    s_logger.info("Copying graphs for cached/shared compiled view definition");
    s_profiler.begin();
    try {
      return new PLAT3249().copy(copyFrom);
    } finally {
      s_profiler.end();
    }
  }

}
