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
public class SortinoRatioCalculatorTest {
  private static final double PERIODS_PER_YEAR = 1;
  private static final long[] T = new long[] {1};
  private static final double STD_DEV = 0.30;
  private static final DoubleTimeSeries<?> ASSET_RETURN = ImmutableInstantDoubleTimeSeries.of(T, new double[] {0.15});
  private static final DoubleTimeSeries<?> RISK_FREE = ImmutableInstantDoubleTimeSeries.of(T, new double[] {0.12});
  private static final DoubleTimeSeriesStatisticsCalculator EXCESS_RETURN = new DoubleTimeSeriesStatisticsCalculator(new Function<double[], Double>() {

    @Override
    public Double evaluate(final double[]... x) {
      return x[0][0];
    }

  });
  private static final DoubleTimeSeriesStatisticsCalculator STD = new DoubleTimeSeriesStatisticsCalculator(new Function<double[], Double>() {

    @Override
    public Double evaluate(final double[]... x) {
      return STD_DEV;
    }

  });
  private static final SharpeRatioCalculator SHARPE = new SharpeRatioCalculator(PERIODS_PER_YEAR, EXCESS_RETURN, STD);

  @Test
  public void test() {
    final double assetReturn = 0.15;
    final double benchmarkReturn = 0.12;
    final double standardDeviation = 0.30;
    assertEquals(new SortinoRatioCalculator().calculate(assetReturn, benchmarkReturn, standardDeviation), SHARPE.evaluate(ASSET_RETURN, RISK_FREE), 0);
  }
}
