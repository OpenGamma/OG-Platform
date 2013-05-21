/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyGraphExplorerImpl;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;

public class MarketDataManipulatorTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullConstructionFails() {
    new MarketDataManipulator(null);
  }

  @Test
  public void testInvocationWithEmptyGraphsGivesEmptyResults() {

    MarketDataManipulator manipulator = createNoOpManipulator();
    Map<DependencyGraph,Map<MarketDataSelector,Set<ValueSpecification>>> result =
        manipulator.modifyDependencyGraphs(ImmutableSet.<DependencyGraphExplorer>of());
    assertEquals(result.isEmpty(), true);
  }

  @Test
  public void testNoOpSelectorDoesNothingToGraph() {

    MarketDataManipulator manipulator = createNoOpManipulator();
    DependencyGraph graph = createSimpleGraphWithMarketDataNodes();
    Set<ValueSpecification> originalOutputSpecifications = ImmutableSet.copyOf(graph.getOutputSpecifications());

    Map<DependencyGraph,Map<MarketDataSelector,Set<ValueSpecification>>> result =
        manipulator.modifyDependencyGraphs(ImmutableSet.<DependencyGraphExplorer>of(new DependencyGraphExplorerImpl(graph)));

    assertEquals(result.isEmpty(), true);
    assertEquals(graph.getOutputSpecifications(), originalOutputSpecifications);
  }

  @Test
  public void testYieldCurveSelectorAltersGraph() {

    MarketDataManipulator manipulator = new MarketDataManipulator(YieldCurveSelector.of(new YieldCurveKey(Currency.USD, "Forward3M")));
    DependencyGraph graph = createSimpleGraphWithMarketDataNodes();
    Set<ValueSpecification> originalOutputSpecifications = ImmutableSet.copyOf(graph.getOutputSpecifications());
    ValueSpecification originalValueSpec = Iterables.getOnlyElement(originalOutputSpecifications);

    Map<DependencyGraph,Map<MarketDataSelector,Set<ValueSpecification>>> result =
        manipulator.modifyDependencyGraphs(ImmutableSet.<DependencyGraphExplorer>of(new DependencyGraphExplorerImpl(
            graph)));

    checkNodeHasBeenAddedToGraph(graph, originalOutputSpecifications, originalValueSpec, result);
  }

  @Test
  public void testCompositeSelectorReturnsUnderlyingSelector() {

    MarketDataSelector yieldCurveSelector = YieldCurveSelector.of(new YieldCurveKey(Currency.USD, "Forward3M"));
    MarketDataManipulator manipulator = new MarketDataManipulator(CompositeMarketDataSelector.of(yieldCurveSelector));
    DependencyGraph graph = createSimpleGraphWithMarketDataNodes();
    Set<ValueSpecification> originalOutputSpecifications = ImmutableSet.copyOf(graph.getOutputSpecifications());
    ValueSpecification originalValueSpec = Iterables.getOnlyElement(originalOutputSpecifications);

    Map<DependencyGraph,Map<MarketDataSelector,Set<ValueSpecification>>> result =
        manipulator.modifyDependencyGraphs(ImmutableSet.<DependencyGraphExplorer>of(new DependencyGraphExplorerImpl(
            graph)));

    checkNodeHasBeenAddedToGraph(graph, originalOutputSpecifications, originalValueSpec, result);

    Map<MarketDataSelector, Set<ValueSpecification>> selectorMap = result.get(graph);
    assertEquals(selectorMap.size(), 1);

    // Selector is the underlying, not composite
    assertEquals(selectorMap.containsKey(yieldCurveSelector), true);
  }

  private void checkNodeHasBeenAddedToGraph(DependencyGraph graph,
                                            Set<ValueSpecification> originalOutputSpecifications,
                                            ValueSpecification originalValueSpec,
                                            Map<DependencyGraph, Map<MarketDataSelector, Set<ValueSpecification>>> result) {
    assertEquals(result.isEmpty(), false);
    Set<ValueSpecification> outputSpecifications = new HashSet<>(graph.getOutputSpecifications());
    assertEquals(outputSpecifications.size(), originalOutputSpecifications.size() + 1);

    outputSpecifications.removeAll(originalOutputSpecifications);
    ValueSpecification additionalSpec = Iterables.getOnlyElement(outputSpecifications);

    assertEquals(additionalSpec.getValueName(), originalValueSpec.getValueName());
    assertEquals(additionalSpec.getTargetSpecification(), originalValueSpec.getTargetSpecification());

    ValueProperties expectedProps = originalValueSpec.getProperties().copy().with("MANIPULATION_NODE", "true").get();
    assertEquals(additionalSpec.getProperties(), expectedProps);
  }

  private DependencyGraph createSimpleGraphWithMarketDataNodes() {
    DependencyGraph graph = new DependencyGraph("testGraph");
    ComputationTargetSpecification targetSpecification = new ComputationTargetSpecification(ComputationTargetType.CURRENCY,
                                                                                            Currency.USD.getUniqueId());
    ComputationTarget target = new ComputationTarget(targetSpecification, Currency.USD);
    DependencyNode yieldCurveNode = new DependencyNode(targetSpecification);
    yieldCurveNode.setFunction(new MockFunction(target));
    yieldCurveNode.addOutputValue(new ValueSpecification("YieldCurveMarketData",
                                                         targetSpecification,
                                                         ValueProperties.builder().with("Curve", "Forward3M").with("Function", "someFunction").get()));
    graph.addDependencyNode(yieldCurveNode);
    return graph;
  }

  private MarketDataManipulator createNoOpManipulator() {
    return new MarketDataManipulator(NoOpMarketDataSelector.getInstance());
  }

}
