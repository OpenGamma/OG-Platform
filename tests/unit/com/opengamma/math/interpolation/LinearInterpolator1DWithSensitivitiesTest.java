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
  private static final Interpolator1DWithSensitivities<Interpolator1DModel> INTERPOLATOR = new LinearInterpolator1DWithSensitivities();
  private static final Function1D<Double, Double> FUNCTION = new Function1D<Double, Double>() {
    @Override
    public Double evaluate(final Double x) {
      return 2 * x - 7;
    }
  };

  private static final Interpolator1DModel MODEL;

  static {
    double[] x = new double[10];
    double[] y = new double[10];
    for (int i = 0; i < 10; i++) {
      x[i] = Double.valueOf(i);
      y[i] = FUNCTION.evaluate(x[i]);
    }
    MODEL = Interpolator1DModelFactory.fromSortedArrays(x, y);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullModel() {
    INTERPOLATOR.interpolate(null, 2.3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(MODEL, null);
  }

  @Test(expected = InterpolationException.class)
  public void testLowValue() {
    INTERPOLATOR.interpolate(MODEL, -4.0);
  }

  @Test(expected = InterpolationException.class)
  public void testHighValue() {
    INTERPOLATOR.interpolate(MODEL, 10.);
  }

  @Test
  public void test() {

    InterpolationResultWithSensitivities result = INTERPOLATOR.interpolate(MODEL, 3.4);
    assertEquals(result.getResult(), FUNCTION.evaluate(3.4), EPS);
    double[] sense = result.getSensitivities();
    assertEquals(0.0, sense[2], EPS);
    assertEquals(0.6, sense[3], EPS);
    assertEquals(0.4, sense[4], EPS);
    assertEquals(0.0, sense[5], EPS);

    result = INTERPOLATOR.interpolate(MODEL, 7.0);
    assertEquals(result.getResult(), FUNCTION.evaluate(7.0), EPS);
    sense = result.getSensitivities();

    assertEquals(0.0, sense[6], EPS);
    assertEquals(1.0, sense[7], EPS);
    assertEquals(0.0, sense[8], EPS);

  }

}
