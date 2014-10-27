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
import org.threeten.bp.Period;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.INTEGRATION)
public class PointShiftTest {

  @Test
  public void yieldCurve() {
    Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/groovy/YieldCurvePointShiftTest.groovy", null);
    ScenarioDefinition definition = scenario.createDefinition();
    assertEquals("point shift test", definition.getName());
    Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();
    FunctionParameters params = map.get(new YieldCurveSelector(null, null, null, null, null));
    assertNotNull(params);
    Object value = ((SimpleFunctionParameters) params).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator manipulator = (CompositeStructureManipulator) value;
    List manipulators = manipulator.getManipulators();
    assertEquals(1, manipulators.size());
    List<YieldCurvePointShift> shifts =
        ImmutableList.of(
            new YieldCurvePointShift(2, 0.1),
            new YieldCurvePointShift(3, 0.2));
    YieldCurvePointShiftManipulator expected = new YieldCurvePointShiftManipulator(ScenarioShiftType.RELATIVE, shifts);
    assertEquals(expected, manipulators.get(0));
  }

  @Test
  public void yieldCurveData() {
    Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/groovy/YieldCurveDataPointShiftTest.groovy", null);
    ScenarioDefinition definition = scenario.createDefinition();
    assertEquals("point shift test", definition.getName());
    Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();
    FunctionParameters params = map.get(new YieldCurveDataSelector(null, null, null, null, null));
    assertNotNull(params);
    Object value = ((SimpleFunctionParameters) params).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator manipulator = (CompositeStructureManipulator) value;
    List manipulators = manipulator.getManipulators();
    assertEquals(1, manipulators.size());
    List<YieldCurveDataPointShift> shifts =
        ImmutableList.of(
            new YieldCurveDataPointShift(Period.ofMonths(3), 0.1),
            new YieldCurveDataPointShift(Period.ofYears(1), 0.2));
    YieldCurveDataPointShiftsManipulator expected = new YieldCurveDataPointShiftsManipulator(ScenarioShiftType.RELATIVE, shifts);
    assertEquals(expected, manipulators.get(0));
  }
}
