/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive.robust;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TrimmedMeanCalculatorTest {
  private static final int N = 100;
  private static final Function1D<double[], Double> CALCULATOR = new TrimmedMeanCalculator(0.1);
  private static final Function1D<double[], Double> MEAN = new MeanCalculator();
  private static final double EPS = 1e-12;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowGamma() {
    new WinsorizedMeanCalculator(-0.3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighGamma() {
    new WinsorizedMeanCalculator(1.3);
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
  public void test() {
    final double[] x = new double[N];
    final double[] y = new double[N - 20];
    for (int i = 0; i < 10; i++) {
      x[i] = 0.;
    }
    for (int i = 10; i < N - 10; i++) {
      x[i] = Double.valueOf(i);
      y[i - 10] = Double.valueOf(i);
    }
    for (int i = N - 10; i < N; i++) {
      x[i] = 0.;
    }
    assertEquals(CALCULATOR.evaluate(x), MEAN.evaluate(y), EPS);
    for (int i = 0; i < N - 1; i++) {
      x[i] = Double.valueOf(i);
    }
    x[N - 1] = 100000.;
    assertTrue(CALCULATOR.evaluate(x) < MEAN.evaluate(x));
  }
}
