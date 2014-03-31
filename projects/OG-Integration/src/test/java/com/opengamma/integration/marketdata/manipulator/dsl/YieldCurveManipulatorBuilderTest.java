/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static com.opengamma.integration.marketdata.manipulator.dsl.SimulationUtils.bucketedShift;
import static com.opengamma.integration.marketdata.manipulator.dsl.SimulationUtils.pointShift;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class YieldCurveManipulatorBuilderTest {

  private YieldCurveManipulatorBuilder _builder;
  
  private StructureManipulator<?> _manipulatorResult; //added via the scenario
  
  @BeforeMethod
  public void beforeMethod() {
    YieldCurveSelector mockSelector = Mockito.mock(YieldCurveSelector.class);
    //builder which assigns the created manipulator to _manipulatorResult
    _builder = new YieldCurveManipulatorBuilder(mockSelector, new Scenario("test") {

      @Override
      void add(DistinctMarketDataSelector selector, StructureManipulator<?> manipulator) {
        YieldCurveManipulatorBuilderTest.this._manipulatorResult = manipulator;
      }
      
    });
  }
  
  @Test
  public void bucketedShifts() {
    _builder.bucketedShifts(ScenarioShiftType.ABSOLUTE, bucketedShift(Period.ofYears(1), Period.ofYears(2), 3));
    
    YieldCurveBucketedShiftManipulator result = (YieldCurveBucketedShiftManipulator) _manipulatorResult;
    
    assertTrue("One shift expected", 1 == result.getShifts().size());

    YieldCurveBucketedShift shift = result.getShifts().get(0);
    
    assertEquals(Period.ofYears(1), shift.getStart());
    assertEquals(Period.ofYears(2), shift.getEnd());
    assertEquals(3., shift.getShift());
    
    
  }

  @Test
  public void pointShifts() {
    _builder.pointShifts(ScenarioShiftType.ABSOLUTE, pointShift(1, 2));
    
    YieldCurvePointShiftManipulator result = (YieldCurvePointShiftManipulator)_manipulatorResult;
    
    assertTrue("One shift expected", 1 == result.getPointShifts().size());
    
    YieldCurvePointShift shift = result.getPointShifts().get(0);
    
    assertEquals(1, shift.getPointIndex());
    assertEquals(2d, shift.getShift());
  }
}
