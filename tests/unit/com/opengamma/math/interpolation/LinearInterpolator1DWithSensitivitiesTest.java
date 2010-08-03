/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class LinearInterpolator1DWithSensitivitiesTest {

  private static final double EPS = 1e-15;
  private static final Interpolator1DWithSensitivities<Interpolator1DDataBundle> INTERPOLATOR = new LinearInterpolator1DWithSensitivities();
  private static final Function1D<Double, Double> FUNCTION = new Function1D<Double, Double>() {
    @Override
    public Double evaluate(final Double x) {
      return 2 * x - 7;
    }
  };

  private static final Interpolator1DDataBundle MODEL;

  static {
    final double[] x = new double[10];
    final double[] y = new double[10];
    for (int i = 0; i < 10; i++) {
      x[i] = Double.valueOf(i);
      y[i] = FUNCTION.evaluate(x[i]);
    }
    MODEL = Interpolator1DDataBundleFactory.fromSortedArrays(x, y);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullModel() {
    INTERPOLATOR.interpolate(null, 2.3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(MODEL, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowValue() {
    INTERPOLATOR.interpolate(MODEL, -4.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighValue() {
    INTERPOLATOR.interpolate(MODEL, 10.);
  }

  @Test
  public void test() {
    InterpolationResultWithSensitivities result = INTERPOLATOR.interpolate(MODEL, 3.4);
    assertEquals(result.getResult(), FUNCTION.evaluate(3.4), EPS);
    double[] sensitivity = result.getSensitivities();
    assertEquals(0.0, sensitivity[2], EPS);
    assertEquals(0.6, sensitivity[3], EPS);
    assertEquals(0.4, sensitivity[4], EPS);
    assertEquals(0.0, sensitivity[5], EPS);

    result = INTERPOLATOR.interpolate(MODEL, 7.0);
    assertEquals(result.getResult(), FUNCTION.evaluate(7.0), EPS);
    sensitivity = result.getSensitivities();

    assertEquals(0.0, sensitivity[6], EPS);
    assertEquals(1.0, sensitivity[7], EPS);
    assertEquals(0.0, sensitivity[8], EPS);

  }

}
