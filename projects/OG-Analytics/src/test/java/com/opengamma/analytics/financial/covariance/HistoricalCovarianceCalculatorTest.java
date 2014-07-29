/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HistoricalCovarianceCalculatorTest {
  private static final DoubleTimeSeries<?> TS1 = ImmutableInstantDoubleTimeSeries.of(new long[] {1, 2, 3, 4, 5}, new double[] {1, 1, 1, 1, 1});
  private static final CovarianceCalculator CALCULATOR = new HistoricalCovarianceCalculator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    CALCULATOR.evaluate((DoubleTimeSeries<?>[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongSizeArray() {
    CALCULATOR.evaluate(TS1);
  }

  @Test
  public void test() {
    final double n = TS1.size();
    final double covariance = CALCULATOR.evaluate(TS1, TS1);
    assertEquals(covariance, n / (n - 1) - 1, 1e-9);
  }
}
