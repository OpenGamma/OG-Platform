/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SpotRateScriptTest {

  @Test
  public void scalingLimits() {
    String script =
        "scenario 'scalingLimits', {" +
        "  spotRate {" +
        "    currencyPair 'EURUSD'" +
        "    apply {" +
        "      scaling 2, 0.1, 1.2" +
        "    }" +
        "  }" +
        "}";
    Scenario scenario = SimulationUtils.createScenarioFromDsl(new StringReader(script), null);
    ScenarioDefinition definition = scenario.createDefinition();
    Map<DistinctMarketDataSelector, FunctionParameters> definitionMap = definition.getDefinitionMap();
    assertEquals(1, definitionMap.size());
    SimpleFunctionParameters functionParameters = (SimpleFunctionParameters) definitionMap.values().iterator().next();
    CompositeStructureManipulator<?> compositeManipulator =
        functionParameters.getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    List<? extends StructureManipulator<?>> manipulators = compositeManipulator.getManipulators();
    assertEquals(1, manipulators.size());
    assertEquals(new SpotRateScaling(2d, 0.1, 1.2, CurrencyPair.of(Currency.EUR, Currency.USD)), manipulators.get(0));
  }

  @Test
  public void shiftLimits() {
    String script =
        "scenario 'shiftLimits', {" +
        "  spotRate {" +
        "    currencyPair 'EURUSD'" +
        "    apply {" +
        "      shift Absolute, 2, 0.1, 1.2" +
        "    }" +
        "  }" +
        "}";
    Scenario scenario = SimulationUtils.createScenarioFromDsl(new StringReader(script), null);
    ScenarioDefinition definition = scenario.createDefinition();
    Map<DistinctMarketDataSelector, FunctionParameters> definitionMap = definition.getDefinitionMap();
    assertEquals(1, definitionMap.size());
    SimpleFunctionParameters functionParameters = (SimpleFunctionParameters) definitionMap.values().iterator().next();
    CompositeStructureManipulator<?> compositeManipulator =
        functionParameters.getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    List<? extends StructureManipulator<?>> manipulators = compositeManipulator.getManipulators();
    assertEquals(1, manipulators.size());
    CurrencyPair eurUsd = CurrencyPair.of(Currency.EUR, Currency.USD);
    SpotRateShift expected = new SpotRateShift(ScenarioShiftType.ABSOLUTE, 2d, 0.1, 1.2, eurUsd);
    assertEquals(expected, manipulators.get(0));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void boundedShiftMultiplePairs() {
    String script =
        "scenario 'shiftLimits', {" +
        "  spotRate {" +
        "    currencyPairs 'EURUSD', 'GBPUSD'" +
        "    apply {" +
        "      shift Absolute, 2, 0.1, 1.2" +
        "    }" +
        "  }" +
        "}";
    SimulationUtils.createScenarioFromDsl(new StringReader(script), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void boundedScalingMultiplePairs() {
    String script =
        "scenario 'shiftLimits', {" +
        "  spotRate {" +
        "    currencyPairs 'EURUSD', 'GBPUSD'" +
        "    apply {" +
        "      scaling 2, 0.1, 1.2" +
        "    }" +
        "  }" +
        "}";
    SimulationUtils.createScenarioFromDsl(new StringReader(script), null);
  }
}
