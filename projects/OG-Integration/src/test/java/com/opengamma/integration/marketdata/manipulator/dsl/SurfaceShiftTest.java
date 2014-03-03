/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static com.opengamma.integration.marketdata.manipulator.dsl.SimulationUtils.volShift;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SurfaceShiftTest {

  @Test
  public void relative() {
    Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/groovy/SurfaceTest1.groovy", null);
    ScenarioDefinition definition = scenario.createDefinition();
    assertEquals("relative surface test", definition.getName());
    Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();
    FunctionParameters params = map.get(new VolatilitySurfaceSelector(null, null, null, null, null, null, null));
    assertNotNull(params);
    Object value = ((SimpleFunctionParameters) params).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator manipulator = (CompositeStructureManipulator) value;
    List manipulators = manipulator.getManipulators();
    assertEquals(1, manipulators.size());
    List<VolatilitySurfaceShift> shifts =
        ImmutableList.of(
            new VolatilitySurfaceShift(Period.ofMonths(6), 1.5, 0.1),
            new VolatilitySurfaceShift(Period.ofYears(1), 2.5, 0.2));
    VolatilitySurfaceShiftManipulator expected = VolatilitySurfaceShiftManipulator.create(ScenarioShiftType.RELATIVE,
                                                                                          shifts);
    assertEquals(expected, manipulators.get(0));
  }

  @Test
  public void absolute() {
    Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/groovy/SurfaceTest2.groovy", null);
    ScenarioDefinition definition = scenario.createDefinition();
    assertEquals("absolute surface test", definition.getName());
    Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();
    FunctionParameters params = map.get(new VolatilitySurfaceSelector(null, null, null, null, null, null, null));
    assertNotNull(params);
    Object value = ((SimpleFunctionParameters) params).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator manipulator = (CompositeStructureManipulator) value;
    List manipulators = manipulator.getManipulators();
    assertEquals(1, manipulators.size());
    List<VolatilitySurfaceShift> shifts =
        ImmutableList.of(
            new VolatilitySurfaceShift(0.5, 0.6, 0.1),
            new VolatilitySurfaceShift(1.5, 0.7, 0.2));
    VolatilitySurfaceShiftManipulator expected = VolatilitySurfaceShiftManipulator.create(ScenarioShiftType.ABSOLUTE,
                                                                                          shifts);
    assertEquals(expected, manipulators.get(0));
  }

  @Test
  public void dateDouble() {
    Scenario scenario = new Scenario("Java API test");
    scenario.surface().apply().shifts(ScenarioShiftType.RELATIVE,
                                      volShift(Period.ofMonths(6), 1.5, 0.1),
                                      volShift(Period.ofYears(1), 2.5, 0.2));
    ScenarioDefinition definition = scenario.createDefinition();
    assertEquals("Java API test", definition.getName());
    Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();
    FunctionParameters params = map.get(new VolatilitySurfaceSelector(null, null, null, null, null, null, null));
    assertNotNull(params);
    Object value = ((SimpleFunctionParameters) params).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator manipulator = (CompositeStructureManipulator) value;
    List manipulators = manipulator.getManipulators();
    assertEquals(1, manipulators.size());
    List<VolatilitySurfaceShift> shifts =
        ImmutableList.of(
            new VolatilitySurfaceShift(Period.ofMonths(6), 1.5, 0.1),
            new VolatilitySurfaceShift(Period.ofYears(1), 2.5, 0.2));
    VolatilitySurfaceShiftManipulator expected = VolatilitySurfaceShiftManipulator.create(ScenarioShiftType.RELATIVE, shifts);
    assertEquals(expected, manipulators.get(0));
  }

  @Test
  public void doubleDate() {
    Scenario scenario = new Scenario("Java API test");
    scenario.surface().apply().shifts(ScenarioShiftType.RELATIVE,
                                      volShift(1.5, Period.ofMonths(6), 0.1),
                                      volShift(2.5, Period.ofYears(1), 0.2));
    ScenarioDefinition definition = scenario.createDefinition();
    assertEquals("Java API test", definition.getName());
    Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();
    FunctionParameters params = map.get(new VolatilitySurfaceSelector(null, null, null, null, null, null, null));
    assertNotNull(params);
    Object value = ((SimpleFunctionParameters) params).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator manipulator = (CompositeStructureManipulator) value;
    List manipulators = manipulator.getManipulators();
    assertEquals(1, manipulators.size());
    List<VolatilitySurfaceShift> shifts =
        ImmutableList.of(
            new VolatilitySurfaceShift(1.5, Period.ofMonths(6), 0.1),
            new VolatilitySurfaceShift(2.5, Period.ofYears(1), 0.2));
    VolatilitySurfaceShiftManipulator expected = VolatilitySurfaceShiftManipulator.create(ScenarioShiftType.RELATIVE, shifts);
    assertEquals(expected, manipulators.get(0));
  }

  @Test
  public void doubleDouble() {
    Scenario scenario = new Scenario("Java API test");
    scenario.surface().apply().shifts(ScenarioShiftType.RELATIVE,
                                      volShift(1.5, 6.0, 0.1),
                                      volShift(2.5, 1.0, 0.2));
    ScenarioDefinition definition = scenario.createDefinition();
    assertEquals("Java API test", definition.getName());
    Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();
    FunctionParameters params = map.get(new VolatilitySurfaceSelector(null, null, null, null, null, null, null));
    assertNotNull(params);
    Object value = ((SimpleFunctionParameters) params).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator manipulator = (CompositeStructureManipulator) value;
    List manipulators = manipulator.getManipulators();
    assertEquals(1, manipulators.size());
    List<VolatilitySurfaceShift> shifts =
        ImmutableList.of(
            new VolatilitySurfaceShift(1.5, 6.0, 0.1),
            new VolatilitySurfaceShift(2.5, 1.0, 0.2));
    VolatilitySurfaceShiftManipulator expected = VolatilitySurfaceShiftManipulator.create(ScenarioShiftType.RELATIVE, shifts);
    assertEquals(expected, manipulators.get(0));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dateDate() {
    Scenario scenario = new Scenario("Java API test");
    scenario.surface().apply().shifts(ScenarioShiftType.RELATIVE,
                                      volShift(Period.ofYears(1), Period.ofMonths(6), 0.1),
                                      volShift(Period.ofYears(2), Period.ofMonths(9), 0.2));
  }
}
