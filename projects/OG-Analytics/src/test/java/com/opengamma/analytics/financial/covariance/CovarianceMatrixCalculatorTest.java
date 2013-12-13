/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CovarianceMatrixCalculatorTest {
  private static final DoubleTimeSeries<?> TS1 = ImmutableInstantDoubleTimeSeries.of(new long[] {1, 2, 3, 4}, new double[] {-1, 1, -1, 1});
  private static final DoubleTimeSeries<?> TS2 = TS1.multiply(-1);
  private static final CovarianceCalculator COVARIANCE = new HistoricalCovarianceCalculator();
  private static final Function<DoubleTimeSeries<?>, DoubleMatrix2D> CALCULATOR = new CovarianceMatrixCalculator(COVARIANCE);
  private static final double EPS = 1e-9;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    new CovarianceMatrixCalculator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTSArray() {
    CALCULATOR.evaluate((DoubleTimeSeries<?>[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
