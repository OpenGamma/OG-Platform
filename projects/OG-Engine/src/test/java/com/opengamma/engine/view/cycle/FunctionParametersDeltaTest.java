/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.MarketDataPointSelector;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test the {@link FunctionParametersDelta} class
 */
@Test(groups = TestGroup.UNIT)
public class FunctionParametersDeltaTest {

  public void testEmpty() {
    final ViewCycleExecutionOptions emptyOptions = new ViewCycleExecutionOptions();
    assertSame(FunctionParametersDelta.of(emptyOptions, emptyOptions), FunctionParametersDelta.EMPTY);
    assertEquals(FunctionParametersDelta.EMPTY.getValueSpecifications("Default", null, null), Collections.emptySet());
  }

  public void testEmptyFirst() {
    final Map<DistinctMarketDataSelector, FunctionParameters> first = Collections.emptyMap();
    final Map<DistinctMarketDataSelector, FunctionParameters> second = Collections.singletonMap(MarketDataPointSelector.of(ExternalId.of("Test", "Foo")),
        Mockito.mock(FunctionParameters.class));
    final FunctionParametersDelta delta = FunctionParametersDelta.of(first, second);
    assertEquals(delta.getSelectors(), second.keySet());
  }

  public void testEmptySecond() {
    final Map<DistinctMarketDataSelector, FunctionParameters> first = Collections.singletonMap(MarketDataPointSelector.of(ExternalId.of("Test", "Foo")),
        Mockito.mock(FunctionParameters.class));
    final Map<DistinctMarketDataSelector, FunctionParameters> second = Collections.emptyMap();
    final FunctionParametersDelta delta = FunctionParametersDelta.of(first, second);
    assertEquals(delta.getSelectors(), first.keySet());
  }

  public void testDeltaEmpty() {
    final Map<DistinctMarketDataSelector, FunctionParameters> first = Collections.singletonMap(MarketDataPointSelector.of(ExternalId.of("Test", "Foo")),
        (FunctionParameters) new EmptyFunctionParameters());
    final Map<DistinctMarketDataSelector, FunctionParameters> second = Collections.singletonMap(MarketDataPointSelector.of(ExternalId.of("Test", "Foo")),
        (FunctionParameters) new EmptyFunctionParameters());
    final FunctionParametersDelta delta = FunctionParametersDelta.of(first, second);
    assertSame(delta, FunctionParametersDelta.EMPTY);
  }

  public void testDeltaIntersection() {
    final DistinctMarketDataSelector mA = MarketDataPointSelector.of(ExternalId.of("Test", "A"));
    final DistinctMarketDataSelector mB = MarketDataPointSelector.of(ExternalId.of("Test", "B"));
    final DistinctMarketDataSelector mC = MarketDataPointSelector.of(ExternalId.of("Test", "C"));
    final DistinctMarketDataSelector mD = MarketDataPointSelector.of(ExternalId.of("Test", "D"));
    final FunctionParameters pA = new EmptyFunctionParameters();
    final FunctionParameters pB = new EmptyFunctionParameters();
    final FunctionParameters pC1 = new SimpleFunctionParameters(ImmutableMap.of("Test", "Foo"));
    final FunctionParameters pC2 = new SimpleFunctionParameters(ImmutableMap.of("Test", "Bar"));
    final FunctionParameters pD = new EmptyFunctionParameters();
    final Map<DistinctMarketDataSelector, FunctionParameters> first = ImmutableMap.of(mA, pA, mB, pB, mC, pC1);
    final Map<DistinctMarketDataSelector, FunctionParameters> second = ImmutableMap.of(mB, pB, mC, pC2, mD, pD);
    final FunctionParametersDelta delta = FunctionParametersDelta.of(first, second);
    assertEquals(delta.getSelectors(), Arrays.asList(mA, mC, mD));
  }

  public void testCalcConfigEmptyFirst() {
    final FunctionParametersDelta delta = FunctionParametersDelta.of(Collections.singleton(MarketDataPointSelector.of(ExternalId.of("Test", "A"))));
    final CompiledViewDefinition cvd1 = Mockito.mock(CompiledViewDefinition.class);
    Mockito.when(cvd1.getCompiledCalculationConfiguration("Default")).thenReturn(null);
    final CompiledViewDefinition cvd2 = Mockito.mock(CompiledViewDefinition.class);
    assertEquals(delta.getValueSpecifications("Default", cvd1, cvd2), Collections.emptySet());
    Mockito.verify(cvd1).getCompiledCalculationConfiguration("Default");
    Mockito.verifyZeroInteractions(cvd2);
  }

  public void testCalcConfigEmptySecond() {
    final FunctionParametersDelta delta = FunctionParametersDelta.of(Collections.singleton(MarketDataPointSelector.of(ExternalId.of("Test", "A"))));
    final CompiledViewDefinition cvd1 = Mockito.mock(CompiledViewDefinition.class);
    Mockito.when(cvd1.getCompiledCalculationConfiguration("Default")).thenReturn(Mockito.mock(CompiledViewCalculationConfiguration.class));
    final CompiledViewDefinition cvd2 = Mockito.mock(CompiledViewDefinition.class);
    Mockito.when(cvd2.getCompiledCalculationConfiguration("Default")).thenReturn(null);
    assertEquals(delta.getValueSpecifications("Default", cvd1, cvd2), Collections.emptySet());
    Mockito.verify(cvd1).getCompiledCalculationConfiguration("Default");
    Mockito.verify(cvd2).getCompiledCalculationConfiguration("Default");
  }

  public void testCalcConfig() {
    final DistinctMarketDataSelector mA = MarketDataPointSelector.of(ExternalId.of("Test", "A"));
    final DistinctMarketDataSelector mB = MarketDataPointSelector.of(ExternalId.of("Test", "B"));
    final DistinctMarketDataSelector mC = MarketDataPointSelector.of(ExternalId.of("Test", "C"));
    final DistinctMarketDataSelector mD = MarketDataPointSelector.of(ExternalId.of("Test", "D"));
    final ValueSpecification vA = new ValueSpecification("A", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
    final ValueSpecification vB = new ValueSpecification("B", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
    final ValueSpecification vC = new ValueSpecification("C", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
    final ValueSpecification vD = new ValueSpecification("D", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
    final FunctionParametersDelta delta = FunctionParametersDelta.of(Arrays.asList(mA, mC, mD));
    final CompiledViewDefinition cvd1 = Mockito.mock(CompiledViewDefinition.class);
    final CompiledViewCalculationConfiguration cvcc1 = Mockito.mock(CompiledViewCalculationConfiguration.class);
    Mockito.when(cvcc1.getMarketDataSelections()).thenReturn(ImmutableMap.of(mA, Collections.singleton(vA), mB, Collections.singleton(vB), mC, Collections.singleton(vC)));
    Mockito.when(cvd1.getCompiledCalculationConfiguration("Default")).thenReturn(cvcc1);
    final CompiledViewDefinition cvd2 = Mockito.mock(CompiledViewDefinition.class);
    final CompiledViewCalculationConfiguration cvcc2 = Mockito.mock(CompiledViewCalculationConfiguration.class);
    Mockito.when(cvcc2.getMarketDataSelections()).thenReturn(ImmutableMap.of(mB, Collections.singleton(vB), mC, Collections.singleton(vC), mD, Collections.singleton(vD)));
    Mockito.when(cvd2.getCompiledCalculationConfiguration("Default")).thenReturn(cvcc2);
    final Set<ValueSpecification> changed = delta.getValueSpecifications("Default", cvd1, cvd2);
    assertEquals(changed, ImmutableSet.of(vA, vC, vD));
  }

}
