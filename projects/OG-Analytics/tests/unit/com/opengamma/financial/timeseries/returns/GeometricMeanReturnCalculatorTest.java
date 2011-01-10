/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class GeometricMeanReturnCalculatorTest {
  private static final Function1D<double[], Double> SIMPLE = new SimplyCompoundedGeometricMeanReturnCalculator();
  private static final Function1D<double[], Double> CONTINUOUS = new ContinuouslyCompoundedGeometricMeanReturnCalculator();

  @Test(expected = IllegalArgumentException.class)
  public void testNull1() {
    SIMPLE.evaluate((double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty1() {
    SIMPLE.evaluate(new double[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull2() {
    CONTINUOUS.evaluate((double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty2() {
    CONTINUOUS.evaluate(new double[0]);
  }

  @Test
  public void test() {
    final int n = 100;
    final double[] x = new double[n];
    final double r = Math.random();
    for (int i = 0; i < n; i++) {
      x[i] = r;
    }
    assertEquals(SIMPLE.evaluate(x), r, 1e-12);
    assertEquals(CONTINUOUS.evaluate(x), r, 1e-12);
  }
}
