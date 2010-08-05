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
public class Interpolator1DDoubleQuadraticDataBundleTest {
  private static final RealPolynomialFunction1D QUADRATIC = new RealPolynomialFunction1D(new double[] {2, 3, 4});
  private static final Interpolator1DDoubleQuadraticDataBundle DATA;
  private static final double[] X;
  private static final double[] Y;
  private static final double EPS = 1e-12;

  static {
    final int n = 10;
    X = new double[n];
    Y = new double[n];
    for (int i = 0; i < n; i++) {
      X[i] = 3 * i;
      Y[i] = QUADRATIC.evaluate(X[i]);
    }
    DATA = new Interpolator1DDoubleQuadraticDataBundle(new ArrayInterpolator1DDataBundle(X, Y));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    new Interpolator1DCubicSplineDataBundle(null);
  }

  @Test
  public void testGetters() {
    assertTrue(DATA.containsKey(3.));
    assertFalse(DATA.containsKey(2.));
    assertEquals(DATA.firstKey(), 0., EPS);
    assertEquals(DATA.firstValue(), QUADRATIC.evaluate(0.), EPS);
    assertEquals(DATA.get(6.), QUADRATIC.evaluate(6.), EPS);
    assertArrayEquals(DATA.getKeys(), X, 0);
    assertEquals(DATA.getLowerBoundIndex(11.), 3);
    assertEquals(DATA.getLowerBoundKey(7.), 6, EPS);
    assertArrayEquals(DATA.getValues(), Y, EPS);
    assertEquals(DATA.higherKey(7.), 9, 0);
    assertEquals(DATA.higherValue(7.), QUADRATIC.evaluate(9.), EPS);
    assertEquals(DATA.lastKey(), 27., EPS);
    assertEquals(DATA.lastValue(), QUADRATIC.evaluate(27.), EPS);
    assertEquals(DATA.size(), 10);
    final InterpolationBoundedValues boundedValues = DATA.getBoundedValues(4.);
    assertEquals(boundedValues.getLowerBoundIndex(), 1);
    assertEquals(boundedValues.getLowerBoundKey(), 3., EPS);
    assertEquals(boundedValues.getLowerBoundValue(), QUADRATIC.evaluate(3.), EPS);
    assertEquals(boundedValues.getHigherBoundKey(), 6., EPS);
    assertEquals(boundedValues.getHigherBoundValue(), QUADRATIC.evaluate(6.), EPS);
  }

  @Test
  public void testQuadratics() {
    //TODO
  }
}
