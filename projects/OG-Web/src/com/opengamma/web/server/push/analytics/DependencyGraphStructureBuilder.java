/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

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
 *
 */
public class DependencyGraphStructureBuilder {

  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphStructureBuilder.class);

  // TODO could this be used for both grid types?
  private final List<DependencyGraphGridStructure.Row> _rows = Lists.newArrayList();
  private final DependencyGraphGridStructure _structure;

  /* Keeps track of the index of the last row */
  private int _lastRow = 0;

  public DependencyGraphStructureBuilder(CompiledViewDefinition compiledViewDef,
                                         ValueSpecification root,
                                         String calcConfigName,
                                         ComputationTargetResolver targetResolver,
                                         ResultsFormatter formatter) {
    // TODO see [PLAT-XXXX] this is a bit nasty but should work as long as the engine and web are running in the same process
    if (!(compiledViewDef instanceof CompiledViewDefinitionWithGraphs)) {
      s_logger.warn("Compiled view definition is not an instance of CompiledViewDefinitionWithGraphs, class={}." +
                        " Dependency graphs not supported");
      // TODO create empty() factory method
      _structure = new DependencyGraphGridStructure(AnalyticsNode.emptyRoot(),
                                                    Collections.<DependencyGraphGridStructure.Row>emptyList(),
                                                    targetResolver,
                                                    formatter);
    } else {
      CompiledViewDefinitionWithGraphs viewDef = (CompiledViewDefinitionWithGraphs) compiledViewDef;
      DependencyGraphExplorer depGraphExplorer = viewDef.getDependencyGraphExplorer(calcConfigName);
      DependencyGraph depGraph = depGraphExplorer.getSubgraphProducing(root);
      AnalyticsNode node = createNode(root, depGraph);
      _structure = new DependencyGraphGridStructure(node, _rows, targetResolver, formatter);
    }
  }

  private AnalyticsNode createNode(ValueSpecification valueSpec, DependencyGraph depGraph) {
    DependencyNode targetNode = depGraph.getNodeProducing(valueSpec);
    String fnName = targetNode.getFunction().getFunction().getFunctionDefinition().getShortName();
    _rows.add(new DependencyGraphGridStructure.Row(valueSpec, fnName));
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

  public DependencyGraphGridStructure getStructure() {
    return _structure;
  }
}
