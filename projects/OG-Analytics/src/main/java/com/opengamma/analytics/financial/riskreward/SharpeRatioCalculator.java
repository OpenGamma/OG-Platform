/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskreward;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.financial.timeseries.util.TimeSeriesDataTestUtils;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * The Sharpe ratio is a measure of the excess return with respect to a
 * benchmark per unit of risk of an asset or portfolio. It uses the standard
 * deviation as the measure of total risk. 
 * <p>
 * The Sharpe ratio is defined as:
 * $$
 * \begin{eqnarray*}
 * S = \frac{R - R_f}{\sigma} = \frac{E[R - R_f]}{\sqrt{var[R - R_f]}}
 * \end{eqnarray*}
 * $$
 * where $R$ is the asset return, $R_f$ is the return on the benchmark asset,
 * $E[R - R_f]$ is the expected value of the excess of the asset return over
 * the benchmark return and $\sigma$ is the standard deviation of the asset.
 */
public class SharpeRatioCalculator implements Function<DoubleTimeSeries<?>, Double> {
  private final double _returnPeriodsPerYear;
  private final DoubleTimeSeriesStatisticsCalculator _expectedExcessReturnCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _standardDeviationCalculator;

  public SharpeRatioCalculator(final double returnPeriodsPerYear, final DoubleTimeSeriesStatisticsCalculator expectedExcessReturnCalculator,
      final DoubleTimeSeriesStatisticsCalculator standardDeviationCalculator) {
    Validate.isTrue(returnPeriodsPerYear > 0);
    Validate.notNull(expectedExcessReturnCalculator, "expected excess return calculator");
    Validate.notNull(standardDeviationCalculator, "standard deviation calculator");
    _returnPeriodsPerYear = returnPeriodsPerYear;
    _expectedExcessReturnCalculator = expectedExcessReturnCalculator;
    _standardDeviationCalculator = standardDeviationCalculator;
  }

  /**
   * Calculates the annualized Sharpe ratio
   * @param ts An array of time series where the first element is the return of the asset and the second is the return of the benchmark
   * @return The Sharpe ratio
   * @throws IllegalArgumentException If the array is null, doesn't contain two elements or if either of the elements is null
   */
  @Override
  public Double evaluate(final DoubleTimeSeries<?>... ts) {
    Validate.notNull(ts, "ts array");
    TimeSeriesDataTestUtils.testNotNullOrEmpty(ts[0]);
    TimeSeriesDataTestUtils.testNotNullOrEmpty(ts[1]);
    final DoubleTimeSeries<?> excessReturn = ts[0].subtract(ts[1]); //TODO change when we have proper excess return calculators
    final double assetExcessReturn = _expectedExcessReturnCalculator.evaluate(excessReturn) * _returnPeriodsPerYear;
    final double standardDeviation = _standardDeviationCalculator.evaluate(excessReturn) * Math.sqrt(_returnPeriodsPerYear);
    return assetExcessReturn / standardDeviation;
  }
}
