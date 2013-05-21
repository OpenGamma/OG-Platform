/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

public class MarketDataManipulator {

  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataManipulator.class);

  private final MarketDataSelector _marketDataSelector;

  public MarketDataManipulator(MarketDataSelector marketDataSelector) {
    _marketDataSelector = marketDataSelector;
    ArgumentChecker.notNull(marketDataSelector, "marketDataShiftSpecifications");
  }

  public Map<DependencyGraph, Map<MarketDataSelector, Set<ValueSpecification>>> modifyDependencyGraphs(Collection<DependencyGraphExplorer> graphExplorers) {

    // Drop out immediately if we have no shifts specified
    if (!_marketDataSelector.containsShifts()) {
      s_logger.debug("No active market data shifts defined - nothing to do");
      return ImmutableMap.of();
    }

    Map<DependencyGraph, Map<MarketDataSelector, Set<ValueSpecification>>> results = new HashMap<>();

    for (DependencyGraphExplorer explorer : graphExplorers) {

      Map<MarketDataSelector, Set<ValueSpecification>> graphMatches = new HashMap<>();

      DependencyGraph graph = explorer.getWholeGraph();
      String configurationName = graph.getCalculationConfigurationName();

      YieldCurveStructureExtractor extractor = new YieldCurveStructureExtractor(graph);

      for (Map.Entry<YieldCurveKey, DependencyNode> entry : extractor.extractStructures().entrySet()) {

        YieldCurveKey structureId = entry.getKey();
        DependencyNode node = entry.getValue();

        MarketDataSelector matchingSelector =
            _marketDataSelector.findMatchingSelector(StructureIdentifier.of(structureId), configurationName);
        if (matchingSelector != null) {

          // Alter the dependency graph, inserting a new node to allow manipulation of the structure data
          // The extractor is responsible for checking that a manipulation node has not already been inserted
          // New node will satisfy the same value spec as the original node, but will take the outputs
          // from the original and transform them as required. The dependent nodes of the original node
          // will be transferred to the new node, and as far as they are concerned there will be no change.
          DependencyNode dependencyNode = graph.appendInput(node,
                                                            StructureManipulationFunction.INSTANCE,
                                                            ImmutableMap.of("MANIPULATION_NODE", "true"));

          if (graphMatches.containsKey(matchingSelector)) {
            graphMatches.get(matchingSelector).addAll(dependencyNode.getOutputValues());
          } else {
            graphMatches.put(matchingSelector, new HashSet<>(dependencyNode.getOutputValues()));
          }
        }
      }

      if (!graphMatches.isEmpty()) {
        results.put(graph,  graphMatches);
      }
    }

    return results;
  }
}
