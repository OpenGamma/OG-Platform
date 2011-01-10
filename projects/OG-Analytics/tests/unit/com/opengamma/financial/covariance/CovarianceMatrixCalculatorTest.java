/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;

/**
 * 
 */
public class CovarianceMatrixCalculatorTest {
  private static final DoubleTimeSeries<?> TS1 = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 2, 3, 4}, new double[] {-1, 1, -1, 1});
  private static final DoubleTimeSeries<?> TS2 = TS1.multiply(-1);
  private static final CovarianceCalculator COVARIANCE = new HistoricalCovarianceCalculator();
  private static final Function<DoubleTimeSeries<?>, DoubleMatrix2D> CALCULATOR = new CovarianceMatrixCalculator(COVARIANCE);
  private static final double EPS = 1e-9;

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    new CovarianceMatrixCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTSArray() {
    CALCULATOR.evaluate((DoubleTimeSeries<?>[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTSArray() {
    CALCULATOR.evaluate(new DoubleTimeSeries<?>[0]);
  }

  @Test
  public void test() {
    final DoubleMatrix2D matrix = CALCULATOR.evaluate(TS1, TS2);
    assertEquals(matrix.getNumberOfRows(), 2);
    assertEquals(matrix.getNumberOfColumns(), 2);
    assertEquals(matrix.getEntry(0, 0), 4. / 3, EPS);
    assertEquals(matrix.getEntry(1, 0), -4. / 3, EPS);
    assertEquals(matrix.getEntry(0, 1), -4. / 3, EPS);
    assertEquals(matrix.getEntry(1, 1), 4. / 3, EPS);
  }
}
