/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.PolynomialFunction1D;

/**
 * 
 */
public class RationalFunctionInterpolator1DTest {
  //TODO this test doesn't test answers properly - look at EPS
  private static final Function1D<Double, Double> F = new PolynomialFunction1D(new double[] {-0.87, 3.4, 1., -5.});
  private static final Interpolator1D<Interpolator1DModel, InterpolationResult> INTERPOLATOR = new RationalFunctionInterpolator1D(3);
  private static final Interpolator1DModel MODEL;
  private static final double EPS = 1;

  static {
    final Map<Double, Double> data = new HashMap<Double, Double>();
    double x;
    for (int i = 0; i < 10; i++) {
      x = Double.valueOf(i) / 10.;
      data.put(x, F.evaluate(x));
    }
    MODEL = Interpolator1DModelFactory.fromMap(data);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullModel() {
    INTERPOLATOR.interpolate(null, 2.);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(MODEL, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientData() {
    new RationalFunctionInterpolator1D(10).interpolate(MODEL, 0.23);
  }

  @Test
  public void test() {
    assertEquals(F.evaluate(0.467), INTERPOLATOR.interpolate(MODEL, 0.467).getResult(), EPS);
  }
}
