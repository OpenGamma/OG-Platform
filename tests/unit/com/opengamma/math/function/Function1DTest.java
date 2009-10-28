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
public class Function1DTest {
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
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
      F.evaluate(new Double[] { null });
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
