/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedCurveShiftFunction;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilityCurveTest {
  private static final InterpolatedDoublesCurve CURVE = InterpolatedDoublesCurve.from(new double[] {1, 2, 3}, new double[] {4, 5, 6}, new LinearInterpolator1D());

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve() {
    new VolatilityCurve(null);
  }

  @Test
  public void test() {
    final VolatilityCurve curve = new VolatilityCurve(CURVE);
    assertEquals(curve.getCurve(), CURVE);
    assertEquals(curve.getVolatility(1.5), 4.5, 1e-15);
    VolatilityCurve other = new VolatilityCurve(CURVE);
    assertEquals(other, curve);
    assertEquals(other.hashCode(), curve.hashCode());
    other = new VolatilityCurve(ConstantDoublesCurve.from(0));
    assertFalse(other.equals(curve));
  }

  @Test
  public void testParallel() {
    final InterpolatedCurveShiftFunction f = new InterpolatedCurveShiftFunction();
    final VolatilityCurve vol = new VolatilityCurve(CURVE);
    VolatilityCurve shifted1 = vol.withParallelShift(3);
    InterpolatedDoublesCurve shifted2 = f.evaluate(CURVE, 3.);
    assertArrayEquals(shifted1.getCurve().getXData(), shifted2.getXData());
    assertArrayEquals(shifted1.getCurve().getYData(), shifted2.getYData());
    shifted1 = vol.withSingleShift(1, 3);
    shifted2 = f.evaluate(CURVE, 1, 3.);
    assertArrayEquals(shifted1.getCurve().getXData(), shifted2.getXData());
    assertArrayEquals(shifted1.getCurve().getYData(), shifted2.getYData());
    shifted1 = vol.withMultipleShifts(new double[] {1, 2}, new double[] {3, 4});
    shifted2 = f.evaluate(CURVE, new double[] {1, 2}, new double[] {3, 4});
    assertArrayEquals(shifted1.getCurve().getXData(), shifted2.getXData());
    assertArrayEquals(shifted1.getCurve().getYData(), shifted2.getYData());
  }
}
