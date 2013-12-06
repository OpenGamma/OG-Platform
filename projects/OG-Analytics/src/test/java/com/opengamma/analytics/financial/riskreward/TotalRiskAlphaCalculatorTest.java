/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskreward;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TotalRiskAlphaCalculatorTest {
  private static final long[] T = new long[] {1};
  private static final double ASSET_STD_DEV = 0.15;
  private static final double MARKET_STD_DEV = 0.17;
  private static final DoubleTimeSeries<?> ASSET_RETURN = ImmutableInstantDoubleTimeSeries.of(T, new double[] {0.12});
  private static final DoubleTimeSeries<?> RISK_FREE = ImmutableInstantDoubleTimeSeries.of(T, new double[] {0.03});
  private static final DoubleTimeSeries<?> MARKET_RETURN = ImmutableInstantDoubleTimeSeries.of(T, new double[] {0.11});
  private static final DoubleTimeSeriesStatisticsCalculator RETURN = new DoubleTimeSeriesStatisticsCalculator(new Function<double[], Double>() {

    @Override
    public Double evaluate(final double[]... x) {
      return x[0][0];
    }

  });
  private static final DoubleTimeSeriesStatisticsCalculator ASSET_STD = new DoubleTimeSeriesStatisticsCalculator(new Function<double[], Double>() {

    @Override
    public Double evaluate(final double[]... x) {
      return ASSET_STD_DEV;
    }

  });
  private static final DoubleTimeSeriesStatisticsCalculator MARKET_STD = new DoubleTimeSeriesStatisticsCalculator(new Function<double[], Double>() {

    @Override
    public Double evaluate(final double[]... x) {
      return MARKET_STD_DEV;
    }

  });
  private static final TotalRiskAlphaCalculator CALCULATOR = new TotalRiskAlphaCalculator(RETURN, RETURN, RETURN, ASSET_STD, MARKET_STD);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new TotalRiskAlphaCalculator(null, RETURN, RETURN, ASSET_STD, ASSET_STD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    new TotalRiskAlphaCalculator(RETURN, null, RETURN, ASSET_STD, ASSET_STD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator3() {
    new TotalRiskAlphaCalculator(RETURN, RETURN, null, ASSET_STD, ASSET_STD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator4() {
    new TotalRiskAlphaCalculator(RETURN, RETURN, RETURN, null, ASSET_STD);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator5() {
    new TotalRiskAlphaCalculator(RETURN, RETURN, RETURN, ASSET_STD, null);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(ASSET_RETURN, RISK_FREE, MARKET_RETURN), 0.0194, 1e-4);
  }
}
