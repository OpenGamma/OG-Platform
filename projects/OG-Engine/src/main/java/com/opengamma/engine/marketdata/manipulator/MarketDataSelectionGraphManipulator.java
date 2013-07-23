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
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Performs manipulation of the compiled dependency graphs based on a general marketDataSelector (which is
 * applied to all graphs) and a set of specific market data selectors (which is applied only to named
 * graphs).
 *
 * If the graph contains nodes that match the selectors, they will be updated such that proxy nodes are
 * inserted which are able to intercept market data values and transform then.
 */
public class MarketDataSelectionGraphManipulator {

  /**
   * The market data selector which will be applied to all graphs.
   */
  private final MarketDataSelector _marketDataSelector;

  /**
   * The selectors which will be applied only to named graphs.
   */
  private final Map<String, Set<MarketDataSelector>> _specificSelectors = new HashMap<>();

  /**
   * Constructor for the class taking the general and specific market data selectors.
   *
   * @param marketDataSelector the market data selector which will be applied to all graphs, not null
   * @param specificSelectors the market data selectors which will be applied only to named graphs, not null
   */
  public MarketDataSelectionGraphManipulator(MarketDataSelector marketDataSelector,
                                             Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> specificSelectors) {
    ArgumentChecker.notNull(marketDataSelector, "marketDataSelector");
    ArgumentChecker.notNull(specificSelectors, "specificSelectors");
    _marketDataSelector = marketDataSelector;
    for (Map.Entry<String, Map<DistinctMarketDataSelector, FunctionParameters>> entry : specificSelectors.entrySet()) {
      // Workaround code for Java generics
      Set<MarketDataSelector> selectors = new HashSet<>();
      for (MarketDataSelector selector : entry.getValue().keySet()) {
        selectors.add(selector);
      }
      _specificSelectors.put(entry.getKey(), selectors);
    }
  }

  /**
   * Navigates the specified graph, identifying any nodes which meet the selection criteria of
   * the market data selectors. Those which do match will have new nodes inserted into the graph,
   * proxying the market data ndoes and providing the ability to perform transformations on the
   * data as required.
   *
   *
   * @param graph the graph to be potentially modified, not null
   * @param resolver For looking up data used in the selection criteria, e.g. securities
   * @return a map of the market data selectors which matched nodes in the graph, together with
   * the value specifications of the new proxy nodes
   */
  public Map<DistinctMarketDataSelector, Set<ValueSpecification>> modifyDependencyGraph(DependencyGraph graph,
                                                                                        ComputationTargetResolver.AtVersionCorrection resolver) {

    ArgumentChecker.notNull(graph, "graph");

    String configurationName = graph.getCalculationConfigurationName();
    MarketDataSelector combinedSelector = buildCombinedSelector(configurationName);

    // Drop out immediately if we have no shifts specified (caller should already have verified
    // this for the primary selector)
    if (!combinedSelector.hasSelectionsDefined()) {
      return ImmutableMap.of();
    }

    DependencyGraphStructureExtractor extractor = new DependencyGraphStructureExtractor(graph, combinedSelector.getApplicableStructureTypes());
    Map<DistinctMarketDataSelector, Set<ValueSpecification>> matches = new HashMap<>();
    DefaultSelectorResolver selectorResolver = new DefaultSelectorResolver(resolver);

    for (Map.Entry<StructureIdentifier<?>, DependencyNode> entry : extractor.extractStructures().entrySet()) {

      DependencyNode node = entry.getValue();

      // todo - this could match multiple but we just get one - may be problematic
      DistinctMarketDataSelector matchingSelector =
          combinedSelector.findMatchingSelector(entry.getKey(), configurationName, selectorResolver);
      if (matchingSelector != null) {

        // Alter the dependency graph, inserting a new node to allow manipulation of the structure data
        // The extractor is responsible for checking that a manipulation node has not already been inserted
        // New node will satisfy the same value spec as the original node, but will take the outputs
        // from the original and transform them as required. The dependent nodes of the original node
        // will be transferred to the new node, and as far as they are concerned there will be no change.
        DependencyNode proxyNode = graph.appendInput(
            node,
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

  private MarketDataSelector buildCombinedSelector(String configurationName) {
    Set<MarketDataSelector> selectors = new HashSet<>(extractSpecificSelectors(configurationName));
    selectors.add(_marketDataSelector);
    return CompositeMarketDataSelector.of(selectors);
  }

  private Set<MarketDataSelector> extractSpecificSelectors(String configurationName) {
    return _specificSelectors.containsKey(configurationName) ?
        _specificSelectors.get(configurationName) :
        new HashSet<MarketDataSelector>();
  }

  public boolean hasManipulationsDefined() {
    return _marketDataSelector.hasSelectionsDefined() || !_specificSelectors.isEmpty();
  }
}
