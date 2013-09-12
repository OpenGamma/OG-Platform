/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.cycle.ViewCycle;

/**
 * Builds the row and column structure of a dependency graph grid given the compiled view definition and the
 * target at the root of the graph.
 */
/* package */ class  DependencyGraphStructureBuilder {

  /** {@link ValueSpecification}s for all rows in the grid in row index order. */
  private final List<ValueSpecification> _valueSpecs = Lists.newArrayList();
  /** Function names for all rows in the grid in row index order. */
  private final List<String> _fnNames = Lists.newArrayList();
  /** The grid structure. */
  private final DependencyGraphGridStructure _structure;

  /** Mutable variable for keeping track of the index of the last row */
  private int _lastRow;

  /**
   * @param compiledViewDef The compiled view definition containing the dependency graph
   * @param rootSpec Specification of the value whose dependency graph structure is being built
   * @param calcConfigName The calculation configuration used when calculating the value
   * @param targetResolver For looking up calculation targets given their specification
   * @param cycle The most recent view cycle
   */
  /* package */ DependencyGraphStructureBuilder(CompiledViewDefinition compiledViewDef,
                                                ValueRequirement requirement,
                                                ValueSpecification rootSpec,
                                                String calcConfigName,
                                                ComputationTargetResolver targetResolver,
                                                ViewCycle cycle) {
    // TODO see [PLAT-2478] this is a bit nasty
    // with this hack in place the user can open a dependency graph before the first set of results arrives
    // and see the graph structure with no values. without this hack the graph would be completely empty.
    // it only works if this class is running in the same VM as the engine
    //
    // if the engine and the web components are in a different VM then compiledViewDef won't be an instance of
    // CompiledViewDefinitionWithGraphs and the hack won't work. in that case the view cycle will be empty and the
    // user won't see a dependency graph if this is called before the first set of results arrives.
    // as soon as the first set of results arrives it will work the same as if all the components are in the same VM
    CompiledViewDefinitionWithGraphs viewDef;
    if (compiledViewDef instanceof CompiledViewDefinitionWithGraphs) {
      viewDef = (CompiledViewDefinitionWithGraphs) compiledViewDef;
    } else {
      viewDef = cycle.getCompiledViewDefinition();
    }
    DependencyGraphExplorer depGraphExplorer = viewDef.getDependencyGraphExplorer(calcConfigName);
    DependencyGraph depGraph = depGraphExplorer.getSubgraphProducing(rootSpec);
    AnalyticsNode node = createNode(rootSpec, depGraph, true);
    _structure = new DependencyGraphGridStructure(node, calcConfigName, requirement, _valueSpecs, _fnNames, targetResolver);
  }

  /**
   * Builds the tree structure of the graph starting at a node and working up the dependency graph through all the
   * nodes it depends on. Recursively builds up the node structure representing whole the dependency graph.
   * @param valueSpec The value specification of the target that is the current root
   * @param depGraph Dependency graph for the entire view definition, possibly null
   * @param rootNode Whether the value specification is for the root node of the dependency graph
   * @return Root node of the grid structure representing the dependency graph for the value
   */
  private AnalyticsNode createNode(ValueSpecification valueSpec, DependencyGraph depGraph, boolean rootNode) {
    if (depGraph == null) {
      return null;
    }
    DependencyNode targetNode = depGraph.getNodeProducing(valueSpec);
    String fnName = targetNode.getFunction().getFunction().getFunctionDefinition().getShortName();
    _valueSpecs.add(valueSpec);
    _fnNames.add(fnName);
    int nodeStart = _lastRow;
    List<AnalyticsNode> nodes = Lists.newArrayList();
    Set<ValueSpecification> inputValues = targetNode.getInputValues();
    if (inputValues.isEmpty()) {
      if (rootNode) {
        // the root node should never be null even if it has no children
        return new AnalyticsNode(nodeStart, _lastRow, Collections.<AnalyticsNode>emptyList(), false);
      } else {
        // non-root leaf nodes don't need a node of their own, their place in the structure is handled by their parent
        return null;
      }
    } else {
      for (ValueSpecification input : inputValues) {
        ++_lastRow;
        AnalyticsNode newNode = createNode(input, depGraph, false);
        if (newNode != null) {
          nodes.add(newNode);
        }
      }
      return new AnalyticsNode(nodeStart, _lastRow, Collections.unmodifiableList(nodes), false);
    }
  }

  /**
   * @return The grid structure
   */
  /* package */ DependencyGraphGridStructure getStructure() {
    return _structure;
  }
}
