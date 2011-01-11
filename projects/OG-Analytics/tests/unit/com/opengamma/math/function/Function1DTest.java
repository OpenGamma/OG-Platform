/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import org.junit.Test;

public class Function1DTest {
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 0.;
    }

  };

  @Test(expected = IllegalArgumentException.class)
  public void testNullInputList() {
    F.evaluate((Double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyInputList() {
    F.evaluate(new Double[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInputListWithNulls() {
    F.evaluate(new Double[] {null});
  }

}
