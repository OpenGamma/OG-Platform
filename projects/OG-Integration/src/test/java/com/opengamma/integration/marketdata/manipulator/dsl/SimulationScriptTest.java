/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SimulationScriptTest {

  private static final Logger s_logger = LoggerFactory.getLogger(SimulationScriptTest.class);

  @Test
  public void createSimulationFromDsl() {
    Simulation simulation = SimulationUtils.createSimulationFromDsl("src/test/resources/scenarios/SimulationDslTest.groovy", null);
    assertNotNull(simulation);
    // TODO check the simulation
    s_logger.debug(simulation.toString());
  }

  @Test
  public void createScenarioFromDsl() {
    Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/resources/scenarios/ScenarioDslTest.groovy", null);
    assertNotNull(scenario);
    // TODO check the simulation
    s_logger.debug(scenario.toString());
  }

  @Test
  public void parameters() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("foo", "FOO", "bar", 123);
    Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/resources/scenarios/ParametersTest.groovy", params);
    assertNotNull(scenario);
    ScenarioDefinition scenarioDefinition = scenario.createDefinition();
    Map<DistinctMarketDataSelector, FunctionParameters> definitionMap = scenarioDefinition.getDefinitionMap();
    PointSelector selector = new PointSelector(null, Sets.newHashSet(ExternalId.of("SCHEME", "FOO")), null, null, null);
    assertTrue(definitionMap.containsKey(selector));
    SimpleFunctionParameters functionParameters = (SimpleFunctionParameters) definitionMap.get(selector);
    CompositeStructureManipulator<?> composite = functionParameters.getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    StructureManipulator<?> manipulator = composite.getManipulators().get(0);
    assertEquals(new Scaling(123), manipulator);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void missingParameters() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("foo", "FOO");
    SimulationUtils.createScenarioFromDsl("src/test/resources/scenarios/ParametersTest.groovy", params);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongParameterType() {
    Map<String, Object> params = ImmutableMap.<String, Object>of("foo", "FOO", "bar", "BAR");
    SimulationUtils.createScenarioFromDsl("src/test/resources/scenarios/ParametersTest.groovy", params);
  }
}
