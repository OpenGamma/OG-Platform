/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

/**
 * 
 */
public class PercentileCalculatorTest {
  private static final PercentileCalculator CALCULATOR = new PercentileCalculator(0.1);
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final int N = 100;
  private static final Double[] X = new Double[N];

  static {
    for (int i = 0; i < N; i++) {
      X[i] = RANDOM.nextDouble();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighPercentile() {
    new PercentileCalculator(1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowPercentile() {
    new PercentileCalculator(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetHighPercentile() {
    CALCULATOR.setPercentile(1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetLowPercentile() {
    CALCULATOR.setPercentile(0);
  }

  @Test(expected = NullPointerException.class)
  public void testNullArray() {
    CALCULATOR.evaluate((Double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyArray() {
    CALCULATOR.evaluate(new Double[0]);
  }

  @Test
  public void testExtremes() {
    final Double[] y = Arrays.copyOf(X, X.length);
    Arrays.sort(y);
    CALCULATOR.setPercentile(1e-15);
    assertEquals(CALCULATOR.evaluate(X), y[0], 0);
    CALCULATOR.setPercentile(1 - 1e-15);
    assertEquals(CALCULATOR.evaluate(X), y[N - 1], 0);
  }

  @Test
  public void test() {
    testResult(X, 10);
    testResult(X, 99);
    testResult(X, 50);
  }

  private void testResult(final Double[] x, final int percentile) {
    final Double[] copy = Arrays.copyOf(x, N);
    Arrays.sort(copy);
    int count = 0;
    CALCULATOR.setPercentile(((double) percentile) / N);
    final double value = CALCULATOR.evaluate(x);
    while (copy[count++] < value) {
      //intended
    }
    assertEquals(count - 1, percentile);
  }
}
