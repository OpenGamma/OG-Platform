/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class GaussianQuadratureFunctionTest {
  private static final double[] X = new double[] {1, 2, 3, 4};
  private static final double[] W = new double[] {6, 7, 8, 9};
  private static final GaussianQuadratureFunction F = new GaussianQuadratureFunction(X, W);

  @Test(expected = IllegalArgumentException.class)
  public void testNullAbscissas() {
    new GaussianQuadratureFunction(null, W);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullWeights() {
    new GaussianQuadratureFunction(X, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongLength() {
    new GaussianQuadratureFunction(X, new double[] {1, 2, 3});
  }

  @Test
  public void test() {
    GaussianQuadratureFunction other = new GaussianQuadratureFunction(X, W);
    assertEquals(F, other);
    assertEquals(F.hashCode(), other.hashCode());
    other = new GaussianQuadratureFunction(W, W);
    assertFalse(F.equals(other));
    other = new GaussianQuadratureFunction(X, X);
    assertFalse(F.equals(other));
    assertArrayEquals(F.getAbscissas(), X, 0);
    assertArrayEquals(F.getWeights(), W, 0);
  }
}
