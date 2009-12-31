/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive.robust;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;

/**
 * 
 * @author emcleod
 */
public class WinsorizedMeanCalculatorTest {
  private static final int N = 100;
  private static final Function1D<Double[], Double> CALCULATOR = new WinsorizedMeanCalculator(0.1);
  private static final Function1D<Double[], Double> MEAN = new MeanCalculator();
  private static final double EPS = 1e-12;

  @Test(expected = IllegalArgumentException.class)
  public void testLowGamma() {
    new WinsorizedMeanCalculator(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighGamma() {
    new WinsorizedMeanCalculator(1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArray() {
    CALCULATOR.evaluate((Double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyArray() {
    CALCULATOR.evaluate(new Double[0]);
  }

  @Test
  public void test() {
    final Double[] x = new Double[N];
    for (int i = 0; i < 10; i++) {
      x[i] = Double.valueOf(10);
    }
    for (int i = 10; i < N - 10; i++) {
      x[i] = Double.valueOf(i);
    }
    for (int i = N - 10; i < N; i++) {
      x[i] = Double.valueOf(N - 10 - 1);
    }
    assertEquals(CALCULATOR.evaluate(x), MEAN.evaluate(x), EPS);
    for (int i = 0; i < N - 1; i++) {
      x[i] = Double.valueOf(i);
    }
    x[N - 1] = 100000.;
    assertTrue(CALCULATOR.evaluate(x) < MEAN.evaluate(x));
  }
}
