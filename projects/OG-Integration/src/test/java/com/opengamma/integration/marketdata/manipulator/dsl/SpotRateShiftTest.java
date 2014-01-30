/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SpotRateShiftTest {

  private static final double DELTA = 0.00000001;
  private static final SpotRateShift SHIFT = new SpotRateShift(1d, ImmutableSet.of(CurrencyPair.parse("EUR/USD"),
                                                                                   CurrencyPair.parse("CHF/JPY")));

  private static ValueSpecification valueSpec(String currencyPairStr) {
    ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get();
    CurrencyPair currencyPair = CurrencyPair.parse(currencyPairStr);
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(CurrencyPair.TYPE, currencyPair.getUniqueId());
    return new ValueSpecification("SpotRate", targetSpec, properties);
  }

  @Test
  public void normalPair() {
    assertEquals(3d, SHIFT.execute(2d, valueSpec("EUR/USD"), new FunctionExecutionContext()), DELTA);
    assertEquals(5d, SHIFT.execute(4d, valueSpec("CHF/JPY"), new FunctionExecutionContext()), DELTA);
  }

  @Test
  public void inversePair() {
    assertEquals(0.6666666666d, SHIFT.execute(2d, valueSpec("USD/EUR"), new FunctionExecutionContext()), DELTA);
    assertEquals(0.8d, SHIFT.execute(4d, valueSpec("JPY/CHF"), new FunctionExecutionContext()), DELTA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void unexpectedTargetType() {
    ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "foo").get();
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.CURRENCY,
                                                                                   Currency.GBP.getUniqueId());
    ValueSpecification valueSpec = new ValueSpecification("SpotRate", targetSpec, properties);
    SHIFT.execute(2d, valueSpec, new FunctionExecutionContext());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void unexpectedCurrencyPair() {
    SHIFT.execute(2d, valueSpec("GBP/USD"), new FunctionExecutionContext());
  }

  @Test
  public void dsl() {
    Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/groovy/SpotRateTest.groovy", null);
    ScenarioDefinition definition = scenario.createDefinition();

    assertEquals("spot rate test", definition.getName());
    Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();

    FunctionParameters eurUsdParams = map.get(new SpotRateSelector(null, ImmutableSet.of(CurrencyPair.parse("EUR/USD"))));
    assertNotNull(eurUsdParams);
    Object eurUsdValue = ((SimpleFunctionParameters) eurUsdParams).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator eurUsdManipulator = (CompositeStructureManipulator) eurUsdValue;
    List eurUsdManipulators = eurUsdManipulator.getManipulators();
    assertEquals(1, eurUsdManipulators.size());
    SpotRateShift eurUsdShift = new SpotRateShift(0.1, ImmutableSet.of(CurrencyPair.parse("EUR/USD")));
    assertEquals(eurUsdShift, eurUsdManipulators.get(0));

    FunctionParameters gbpAudParams = map.get(new SpotRateSelector(null, ImmutableSet.of(CurrencyPair.parse("GBP/AUD"))));
    assertNotNull(gbpAudParams);
    Object gbpAudValue = ((SimpleFunctionParameters) gbpAudParams).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator gbpAudManipulator = (CompositeStructureManipulator) gbpAudValue;
    List gbpAudManipulators = gbpAudManipulator.getManipulators();
    assertEquals(1, gbpAudManipulators.size());
    SpotRateScaling gbpAudScaling = new SpotRateScaling(0.2, ImmutableSet.of(CurrencyPair.parse("GBP/AUD")));
    assertEquals(gbpAudScaling, gbpAudManipulators.get(0));

    FunctionParameters eurCadParams = map.get(new SpotRateSelector(null, ImmutableSet.of(CurrencyPair.parse("EUR/CAD"))));
    assertNotNull(eurCadParams);
    Object eurCadValue = ((SimpleFunctionParameters) eurCadParams).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator eurCadManipulator = (CompositeStructureManipulator) eurCadValue;
    List eurCadManipulators = eurCadManipulator.getManipulators();
    assertEquals(1, eurCadManipulators.size());
    SpotRateReplace eurCadReplace = new SpotRateReplace(1.5);
    assertEquals(eurCadReplace, eurCadManipulators.get(0));
  }

  @Test
  public void javaApi() {
    Scenario scenario = new Scenario("Java API test");
    scenario.spotRate().currencyPair("EURUSD").apply().shift(0.1);
    scenario.spotRate().currencyPair("GBPAUD").apply().scaling(0.2);
    scenario.spotRate().currencyPair("EURCAD").apply().replace(1.5);
    ScenarioDefinition definition = scenario.createDefinition();

    assertEquals("Java API test", definition.getName());
    Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();

    FunctionParameters eurUsdParams = map.get(new SpotRateSelector(null, ImmutableSet.of(CurrencyPair.parse("EUR/USD"))));
    assertNotNull(eurUsdParams);
    Object eurUsdValue = ((SimpleFunctionParameters) eurUsdParams).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator eurUsdManipulator = (CompositeStructureManipulator) eurUsdValue;
    List eurUsdManipulators = eurUsdManipulator.getManipulators();
    assertEquals(1, eurUsdManipulators.size());
    SpotRateShift eurUsdShift = new SpotRateShift(0.1, ImmutableSet.of(CurrencyPair.parse("EUR/USD")));
    assertEquals(eurUsdShift, eurUsdManipulators.get(0));

    FunctionParameters gbpAudParams = map.get(new SpotRateSelector(null, ImmutableSet.of(CurrencyPair.parse("GBP/AUD"))));
    assertNotNull(gbpAudParams);
    Object gbpAudValue = ((SimpleFunctionParameters) gbpAudParams).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator gbpAudManipulator = (CompositeStructureManipulator) gbpAudValue;
    List gbpAudManipulators = gbpAudManipulator.getManipulators();
    assertEquals(1, gbpAudManipulators.size());
    SpotRateScaling gbpAudScaling = new SpotRateScaling(0.2, ImmutableSet.of(CurrencyPair.parse("GBP/AUD")));
    assertEquals(gbpAudScaling, gbpAudManipulators.get(0));

    FunctionParameters eurCadParams = map.get(new SpotRateSelector(null, ImmutableSet.of(CurrencyPair.parse("EUR/CAD"))));
    assertNotNull(eurCadParams);
    Object eurCadValue = ((SimpleFunctionParameters) eurCadParams).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator eurCadManipulator = (CompositeStructureManipulator) eurCadValue;
    List eurCadManipulators = eurCadManipulator.getManipulators();
    assertEquals(1, eurCadManipulators.size());
    SpotRateReplace eurCadReplace = new SpotRateReplace(1.5);
    assertEquals(eurCadReplace, eurCadManipulators.get(0));
  }
}
