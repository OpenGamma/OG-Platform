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
import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.SimpleFunctionParameters;
import com.opengamma.engine.function.StructureManipulationFunction;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.ScenarioDefinition;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.integration.marketdata.manipulator.dsl.volsurface.VolatilitySurfaceIndexShifts;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class SurfaceShiftTest {

  private static final double DELTA = 0.000001;

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
    VolatilitySurfaceShiftManipulator expected = VolatilitySurfaceShiftManipulator.create(ScenarioShiftType.RELATIVE, shifts);
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
    VolatilitySurfaceShiftManipulator expected = VolatilitySurfaceShiftManipulator.create(ScenarioShiftType.ABSOLUTE, shifts);
    assertEquals(expected, manipulators.get(0));
  }

  /** Tests creating index shifts from a Groovy script. */
  @Test
  public void index() {
    Scenario scenario = SimulationUtils.createScenarioFromDsl("src/test/groovy/SurfaceTest3.groovy", null);
    ScenarioDefinition definition = scenario.createDefinition();
    assertEquals("surface index shifts", definition.getName());
    Map<DistinctMarketDataSelector, FunctionParameters> map = definition.getDefinitionMap();
    FunctionParameters params = map.get(new VolatilitySurfaceSelector(null, null, null, null, null, null, null));
    assertNotNull(params);
    Object value = ((SimpleFunctionParameters) params).getValue(StructureManipulationFunction.EXPECTED_PARAMETER_NAME);
    CompositeStructureManipulator manipulator = (CompositeStructureManipulator) value;
    List manipulators = manipulator.getManipulators();
    assertEquals(1, manipulators.size());
    List<Double> shifts = Lists.newArrayList(0d, 1e-4, 2e-4);
    VolatilitySurfaceIndexShifts expected = new VolatilitySurfaceIndexShifts(ScenarioShiftType.ABSOLUTE, shifts);
    assertEquals(expected, manipulators.get(0));
  }

  /** Tests applying an index shift to a surface. */
  @Test
  public void indexSurface() {
    NodalDoublesSurface surface = new NodalDoublesSurface(new double[]{1.1, 1.2, 1.2, 1.3, 1.3, 1.3},
                                                          new double[]{0.1, 0.1, 0.2, 0.1, 0.2, 0.3},
                                                          new double[]{1, 2, 3, 4, 5, 6});
    List<Double> shiftList = Lists.newArrayList(0d, 0.1, 0.2);
    VolatilitySurfaceIndexShifts shifts = new VolatilitySurfaceIndexShifts(ScenarioShiftType.ABSOLUTE, shiftList);
    ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "bar").get();
    ValueSpecification spec = new ValueSpecification("foo", ComputationTargetSpecification.NULL, properties);
    VolatilitySurface shiftedSurface = shifts.execute(new VolatilitySurface(surface), spec, new FunctionExecutionContext());
    assertEquals(1.0, shiftedSurface.getVolatility(1.1, 0.1), DELTA);
    assertEquals(2.1, shiftedSurface.getVolatility(1.2, 0.1), DELTA);
    assertEquals(3.1, shiftedSurface.getVolatility(1.2, 0.2), DELTA);
    assertEquals(4.2, shiftedSurface.getVolatility(1.3, 0.1), DELTA);
    assertEquals(5.2, shiftedSurface.getVolatility(1.3, 0.2), DELTA);
    assertEquals(6.2, shiftedSurface.getVolatility(1.3, 0.3), DELTA);
  }

  /** Tests applying an index shift to a surface where there are fewer shifts specified than expiries in the surface. */
  @Test
  public void indexSurfaceFewerShiftsThanExpiries() {
    NodalDoublesSurface surface = new NodalDoublesSurface(new double[]{1.1, 1.2, 1.2, 1.3, 1.3, 1.3},
                                                          new double[]{0.1, 0.1, 0.2, 0.1, 0.2, 0.3},
                                                          new double[]{1, 2, 3, 4, 5, 6});
    List<Double> shiftList = Lists.newArrayList(0d, 0.1);
    VolatilitySurfaceIndexShifts shifts = new VolatilitySurfaceIndexShifts(ScenarioShiftType.ABSOLUTE, shiftList);
    ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "bar").get();
    ValueSpecification spec = new ValueSpecification("foo", ComputationTargetSpecification.NULL, properties);
    VolatilitySurface shiftedSurface = shifts.execute(new VolatilitySurface(surface), spec, new FunctionExecutionContext());
    assertEquals(1.0, shiftedSurface.getVolatility(1.1, 0.1), DELTA);
    assertEquals(2.1, shiftedSurface.getVolatility(1.2, 0.1), DELTA);
    assertEquals(3.1, shiftedSurface.getVolatility(1.2, 0.2), DELTA);
    assertEquals(4.0, shiftedSurface.getVolatility(1.3, 0.1), DELTA);
    assertEquals(5.0, shiftedSurface.getVolatility(1.3, 0.2), DELTA);
    assertEquals(6.0, shiftedSurface.getVolatility(1.3, 0.3), DELTA);
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
