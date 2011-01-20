/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import org.junit.Test;

/**
 * 
 */
public class Function2DTest {
  private static final Function2D<Double, Double> F = new Function2D<Double, Double>() {

    @Override
    public Double evaluate(final Double x1, final Double x2) {
      return 0.;
    }

  };

  @Test(expected = IllegalArgumentException.class)
  public void testNullArray() {
    F.evaluate((Double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyArray() {
    F.evaluate(new Double[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testShortArray() {
    F.evaluate(new Double[] {1.});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFirst() {
    F.evaluate(new Double[] {null, 1.});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSecond() {
    F.evaluate(new Double[] {1., null});
  }
}
