/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;

/**
 * Builds the row and column structure of a dependency graph grid given the compiled view definition and the
 * target at the root of the graph.
 */
/* package */ class  DependencyGraphStructureBuilder {

  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphStructureBuilder.class);

  /** {@link ValueSpecification}s for all rows in the grid in row index order. */
  private final List<ValueSpecification> _valueSpecs = Lists.newArrayList();
  /** Function names for all rows in the grid in row index order. */
  private final List<String> _fnNames = Lists.newArrayList();
  /** The grid structure. */
  private final DependencyGraphGridStructure _structure;

  /** Mutable variable for keeping track of the index of the last row */
  private int _lastRow = 0;

  /**
   * @param compiledViewDef The compiled view definition containing the dependency graph
   * @param root Specificaion of the value whose dependency graph structure is being built
   * @param calcConfigName The calculation configuration used when calculating the value
   * @param targetResolver For looking up calculation targets given their specification
   */
  /* package */ DependencyGraphStructureBuilder(CompiledViewDefinition compiledViewDef,
                                                ValueSpecification root,
                                                String calcConfigName,
                                                ComputationTargetResolver targetResolver) {
    // TODO see [PLAT-2478] this is a bit nasty but will work as long as the engine and web are running in the same VM
    // with this hack in place the user can open a dependency graph before the first set of results arrives
    // and see the graph structure with no values. without this hack the graph would be completely empty.
    // it might be better to try to get the dep graph from the view cycle (which is the only non-hacky way to get it
    // ATM) and only fall back on the hack if that fails. at least that way the dep graphs would mostly work if the
    // engine and web back-end are deployed in different VMs. you'd only see a problem if the user opens a dep graph
    // before the first set of results arrives
    if (!(compiledViewDef instanceof CompiledViewDefinitionWithGraphs)) {
      s_logger.warn("Compiled view definition is not an instance of CompiledViewDefinitionWithGraphs, class={}." +
                        " Dependency graphs not supported");
      _structure = new DependencyGraphGridStructure(AnalyticsNode.emptyRoot(),
                                                    null,
                                                    Collections.<ValueSpecification>emptyList(),
                                                    Collections.<String>emptyList(),
                                                    targetResolver);
    } else {
      CompiledViewDefinitionWithGraphs viewDef = (CompiledViewDefinitionWithGraphs) compiledViewDef;
      DependencyGraphExplorer depGraphExplorer = viewDef.getDependencyGraphExplorer(calcConfigName);
      DependencyGraph depGraph = depGraphExplorer.getSubgraphProducing(root);
      AnalyticsNode node = createNode(root, depGraph);
      _structure = new DependencyGraphGridStructure(node, calcConfigName, _valueSpecs, _fnNames, targetResolver);
    }
  }

  /**
   * Builds the tree structure of the graph starting at a node and working up the dependency graph through all the
   * nodes it depends on. Recursively builds up the node structure representing whole the dependency graph.
   * @param valueSpec The value specification of the target that is the current root
   * @param depGraph Dependency graph for the entire view definition
   * @return Root node of the grid structure representing the dependency graph for the value
   */
  private AnalyticsNode createNode(ValueSpecification valueSpec, DependencyGraph depGraph) {
    DependencyNode targetNode = depGraph.getNodeProducing(valueSpec);
    String fnName = targetNode.getFunction().getFunction().getFunctionDefinition().getShortName();
    _valueSpecs.add(valueSpec);
    _fnNames.add(fnName);
    int nodeStart = _lastRow;
    List<AnalyticsNode> nodes = new ArrayList<AnalyticsNode>();
    Set<ValueSpecification> inputValues = targetNode.getInputValues();
    if (inputValues.isEmpty()) {
      // don't create a node unless it has children
      return null;
    } else {
      for (ValueSpecification input : inputValues) {
        ++_lastRow;
        AnalyticsNode newNode = createNode(input, depGraph);
        if (newNode != null) {
          nodes.add(newNode);
        }
      }
      return new AnalyticsNode(nodeStart, _lastRow, Collections.unmodifiableList(nodes));
    }
  }

  /**
   * @return The grid structure
   */
  /* package */ DependencyGraphGridStructure getStructure() {
    return _structure;
  }
}
