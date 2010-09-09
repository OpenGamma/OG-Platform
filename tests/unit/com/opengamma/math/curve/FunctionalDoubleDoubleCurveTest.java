/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class FunctionalDoubleDoubleCurveTest {
  private static final String NAME1 = "a";
  private static final String NAME2 = "b";
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x + 3;
    }

  };
  private static final FunctionalDoubleDoubleCurve CURVE = new FunctionalDoubleDoubleCurve(F, NAME1);

  @Test(expected = UnsupportedOperationException.class)
  public void testGetXData() {
    CURVE.getXData();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetYData() {
    CURVE.getYData();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetSize() {
    CURVE.size();
  }

  @Test
  public void testEqualsAndHashCode() {
    FunctionalDoubleDoubleCurve other = new FunctionalDoubleDoubleCurve(F, NAME1);
    assertEquals(CURVE, other);
    assertEquals(CURVE.hashCode(), other.hashCode());
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return x * x * x;
      }

    };
    other = new FunctionalDoubleDoubleCurve(f, NAME1);
    assertFalse(CURVE.equals(other));
    other = new FunctionalDoubleDoubleCurve(F, NAME2);
    assertFalse(CURVE.equals(other));
    other = new FunctionalDoubleDoubleCurve(F);
    assertFalse(CURVE.equals(other));
  }

  @Test
  public void testGetters() {
    assertEquals(CURVE.getName(), NAME1);
    assertEquals(CURVE.getYValue(2.3), F.evaluate(2.3), 0);
  }

  @Test
  public void testStaticConstruction() {
    FunctionalDoubleDoubleCurve curve = new FunctionalDoubleDoubleCurve(F);
    FunctionalDoubleDoubleCurve other = FunctionalDoubleDoubleCurve.of(F);
    assertEquals(curve.getFunction(), other.getFunction());
    assertFalse(curve.getName().equals(other.getName()));
    curve = new FunctionalDoubleDoubleCurve(F, NAME1);
    other = FunctionalDoubleDoubleCurve.of(F, NAME1);
    assertEquals(curve, other);
  }

  @Test
  public void testConvert() {
    final double eps = 1e-15;
    final double[] x = new double[] {0, 1, 2};
    final LinearInterpolator1D interpolator = new LinearInterpolator1D();
    DoubleDoubleCurve other = CURVE.toNodalDoubleDoubleCurve(x);
    assertArrayEquals(other.getXDataAsPrimitive(), x, eps);
    assertArrayEquals(other.getYDataAsPrimitive(), new double[] {F.evaluate(x[0]), F.evaluate(x[1]), F.evaluate(x[2])}, eps);
    other = CURVE.toInterpolatedDoubleDoubleCurve(x, interpolator);
    assertArrayEquals(other.getXDataAsPrimitive(), x, eps);
    assertArrayEquals(other.getYDataAsPrimitive(), new double[] {F.evaluate(x[0]), F.evaluate(x[1]), F.evaluate(x[2])}, eps);
    other = CURVE.toInterpolatedDoubleDoubleCurve(x, Collections.<Double, Interpolator1D<? extends Interpolator1DDataBundle>> singletonMap(Double.POSITIVE_INFINITY, interpolator));
    assertArrayEquals(other.getXDataAsPrimitive(), x, eps);
    assertArrayEquals(other.getYDataAsPrimitive(), new double[] {F.evaluate(x[0]), F.evaluate(x[1]), F.evaluate(x[2])}, eps);
  }
}
