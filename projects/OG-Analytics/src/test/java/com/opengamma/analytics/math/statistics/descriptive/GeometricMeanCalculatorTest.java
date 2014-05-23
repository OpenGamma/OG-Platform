/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GeometricMeanCalculatorTest {
  private static final Function1D<double[], Double> ARITHMETIC = new MeanCalculator();
  private static final Function1D<double[], Double> GEOMETRIC = new GeometricMeanCalculator();
  private static final int N = 100;
  private static final double[] FLAT = new double[N];
  private static final double[] X = new double[N];
  private static final double[] LN_X = new double[N];

  static {
    for (int i = 0; i < N; i++) {
      FLAT[i] = 2;
      X[i] = Math.random();
      LN_X[i] = Math.log(X[i]);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    GEOMETRIC.evaluate((double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyArray() {
    GEOMETRIC.evaluate(new double[0]);
  }

  @Test
  public void test() {
    assertEquals(GEOMETRIC.evaluate(FLAT), 2, 0);
    assertEquals(GEOMETRIC.evaluate(X), Math.exp(ARITHMETIC.evaluate(LN_X)), 1e-15);
  }
}
