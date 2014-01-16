/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SimulationScriptTest {

  private static final Logger s_logger = LoggerFactory.getLogger(SimulationScriptTest.class);

  @Test
  public void createSimulationFromDsl() {
    // TODO also test the expected values of the scenario fields. make sure delegation to the simulation works
    Simulation scriptSim = SimulationUtils.createSimulationFromDsl("src/test/groovy/SimulationDslTest.groovy", null);
    assertNotNull(scriptSim);
    Simulation sim = new Simulation("test simulation")
        .baseScenarioName("base")
        .calculationConfigurations("default", "config1")
        .resolverVersionCorrection(VersionCorrection.LATEST)
        .valuationTime(ZonedDateTime.of(2011, 3, 8, 2, 18, 0, 0, ZoneOffset.UTC));

    sim.scenario("scenario 1").curve().named("Forward6M").currencies("USD", "GBP")
        .apply().parallelShift(0.1).singleShift(10, 0.2);
    sim.scenario("scenario 1").marketDataPoint().idMatches("BLOOMBERG_TICKER", ".* Curncy").apply().scaling(1.2);
    sim.scenario("scenario 1").surface().nameMatches("someSurface.*").quoteTypes("TYPE_A", "TYPE_B")
        .apply().singleAdditiveShift(0.1, 0.1, 2.2);

    sim.scenario("scenario 2")
        .valuationTime(ZonedDateTime.of(1972, 3, 10, 21, 30, 0, 0, ZoneOffset.UTC))
        .resolverVersionCorrection(VersionCorrection.ofVersionAsOf(Instant.EPOCH))
        .calculationConfigurations("config2", "config3");
    sim.scenario("scenario 2").curve().named("Discounting").currencies("AUD").apply().parallelShift(0.05);
    sim.scenario("scenario 2").curve().named("Forward3M").currencies("AUD").apply().parallelShift(0.15);
    sim.scenario("scenario 2").marketDataPoint().idMatches("BLOOMBERG_TICKER", ".* Comdty").apply().scaling(0.9);
    sim.scenario("scenario 2").surface().quoteTypes("TYPE_C", "TYPE_D").apply().parallelShift(0.1);

    assertEquals(sim, scriptSim);
  }

  @Test
  public void createScenarioFromDsl() {
    Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/groovy/ScenarioDslTest.groovy", null);
    assertNotNull(scenario);
    // TODO check the simulation
    s_logger.debug(scenario.toString());
  }

  @Test
  public void parameters() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("foo", "FOO", "bar", 123d);
    Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/groovy/ParametersTest.groovy", params);
    assertNotNull(scenario);
    ScenarioDefinition scenarioDefinition = scenario.createDefinition();
    Map<DistinctMarketDataSelector, FunctionParameters> definitionMap = scenarioDefinition.getDefinitionMap();
    PointSelector selector = new PointSelector(null, Sets.newHashSet(ExternalId.of("SCHEME", "FOO")), null, null, null, null, null);
    assertTrue(definitionMap.containsKey(selector));
    SimpleFunctionParameters functionParameters = (SimpleFunctionParameters) definitionMap.get(selector);
    CompositeStructureManipulator<?> composite = functionParameters.getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    StructureManipulator<?> manipulator = composite.getManipulators().get(0);
    assertEquals(new MarketDataScaling(123), manipulator);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void missingParameters() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("foo", "FOO");
    SimulationUtils.createScenarioFromDsl("src/test/groovy/ParametersTest.groovy", params);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongParameterType() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("foo", "FOO", "bar", "BAR");
    SimulationUtils.createScenarioFromDsl("src/test/groovy/ParametersTest.groovy", params);
  }

  /**
   * Checks that simulation properties are picked up be scenarios even if the scenario has already been created.
   * otherwise there would be ordering issues in the scripts.
   */
  @Test
  public void updateSimulationAfterScenario() {
    Simulation simulation = SimulationUtils.createSimulationFromDsl("src/test/groovy/SimulationPropertiesTest.groovy", null);
    Map<String, Scenario> scenarios = simulation.getScenarios();
    Scenario scenario1 = scenarios.get("scen1");
    assertEquals(VersionCorrection.ofVersionAsOf(Instant.EPOCH), scenario1.getResolverVersionCorrection());
    assertEquals(ImmutableSet.of("config2", "config3"), scenario1.getCalcConfigNames());
    assertEquals(ZonedDateTime.of(1972, 3, 10, 21, 30, 0, 0, ZoneOffset.UTC).toInstant(), scenario1.getValuationTime());

    Scenario scenario2 = scenarios.get("scen2");
    assertEquals(null, scenario2.getResolverVersionCorrection());
    assertEquals(ImmutableSet.of("config0", "config1"), scenario2.getCalcConfigNames());
    assertEquals(ZonedDateTime.of(1972, 3, 10, 21, 30, 0, 0, ZoneOffset.UTC).toInstant(), scenario2.getValuationTime());
  }

  @Test(expectedExceptions = TimeoutException.class)
  public void timeout() {
    String script = "while (true) {}";
    SimulationUtils.createScenarioFromDsl(new StringReader(script), null);
  }
}
