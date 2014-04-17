/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SampleCovarianceCalculatorTest {
  private static final double COV1 = 0;
  private static final double VAR0;
  private static final double[] X0;
  private static final double[] X1;
  private static final SampleCovarianceCalculator CALCULATOR = new SampleCovarianceCalculator();
  private static final double EPS = 1e-12;

  static {
    final int n = 1000;
    X0 = new double[n];
    X1 = new double[n];
    for (int i = 0; i < n; i++) {
      X0[i] = Math.random() - 0.5;
      X1[i] = X0[i] * COV1;
    }
    final SampleVarianceCalculator calc = new SampleVarianceCalculator();
    VAR0 = calc.evaluate(X0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS() {
    CALCULATOR.evaluate((double[][]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOneTS() {
    CALCULATOR.evaluate(X0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOneShortTS() {
    CALCULATOR.evaluate(new double[] {1}, X1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthTS() {
    CALCULATOR.evaluate(new double[] {1, 2, 3, 4, 5, 6, 7, 8}, X1);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(X0, X0) / VAR0, 1, EPS);
    assertEquals(CALCULATOR.evaluate(X0, X1), COV1, EPS);
  }
}
