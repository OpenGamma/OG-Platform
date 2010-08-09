/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.math.function.RealPolynomialFunction1D;

/**
 * 
 */
public class Interpolator1DCubicSplineDataBundleTest {
  private static final RealPolynomialFunction1D CUBIC = new RealPolynomialFunction1D(new double[] {1, 3, 3, 1});
  private static final double[] X;
  private static final double[] Y;
  private static final Interpolator1DCubicSplineDataBundle DATA;
  private static final double EPS = 1e-12;

  static {
    final int n = 10;
    X = new double[n];
    Y = new double[n];
    for (int i = 0; i < n; i++) {
      X[i] = 2 * (i + 1);
      Y[i] = CUBIC.evaluate(X[i]);
    }
    DATA = new Interpolator1DCubicSplineDataBundle(new ArrayInterpolator1DDataBundle(X, Y));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    new Interpolator1DCubicSplineDataBundle(null);
  }

  @Test
  public void testGetters() {
    assertTrue(DATA.containsKey(2.));
    assertFalse(DATA.containsKey(3.4));
    assertEquals(DATA.firstKey(), 2., EPS);
    assertEquals(DATA.firstValue(), CUBIC.evaluate(2.), EPS);
    assertEquals(DATA.get(4.), CUBIC.evaluate(4.), EPS);
    assertArrayEquals(DATA.getKeys(), X, 0);
    assertEquals(DATA.getLowerBoundIndex(7.), 2);
    assertEquals(DATA.getLowerBoundKey(7.), 6, EPS);
    assertArrayEquals(DATA.getValues(), Y, EPS);
    assertEquals(DATA.higherKey(7.), 8, 0);
    assertEquals(DATA.higherValue(7.), CUBIC.evaluate(8.), EPS);
    assertEquals(DATA.lastKey(), 20., EPS);
    assertEquals(DATA.lastValue(), CUBIC.evaluate(20.), EPS);
    assertEquals(DATA.size(), 10);
    final InterpolationBoundedValues boundedValues = DATA.getBoundedValues(4.);
    assertEquals(boundedValues.getLowerBoundIndex(), 1);
    assertEquals(boundedValues.getLowerBoundKey(), 4., EPS);
    assertEquals(boundedValues.getLowerBoundValue(), CUBIC.evaluate(4.), EPS);
    assertEquals(boundedValues.getHigherBoundKey(), 6., EPS);
    assertEquals(boundedValues.getHigherBoundValue(), CUBIC.evaluate(6.), EPS);
  }

  @Test
  public void testSecondDerivatives() {
    final double[] y2 = DATA.getSecondDerivatives();
    assertEquals(y2.length, 10);
    assertEquals(y2[0], 0, EPS);
    assertEquals(y2[y2.length - 1], 0, EPS);
  }

  @Test
  public void testSecondDerivativesSensitivities() {
    //TODO
  }
}
