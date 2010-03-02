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
 * @author emcleod
 */
public class PercentileCalculatorTest {
  private static final PercentileCalculator CALCULATOR = new PercentileCalculator(0.1);
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);

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
    final int n = 100;
    final Double[] x = new Double[n];
    for (int i = 0; i < n; i++) {
      x[i] = RANDOM.nextDouble();
    }
    testResult(x, 10, n);
    testResult(x, 99, n);
    testResult(x, 50, n);
  }

  private void testResult(final Double[] x, final int percentile, final int n) {
    final Double[] copy = Arrays.copyOf(x, n);
    Arrays.sort(copy);
    int count = 0;
    CALCULATOR.setPercentile(((double) percentile) / n);
    final double value = CALCULATOR.evaluate(x);
    while (copy[count++] < value) {
    }
    assertEquals(count - 1, percentile);
  }
}
