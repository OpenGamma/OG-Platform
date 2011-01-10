/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static org.junit.Assert.fail;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class MinimumBracketerTestCase {
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return null;
    }

  };

  public void testInputs(final MinimumBracketer bracketer) {
    try {
      bracketer.checkInputs(null, 1., 2.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      bracketer.checkInputs(F, 1., 1.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
