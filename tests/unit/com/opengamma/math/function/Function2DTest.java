/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class Function2DTest {
  private static final Function2D<Double, Double> F = new Function2D<Double, Double>() {

    @Override
    public Double evaluate(final Double x1, final Double x2) {
      return 0.;
    }

  };

  @Test
  public void test() {
    try {
      F.evaluate((Double[]) null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      F.evaluate(new Double[0]);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      F.evaluate(new Double[] { 1. });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      F.evaluate(new Double[] { null, 1. });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      F.evaluate(new Double[] { 1., null });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
