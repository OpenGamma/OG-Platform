/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class AverageCalculatorTest {
  private static final double[] DATA = {1., 1., 3., 2.5, 5.7, 3.7, 5.7, 5.7, -4., 9.};
  private static final Function1D<double[], Double> MEAN = new MeanCalculator();
  private static final Function1D<double[], Double> MEDIAN = new MedianCalculator();
  private static final Function1D<double[], Double> MODE = new ModeCalculator();
  private static final double EPS = 1e-15;

  @Test
  public void testNull() {
    testNull(MEAN);
    testNull(MEDIAN);
    testNull(MODE);
  }

  @Test
  public void testEmpty() {
    testEmpty(MEAN);
    testEmpty(MEDIAN);
    testEmpty(MODE);
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
      fail();
    } catch (final MathException e) {
      // Expected
    }
    assertEquals(MODE.evaluate(DATA), 5.7, EPS);
  }

  private void testNull(final Function1D<double[], Double> calculator) {
    try {
      calculator.evaluate((double[]) null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  private void testEmpty(final Function1D<double[], Double> calculator) {
    final double[] x = new double[0];
    try {
      calculator.evaluate(x);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
