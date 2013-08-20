/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.MockComputationTargetResolver;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class MarketDataSelectionGraphManipulatorTest {

  private final ComputationTargetResolver.AtVersionCorrection _resolver =
      MockComputationTargetResolver.resolved().atVersionCorrection(VersionCorrection.LATEST);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSelectorConstructionFails() {
    new MarketDataSelectionGraphManipulator(null, createEmptyViewCalcManipulations());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullViewCalcSelectorConstructionFails() {
    new MarketDataSelectionGraphManipulator(createYieldCurveSelector(Currency.USD, "Forward3M"), null);
  }

  @Test
  public void testInvocationWithEmptyGraphsGivesEmptyResults() {

    MarketDataSelectionGraphManipulator manipulator = createNoOpManipulator();
    Map<DistinctMarketDataSelector,Set<ValueSpecification>> result =
        manipulator.modifyDependencyGraph(new DependencyGraph("testGraph"), _resolver);
    assertEquals(result.isEmpty(), true);
  }

  @Test
  public void testNoOpSelectorDoesNothingToGraph() {

    MarketDataSelectionGraphManipulator manipulator = createNoOpManipulator();
    DependencyGraph graph = createSimpleGraphWithMarketDataNodes();
    Set<ValueSpecification> originalOutputSpecifications = ImmutableSet.copyOf(graph.getOutputSpecifications());

    Map<DistinctMarketDataSelector,Set<ValueSpecification>> result = manipulator.modifyDependencyGraph(graph, _resolver);

    assertEquals(result.isEmpty(), true);
    assertEquals(graph.getOutputSpecifications(), originalOutputSpecifications);
  }

  @Test
  public void testYieldCurveSelectorAltersGraph() {

    MarketDataSelectionGraphManipulator manipulator = new MarketDataSelectionGraphManipulator(
        createYieldCurveSelector(Currency.USD, "Forward3M"),
        createEmptyViewCalcManipulations());

    DependencyGraph graph = createSimpleGraphWithMarketDataNodes();
    Set<ValueSpecification> originalOutputSpecifications = ImmutableSet.copyOf(graph.getOutputSpecifications());

    Map<DistinctMarketDataSelector,Set<ValueSpecification>> result = manipulator.modifyDependencyGraph(graph, _resolver);

    checkNodeHasBeenAddedToGraph(graph, originalOutputSpecifications, result);
  }

  @Test
  public void testSpecificYieldCurveSelectorAltersNamedGraph() {

    DistinctMarketDataSelector yieldCurveSelector = createYieldCurveSelector(Currency.USD, "Forward3M");

    Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> specificManipulators = new HashMap<>();
    specificManipulators.put("graph",  createYieldCurveSelectorAndParams(Currency.USD, "Forward3M"));

    MarketDataSelectionGraphManipulator manipulator = new MarketDataSelectionGraphManipulator(
        NoOpMarketDataSelector.getInstance(),
        specificManipulators);

    DependencyGraph graph1 = createNamedDependencyGraph("graph");
    Set<ValueSpecification> originalOutputSpecifications1 = ImmutableSet.copyOf(graph1.getOutputSpecifications());

    Map<DistinctMarketDataSelector, Set<ValueSpecification>> result1 = manipulator.modifyDependencyGraph(graph1, _resolver);

    checkNodeHasBeenAddedToGraph(graph1, originalOutputSpecifications1, result1);

    assertEquals(result1.size(), 1);

    // Selector is the underlying, not composite
    assertEquals(result1.containsKey(yieldCurveSelector), true);
  }

  @Test
  public void testSpecificYieldCurveSelectorDoesNotAlterDifferentNamedGraph() {

    Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> specificManipulators = new HashMap<>();
    specificManipulators.put("graph-to-find", createYieldCurveSelectorAndParams(Currency.USD, "Forward3M"));

    MarketDataSelectionGraphManipulator manipulator = new MarketDataSelectionGraphManipulator(
        NoOpMarketDataSelector.getInstance(),
        specificManipulators);

    DependencyGraph graph = createNamedDependencyGraph("not-the-graph-you-are-looking-for");
    Set<ValueSpecification> originalOutputSpecifications2 = ImmutableSet.copyOf(graph.getOutputSpecifications());

    Map<DistinctMarketDataSelector, Set<ValueSpecification>> result = manipulator.modifyDependencyGraph(graph, _resolver);

    assertTrue(result.isEmpty());
    assertEquals(graph.getOutputSpecifications(), originalOutputSpecifications2);

  }

  private ImmutableMap<DistinctMarketDataSelector, FunctionParameters> createYieldCurveSelectorAndParams(
      Currency currency, String curveType) {

    return ImmutableMap.<DistinctMarketDataSelector, FunctionParameters>of(createYieldCurveSelector(currency, curveType),
                                                                           EmptyFunctionParameters.INSTANCE);
  }

  @Test
  public void testCompositeSelectorReturnsUnderlyingSelector() {

    MarketDataSelector yieldCurveSelector = createYieldCurveSelector(Currency.USD, "Forward3M");
    MarketDataSelectionGraphManipulator manipulator = new MarketDataSelectionGraphManipulator(
        CompositeMarketDataSelector.of(yieldCurveSelector),
        createEmptyViewCalcManipulations());
    DependencyGraph graph = createSimpleGraphWithMarketDataNodes();
    Set<ValueSpecification> originalOutputSpecifications = ImmutableSet.copyOf(graph.getOutputSpecifications());

    Map<DistinctMarketDataSelector,Set<ValueSpecification>> result = manipulator.modifyDependencyGraph(graph, _resolver);

    checkNodeHasBeenAddedToGraph(graph, originalOutputSpecifications, result);

    assertEquals(result.size(), 1);

    // Selector is the underlying, not composite
    assertEquals(result.containsKey(yieldCurveSelector), true);
  }

  private DistinctMarketDataSelector createYieldCurveSelector(Currency currency, String curveType) {
    return YieldCurveSelector.of(new YieldCurveKey(currency, curveType));
  }

  private void checkNodeHasBeenAddedToGraph(DependencyGraph graph,
                                            Set<ValueSpecification> originalOutputSpecifications,
                                            Map<DistinctMarketDataSelector, Set<ValueSpecification>> result) {

    assertFalse(result.isEmpty());
    ValueSpecification originalValueSpec = Iterables.getOnlyElement(originalOutputSpecifications);

    Set<ValueSpecification> outputSpecifications = new HashSet<>(graph.getOutputSpecifications());
    assertEquals(outputSpecifications.size(), 2);

    outputSpecifications.removeAll(originalOutputSpecifications);
    ValueSpecification additionalSpec = Iterables.getOnlyElement(outputSpecifications);

    assertEquals(additionalSpec.getValueName(), originalValueSpec.getValueName());
    assertEquals(additionalSpec.getTargetSpecification(), originalValueSpec.getTargetSpecification());

    ValueProperties expectedProps = originalValueSpec.getProperties().copy().with("MANIPULATION_NODE", "true").get();
    assertEquals(additionalSpec.getProperties(), expectedProps);
  }

  private DependencyGraph createSimpleGraphWithMarketDataNodes() {
    return createNamedDependencyGraph("testGraph");
  }

  private DependencyGraph createNamedDependencyGraph(String name) {
    DependencyGraph graph = new DependencyGraph(name);
    ComputationTargetSpecification targetSpecification = new ComputationTargetSpecification(
        ComputationTargetType.CURRENCY,
        Currency.USD.getUniqueId());

    ComputationTarget target = new ComputationTarget(targetSpecification, Currency.USD);

    DependencyNode yieldCurveNode = new DependencyNode(targetSpecification);
    yieldCurveNode.setFunction(new MockFunction(target));
    yieldCurveNode.addOutputValue(new ValueSpecification(
        "YieldCurve",
        targetSpecification,
        ValueProperties.builder().with("Curve", "Forward3M").with("Function", "someFunction").get()));

    graph.addDependencyNode(yieldCurveNode);
    return graph;
  }

  private Map<String, Map<DistinctMarketDataSelector,FunctionParameters>> createEmptyViewCalcManipulations() {
    return new HashMap<>();
  }

  private MarketDataSelectionGraphManipulator createNoOpManipulator() {
    return new MarketDataSelectionGraphManipulator(
        NoOpMarketDataSelector.getInstance(), createEmptyViewCalcManipulations());
  }
}
