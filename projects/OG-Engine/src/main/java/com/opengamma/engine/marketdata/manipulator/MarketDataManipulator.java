/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

public class MarketDataManipulator {

  private final MarketDataSelector _marketDataSelector;

  public MarketDataManipulator(MarketDataSelector marketDataSelector) {
    _marketDataSelector = marketDataSelector;
    ArgumentChecker.notNull(marketDataSelector, "marketDataShiftSpecifications");
  }

  public Map<MarketDataSelector, Set<ValueSpecification>> modifyDependencyGraph(DependencyGraph graph) {

    // Drop out immediately if we have no shifts specified (caller should already have called this anyway)
    if (!hasManipulationsDefined()) {
      return ImmutableMap.of();
    }

    Map<MarketDataSelector, Set<ValueSpecification>> matches = new HashMap<>();

    String configurationName = graph.getCalculationConfigurationName();

    DependencyGraphStructureExtractor extractor = new DependencyGraphStructureExtractor(graph, _marketDataSelector.getApplicableStructureTypes());

    for (Map.Entry<StructureIdentifier<?>, DependencyNode> entry : extractor.extractStructures().entrySet()) {

      DependencyNode node = entry.getValue();

      MarketDataSelector matchingSelector = _marketDataSelector.findMatchingSelector(entry.getKey(), configurationName);
      if (matchingSelector != null) {

        // Alter the dependency graph, inserting a new node to allow manipulation of the structure data
        // The extractor is responsible for checking that a manipulation node has not already been inserted
        // New node will satisfy the same value spec as the original node, but will take the outputs
        // from the original and transform them as required. The dependent nodes of the original node
        // will be transferred to the new node, and as far as they are concerned there will be no change.
        DependencyNode proxyNode = graph.appendInput(node,
                                                          StructureManipulationFunction.INSTANCE,
                                                          ImmutableMap.of("MANIPULATION_NODE", "true"));

        if (matches.containsKey(matchingSelector)) {
          matches.get(matchingSelector).addAll(proxyNode.getOutputValues());
        } else {
          matches.put(matchingSelector, new HashSet<>(proxyNode.getOutputValues()));
        }
      }
    }
    return matches;
  }

  public boolean hasManipulationsDefined() {
    return _marketDataSelector.hasSelectionsDefined();
  }
}
