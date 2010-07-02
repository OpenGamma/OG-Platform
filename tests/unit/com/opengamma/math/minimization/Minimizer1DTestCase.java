/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class Minimizer1DTestCase {
  private static final double EPS = 1e-5;
  private static final Function1D<Double, Double> QUADRATIC = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x + 7 * x + 12;
    }

  };
  private static final Function1D<Double, Double> QUINTIC = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 1 + x * (-3 + x * (-9 + x * (-1 + x * (4 + x))));
    }

  };

  public void testInputs(final Minimizer1D minimizer) {
    try {
      minimizer.minimize(null, new Double[] {2., 3.});
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      minimizer.minimize(QUADRATIC, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      minimizer.minimize(QUADRATIC, new Double[] {1.});
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  public void test(final Minimizer1D minimizer) {
    double[] result = minimizer.minimize(QUADRATIC, new Double[] {-10., 10.});
    assertEquals(result[0], -3.5, EPS);
    result = minimizer.minimize(QUINTIC, new Double[] {0.5, 2.});
    assertEquals(result[0], 1.06154, EPS);
  }
}
