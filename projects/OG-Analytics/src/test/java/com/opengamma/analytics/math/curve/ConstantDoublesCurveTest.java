/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ConstantDoublesCurveTest {
  private static final double Y1 = 20;
  private static final double Y2 = 21;
  private static final String NAME1 = "a";
  private static final String NAME2 = "b";
  private static final ConstantDoublesCurve CURVE = new ConstantDoublesCurve(Y1, NAME1);

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetXData() {
    CURVE.getXData();
  }

  @Test
  public void testEqualsAndHashCode() {
    ConstantDoublesCurve other = new ConstantDoublesCurve(Y1, NAME1);
    assertEquals(CURVE, other);
    assertEquals(CURVE.hashCode(), other.hashCode());
    other = new ConstantDoublesCurve(Y2, NAME1);
    assertFalse(CURVE.equals(other));
    other = new ConstantDoublesCurve(Y1);
    assertFalse(CURVE.equals(other));
    other = new ConstantDoublesCurve(Y1, NAME2);
    assertFalse(CURVE.equals(other));
  }

  @Test
  public void testGetters() {
    assertEquals(CURVE.getName(), NAME1);
    assertEquals(CURVE.getYValue(30.1), Y1, 0);
    assertEquals(CURVE.size(), 1);
    assertArrayEquals(CURVE.getYData(), new Double[] {Y1});
  }

  @Test
  public void testStaticConstruction() {
    ConstantDoublesCurve curve = new ConstantDoublesCurve(Y1);
    ConstantDoublesCurve other = ConstantDoublesCurve.from(Y1);
    assertArrayEquals(curve.getYData(), other.getYData());
    assertFalse(curve.getName().equals(other.getName()));
    curve = new ConstantDoublesCurve(Y1, NAME1);
    other = ConstantDoublesCurve.from(Y1, NAME1);
    assertEquals(curve, other);
  }

  @Test
  public void testConvert() {
    final double eps = 1e-15;
    final double[] x = new double[] {0, 1, 2};
    final double[] y = new double[] {Y1, Y1, Y1};
    final LinearInterpolator1D interpolator = new LinearInterpolator1D();
    InterpolatedDoublesCurve other = CURVE.toInterpolatedDoublesCurve(x, interpolator);
    assertArrayEquals(other.getXDataAsPrimitive(), x, eps);
    assertArrayEquals(other.getYDataAsPrimitive(), y, eps);
  }
}
