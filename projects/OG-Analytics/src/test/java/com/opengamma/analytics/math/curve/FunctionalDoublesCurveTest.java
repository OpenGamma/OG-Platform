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

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionalDoublesCurveTest {
  private static final String NAME1 = "a";
  private static final String NAME2 = "b";
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x + 3;
    }

  };
  private static final FunctionalDoublesCurve CURVE = new FunctionalDoublesCurve(F, NAME1);

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetXData() {
    CURVE.getXData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetYData() {
    CURVE.getYData();
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testGetSize() {
    CURVE.size();
  }

  @Test
  public void testEqualsAndHashCode() {
    FunctionalDoublesCurve other = new FunctionalDoublesCurve(F, NAME1);
    assertEquals(CURVE, other);
    assertEquals(CURVE.hashCode(), other.hashCode());
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return x * x * x;
      }

    };
    other = new FunctionalDoublesCurve(f, NAME1);
    assertFalse(CURVE.equals(other));
    other = new FunctionalDoublesCurve(F, NAME2);
    assertFalse(CURVE.equals(other));
    other = new FunctionalDoublesCurve(F);
    assertFalse(CURVE.equals(other));
  }

  @Test
  public void testGetters() {
    assertEquals(CURVE.getName(), NAME1);
    assertEquals(CURVE.getYValue(2.3), F.evaluate(2.3), 0);
  }

  @Test
  public void testStaticConstruction() {
    FunctionalDoublesCurve curve = new FunctionalDoublesCurve(F);
    FunctionalDoublesCurve other = FunctionalDoublesCurve.from(F);
    assertEquals(curve.getFunction(), other.getFunction());
    assertFalse(curve.getName().equals(other.getName()));
    curve = new FunctionalDoublesCurve(F, NAME1);
    other = FunctionalDoublesCurve.from(F, NAME1);
    assertEquals(curve, other);
  }

  @Test
  public void testConvert() {
    final double eps = 1e-15;
    final double[] x = new double[] {0, 1, 2};
    final LinearInterpolator1D interpolator = new LinearInterpolator1D();
    InterpolatedDoublesCurve other = CURVE.toInterpolatedDoublesCurve(x, interpolator);
    assertArrayEquals(other.getXDataAsPrimitive(), x, eps);
    assertArrayEquals(other.getYDataAsPrimitive(), new double[] {F.evaluate(x[0]), F.evaluate(x[1]), F.evaluate(x[2])}, eps);
  }
}
