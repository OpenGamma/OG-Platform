/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class CurveShiftFunctionFactoryTest {

  @Test(expected = IllegalArgumentException.class)
  public void testWrongClass() {
    CurveShiftFunctionFactory.getFunction(String.class);
  }

  @Test
  public void test() {
    assertEquals(ConstantCurveShiftFunction.class, CurveShiftFunctionFactory.getFunction(ConstantCurveShiftFunction.class).getClass());
    assertEquals(FunctionalCurveShiftFunction.class, CurveShiftFunctionFactory.getFunction(FunctionalCurveShiftFunction.class).getClass());
    assertEquals(InterpolatedCurveShiftFunction.class, CurveShiftFunctionFactory.getFunction(InterpolatedCurveShiftFunction.class).getClass());
    assertEquals(NodalCurveShiftFunction.class, CurveShiftFunctionFactory.getFunction(NodalCurveShiftFunction.class).getClass());
    assertEquals(SpreadCurveShiftFunction.class, CurveShiftFunctionFactory.getFunction(SpreadCurveShiftFunction.class).getClass());
  }
}
