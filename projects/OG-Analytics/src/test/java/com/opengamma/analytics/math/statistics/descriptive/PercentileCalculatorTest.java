/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PercentileCalculatorTest {
  private static final PercentileCalculator CALCULATOR = new PercentileCalculator(0.1);
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final int N = 100;
  private static final double[] X = new double[N];

  static {
    for (int i = 0; i < N; i++) {
      X[i] = RANDOM.nextDouble();
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighPercentile() {
    new PercentileCalculator(1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowPercentile() {
    new PercentileCalculator(0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetHighPercentile() {
    CALCULATOR.setPercentile(1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetLowPercentile() {
    CALCULATOR.setPercentile(0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    CALCULATOR.evaluate((double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyArray() {
    CALCULATOR.evaluate(new double[0]);
  }

  @Test
  public void testExtremes() {
    final double[] y = Arrays.copyOf(X, X.length);
    Arrays.sort(y);
    CALCULATOR.setPercentile(1e-15);
    assertEquals(CALCULATOR.evaluate(X), y[0], 0);
    CALCULATOR.setPercentile(1 - 1e-15);
    assertEquals(CALCULATOR.evaluate(X), y[N - 1], 0);
  }

  @Test
  public void test() {
    assertResult(X, 10);
    assertResult(X, 99);
    assertResult(X, 50);
  }

  private void assertResult(final double[] x, final int percentile) {
    final double[] copy = Arrays.copyOf(x, N);
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
