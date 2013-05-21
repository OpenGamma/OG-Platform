/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.util.ArgumentChecker;

public class MarketDataManipulator {

  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataManipulator.class);

  private final MarketDataSelector _marketDataSelector;

  public MarketDataManipulator(MarketDataSelector marketDataSelector) {
    _marketDataSelector = marketDataSelector;
    ArgumentChecker.notNull(marketDataSelector, "marketDataShiftSpecifications");
  }

  public void modifyDependencyGraphs(Collection<DependencyGraphExplorer> graphExplorers) {

    // Drop out immediately if we have no shifts specified
    if (!_marketDataSelector.containsShifts()) {
      s_logger.debug("No active market data shifts defined - nothing to do");
      return;
    }

    for (DependencyGraphExplorer explorer : graphExplorers) {

      DependencyGraph graph = explorer.getWholeGraph();
      String configurationName = graph.getCalculationConfigurationName();

      YieldCurveStructureExtractor extractor =
          new YieldCurveStructureExtractor( graph);

      for (Map.Entry<YieldCurveKey, DependencyNode> entry : extractor.extractStructures().entrySet()) {

        YieldCurveKey structureId = entry.getKey();
        DependencyNode node = entry.getValue();


        if (_marketDataSelector.appliesTo(StructureIdentifier.of(structureId), configurationName)) {

          // Alter the dependency graph, inserting a new node to allow manipulation of the structure data
          // The extractor is responsible for checking that a manipulation node has not already been inserted
          // New node will satisfy the same value spec as the original node, but will take the outputs
          // from the original and transform them as required. The dependent nodes of the original node
          // will be transferred to the new node, and as far as they are concerned there will be no change.
          graph.appendInput(node, StructureManipulationFunction.INSTANCE, ImmutableMap.of("MANIPULATION_NODE", "true"));
        }
      }
    }
  }
}
