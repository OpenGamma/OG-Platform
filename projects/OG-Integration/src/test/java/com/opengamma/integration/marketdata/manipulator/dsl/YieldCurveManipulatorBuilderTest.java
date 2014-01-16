/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
    _builder.bucketedShifts(/*BucketedShiftType.FORWARD*/)
      .shift(1, 2, 3, CurveShiftType.ABSOLUTE)
      .apply();
    
    YieldCurveBucketedShiftManipulator result = (YieldCurveBucketedShiftManipulator)_manipulatorResult;
    
    assertTrue("One shift expected", 1 == result.getShifts().size());
    //assertEquals(BucketedShiftType.FORWARD, result.getBucketedShiftType());
    
    YieldCurveBucketedShift shift = result.getShifts().get(0);
    
    assertEquals(1., shift.getStartYears());
    assertEquals(2., shift.getEndYears());
    assertEquals(3., shift.getShift());
    
    
  }

  @Test
  public void pointShifts() {
    
    _builder.pointShifts()
      .shift(1, 2, CurveShiftType.ABSOLUTE)
      .apply();
    
    YieldCurvePointShiftManipulator result = (YieldCurvePointShiftManipulator)_manipulatorResult;
    
    assertTrue("One shift expected", 1 == result.getPointShifts().size());
    
    YieldCurvePointShift shift = result.getPointShifts().get(0);
    
    assertEquals(1., shift.getYear());
    assertEquals(2., shift.getShift());
    assertEquals(CurveShiftType.ABSOLUTE, shift.getShiftType());
  }
}
