/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.FunctionalCurveShiftFunction;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function;

/**
 * 
 */
public class FunctionalCurveShiftFunctionTest {
  private static final Function<Double, Double> F = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... x) {
      return x[0] * x[0];
    }

  };
  private static final FunctionalDoublesCurve CURVE = FunctionalDoublesCurve.from(F, "X");
  private static final FunctionalCurveShiftFunction SHIFT = new FunctionalCurveShiftFunction();

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test1() {
    SHIFT.evaluate(CURVE, 2, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test2() {
    SHIFT.evaluate(CURVE, 2, 1, "A");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test3() {
    SHIFT.evaluate(CURVE, new double[] {2}, new double[] {1});
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test4() {
    SHIFT.evaluate(CURVE, new double[] {2}, new double[] {1}, "A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve1() {
    SHIFT.evaluate(null, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve2() {
    SHIFT.evaluate(null, 1, "B");
  }

  @Test
  public void test() {
    final double shift = 0.34;
    FunctionalDoublesCurve shifted = SHIFT.evaluate(CURVE, shift);
    for (int i = 0; i < 10; i++) {
      final double x = Math.random();
      assertEquals(shifted.getYValue(x), F.evaluate(x) + shift, 1e-15);
    }
    assertEquals(shifted.getName(), "PARALLEL_SHIFT_X");
    final String newName = "Y";
    shifted = SHIFT.evaluate(CURVE, shift, newName);
    for (int i = 0; i < 10; i++) {
      final double x = Math.random();
      assertEquals(shifted.getYValue(x), F.evaluate(x) + shift, 1e-15);
    }
    assertEquals(shifted.getName(), newName);
  }
}
