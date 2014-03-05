/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.MockComputationTargetResolver;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class MarketDataSelectionGraphManipulatorTest {

  private final ComputationTargetResolver.AtVersionCorrection _resolver = MockComputationTargetResolver.resolved().atVersionCorrection(VersionCorrection.LATEST);

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
    final MarketDataSelectionGraphManipulator manipulator = createNoOpManipulator();
    final Map<DistinctMarketDataSelector, Set<ValueSpecification>> result = new HashMap<>();
    manipulator.modifyDependencyGraph(new TestDependencyGraphBuilder("testGraph").buildGraph(), _resolver, result);
    assertEquals(result.isEmpty(), true);
  }

  @Test
  public void testNoOpSelectorDoesNothingToGraph() {
    final MarketDataSelectionGraphManipulator manipulator = createNoOpManipulator();
    final DependencyGraph graph = createSimpleGraphWithMarketDataNodes();
    final Set<ValueSpecification> originalOutputSpecifications = DependencyGraphImpl.getAllOutputSpecifications(graph);
    final Map<DistinctMarketDataSelector, Set<ValueSpecification>> result = new HashMap<>();
    final DependencyGraph newGraph = manipulator.modifyDependencyGraph(graph, _resolver, result);
    assertEquals(result.isEmpty(), true);
    assertEquals(DependencyGraphImpl.getAllOutputSpecifications(newGraph), originalOutputSpecifications);
  }

  @Test
  public void testYieldCurveSelectorAltersGraph() {
    final MarketDataSelectionGraphManipulator manipulator = new MarketDataSelectionGraphManipulator(createYieldCurveSelector(Currency.USD, "Forward3M"), createEmptyViewCalcManipulations());
    final DependencyGraph graph = createSimpleGraphWithMarketDataNodes();
    final Set<ValueSpecification> originalOutputSpecifications = DependencyGraphImpl.getAllOutputSpecifications(graph);
    final Map<DistinctMarketDataSelector, Set<ValueSpecification>> result = new HashMap<>();
    final DependencyGraph newGraph = manipulator.modifyDependencyGraph(graph, _resolver, result);
    checkNodeHasBeenAddedToGraph(newGraph, originalOutputSpecifications, result);
  }

  @Test
  public void testSpecificYieldCurveSelectorAltersNamedGraph() {
    final DistinctMarketDataSelector yieldCurveSelector = createYieldCurveSelector(Currency.USD, "Forward3M");
    final Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> specificManipulators = new HashMap<>();
    specificManipulators.put("graph", createYieldCurveSelectorAndParams(Currency.USD, "Forward3M"));
    final MarketDataSelectionGraphManipulator manipulator =
        new MarketDataSelectionGraphManipulator(NoOpMarketDataSelector.getInstance(), specificManipulators);
    final DependencyGraph graph1 = createNamedDependencyGraph("graph");
    final Set<ValueSpecification> originalOutputSpecifications1 = DependencyGraphImpl.getAllOutputSpecifications(graph1);
    final Map<DistinctMarketDataSelector, Set<ValueSpecification>> result1 = new HashMap<>();
    final DependencyGraph newGraph1 = manipulator.modifyDependencyGraph(graph1, _resolver, result1);
    checkNodeHasBeenAddedToGraph(newGraph1, originalOutputSpecifications1, result1);
    assertEquals(result1.size(), 1);
    // Selector is the underlying, not composite
    assertTrue(result1.containsKey(yieldCurveSelector));
  }

  @Test
  public void testSpecificYieldCurveSelectorDoesNotAlterDifferentNamedGraph() {
    final Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> specificManipulators = new HashMap<>();
    specificManipulators.put("graph-to-find", createYieldCurveSelectorAndParams(Currency.USD, "Forward3M"));
    final MarketDataSelectionGraphManipulator manipulator = new MarketDataSelectionGraphManipulator(NoOpMarketDataSelector.getInstance(), specificManipulators);
    final DependencyGraph graph = createNamedDependencyGraph("not-the-graph-you-are-looking-for");
    final Set<ValueSpecification> originalOutputSpecifications2 = DependencyGraphImpl.getAllOutputSpecifications(graph);
    final Map<DistinctMarketDataSelector, Set<ValueSpecification>> result = new HashMap<>();
    final DependencyGraph newGraph = manipulator.modifyDependencyGraph(graph, _resolver, result);
    assertTrue(result.isEmpty());
    assertEquals(DependencyGraphImpl.getAllOutputSpecifications(newGraph), originalOutputSpecifications2);

  }

  private ImmutableMap<DistinctMarketDataSelector, FunctionParameters> createYieldCurveSelectorAndParams(Currency currency, String curveType) {
    return ImmutableMap.<DistinctMarketDataSelector, FunctionParameters>of(createYieldCurveSelector(currency, curveType), EmptyFunctionParameters.INSTANCE);
  }

  @Test
  public void testCompositeSelectorReturnsUnderlyingSelector() {
    final MarketDataSelector yieldCurveSelector = createYieldCurveSelector(Currency.USD, "Forward3M");
    final MarketDataSelectionGraphManipulator manipulator = new MarketDataSelectionGraphManipulator(CompositeMarketDataSelector.of(yieldCurveSelector), createEmptyViewCalcManipulations());
    final DependencyGraph graph = createSimpleGraphWithMarketDataNodes();
    final Set<ValueSpecification> originalOutputSpecifications = DependencyGraphImpl.getAllOutputSpecifications(graph);
    final Map<DistinctMarketDataSelector, Set<ValueSpecification>> result = new HashMap<>();
    final DependencyGraph newGraph = manipulator.modifyDependencyGraph(graph, _resolver, result);
    checkNodeHasBeenAddedToGraph(newGraph, originalOutputSpecifications, result);
    assertEquals(result.size(), 1);
    // Selector is the underlying, not composite
    assertEquals(result.containsKey(yieldCurveSelector), true);
  }

  @Test
  public void noSelectors() {
    Set<MarketDataSelector> noSelectors = Collections.emptySet();
    MarketDataSelectionGraphManipulator manipulator =
        new MarketDataSelectionGraphManipulator(CompositeMarketDataSelector.of(noSelectors),
                                                createEmptyViewCalcManipulations());
    DependencyGraph graph = createSimpleGraphWithMarketDataNodes();
    Map<DistinctMarketDataSelector, Set<ValueSpecification>> result = new HashMap<>();
    manipulator.modifyDependencyGraph(graph, _resolver, result);
    assertTrue(result.isEmpty());
  }

  private DistinctMarketDataSelector createYieldCurveSelector(Currency currency, String curveType) {
    return YieldCurveSelector.of(YieldCurveKey.of(currency, curveType));
  }

  private void checkNodeHasBeenAddedToGraph(DependencyGraph graph,
                                            Set<ValueSpecification> originalOutputSpecifications,
                                            Map<DistinctMarketDataSelector, Set<ValueSpecification>> result) {
    assertFalse(result.isEmpty());
    ValueSpecification originalValueSpec = Iterables.getOnlyElement(originalOutputSpecifications);
    Set<ValueSpecification> outputSpecifications = new HashSet<>(DependencyGraphImpl.getAllOutputSpecifications(graph));
    assertEquals(outputSpecifications.size(), 2);
    outputSpecifications.removeAll(originalOutputSpecifications);
    ValueSpecification additionalSpec = Iterables.getOnlyElement(outputSpecifications);
    assertEquals(additionalSpec.getValueName(), originalValueSpec.getValueName());
    assertEquals(additionalSpec.getTargetSpecification(), originalValueSpec.getTargetSpecification());
    ValueProperties expectedProps = originalValueSpec.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, "someFunctionStructureManipulator").get();
    assertEquals(additionalSpec.getProperties(), expectedProps);
  }

  private DependencyGraph createSimpleGraphWithMarketDataNodes() {
    return createNamedDependencyGraph("testGraph");
  }

  private DependencyGraph createNamedDependencyGraph(String name) {
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder(name);
    final ComputationTargetSpecification targetSpecification = new ComputationTargetSpecification(ComputationTargetType.CURRENCY, Currency.USD.getUniqueId());
    final ComputationTarget target = new ComputationTarget(targetSpecification, Currency.USD);
    gb.addNode(new MockFunction(target)).addOutput(
        new ValueSpecification(ValueRequirementNames.YIELD_CURVE, targetSpecification, ValueProperties.builder().with(ValuePropertyNames.CURVE, "Forward3M")
            .with(ValuePropertyNames.FUNCTION, "someFunction").get()));
    return gb.buildGraph();
  }

  private Map<String, Map<DistinctMarketDataSelector, FunctionParameters>> createEmptyViewCalcManipulations() {
    return new HashMap<>();
  }

  private MarketDataSelectionGraphManipulator createNoOpManipulator() {
    return new MarketDataSelectionGraphManipulator(NoOpMarketDataSelector.getInstance(), createEmptyViewCalcManipulations());
  }
}
