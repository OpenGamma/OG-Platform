/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AverageCalculatorTest {
  private static final double[] DATA = {1., 1., 3., 2.5, 5.7, 3.7, 5.7, 5.7, -4., 9.};
  private static final Function1D<double[], Double> MEAN = new MeanCalculator();
  private static final Function1D<double[], Double> MEDIAN = new MedianCalculator();
  private static final Function1D<double[], Double> MODE = new ModeCalculator();
  private static final double EPS = 1e-15;

  @Test
  public void testNull() {
    assertNull(MEAN);
    assertNull(MEDIAN);
    assertNull(MODE);
  }

  @Test
  public void testEmpty() {
    assertEmpty(MEAN);
    assertEmpty(MEDIAN);
    assertEmpty(MODE);
  }

  @Test
  public void testSingleValue() {
    final double value = 3.;
    final double[] x = {value};
    assertEquals(value, MEAN.evaluate(x), EPS);
    assertEquals(value, MEDIAN.evaluate(x), EPS);
    assertEquals(value, MODE.evaluate(x), EPS);
  }

  @Test
  public void testMean() {
    assertEquals(MEAN.evaluate(DATA), 3.33, EPS);
  }

  @Test
  public void testMedian() {
    assertEquals(MEDIAN.evaluate(DATA), 3.35, EPS);
    final double[] x = Arrays.copyOf(DATA, DATA.length - 1);
    assertEquals(MEDIAN.evaluate(x), 3, EPS);
  }

  @Test
  public void testMode() {
    final double[] x = {1., 2., 3., 4., 5., 6., 7., 8., 9., 10.};
    try {
      MODE.evaluate(x);
      Assert.fail();
    } catch (final MathException e) {
      // Expected
    }
    assertEquals(MODE.evaluate(DATA), 5.7, EPS);
  }

  private void assertNull(final Function1D<double[], Double> calculator) {
    try {
      calculator.evaluate((double[]) null);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  private void assertEmpty(final Function1D<double[], Double> calculator) {
    final double[] x = new double[0];
    try {
      calculator.evaluate(x);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
