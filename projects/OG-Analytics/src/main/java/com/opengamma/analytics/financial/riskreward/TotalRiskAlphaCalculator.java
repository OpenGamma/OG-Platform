/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskreward;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * The total risk alpha measures the performance of an asset by comparing its
 * returns with those of a benchmark portfolio. The benchmark portfolio
 * represents the market risk matched to the total risk of the fund.
 * <p>
 * The total risk alpha is given by:
 * $$
 * \begin{eqnarray*}
 * TRA_i = R_i - \left(R_f + \frac{\mu_M - R_f}{\sigma_M}\sigma_i\right)
 * \end{eqnarray*}
 * $$
 * where $R_i$ is the asset return, $R_f$ is the risk-free return, $\mu_M$ is
 * the market return, $\sigma_M$ is the standard deviation of market returns
 * and $\sigma_i$ is the standard deviation of the asset returns.
 */
public class TotalRiskAlphaCalculator {
  private final DoubleTimeSeriesStatisticsCalculator _expectedAssetReturnCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _expectedRiskFreeReturnCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _expectedMarketReturnCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _marketStandardDeviationCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _assetStandardDeviationCalculator;

  public TotalRiskAlphaCalculator(final DoubleTimeSeriesStatisticsCalculator expectedAssetReturnCalculator,
      final DoubleTimeSeriesStatisticsCalculator expectedRiskFreeReturnCalculator, final DoubleTimeSeriesStatisticsCalculator expectedMarketReturnCalculator,
      final DoubleTimeSeriesStatisticsCalculator assetStandardDeviationCalculator, final DoubleTimeSeriesStatisticsCalculator marketStandardDeviationCalculator) {
    Validate.notNull(expectedAssetReturnCalculator, "expected asset return calculator");
    Validate.notNull(expectedRiskFreeReturnCalculator, "expected risk-free return calculator");
    Validate.notNull(expectedMarketReturnCalculator, "expected market return calculator");
    Validate.notNull(assetStandardDeviationCalculator, "asset standard deviation calculator");
    Validate.notNull(marketStandardDeviationCalculator, "market standard deviation calculator");
    _expectedAssetReturnCalculator = expectedAssetReturnCalculator;
    _expectedRiskFreeReturnCalculator = expectedRiskFreeReturnCalculator;
    _expectedMarketReturnCalculator = expectedMarketReturnCalculator;
    _assetStandardDeviationCalculator = assetStandardDeviationCalculator;
    _marketStandardDeviationCalculator = marketStandardDeviationCalculator;
  }

  /**
   * Calculates the total risk alpha.
   * @param assetReturnTS The return series of the asset
   * @param riskFreeReturnTS The risk-free return series 
   * @param marketReturnTS The return series of the market 
   * @return The total risk alpha
   */
  public double evaluate(final DoubleTimeSeries<?> assetReturnTS, final DoubleTimeSeries<?> riskFreeReturnTS, final DoubleTimeSeries<?> marketReturnTS) {
    Validate.notNull(assetReturnTS, "asset returns");
    Validate.notNull(riskFreeReturnTS, "risk-free returns");
    Validate.notNull(marketReturnTS, "market returns");
    final double assetReturn = _expectedAssetReturnCalculator.evaluate(assetReturnTS);
    final double marketReturn = _expectedMarketReturnCalculator.evaluate(marketReturnTS);
    final double riskFreeReturn = _expectedRiskFreeReturnCalculator.evaluate(riskFreeReturnTS);
    final double assetStandardDeviation = _assetStandardDeviationCalculator.evaluate(assetReturnTS);
    final double marketStandardDeviation = _marketStandardDeviationCalculator.evaluate(marketReturnTS);
    return assetReturn - (riskFreeReturn + (marketReturn - riskFreeReturn) * assetStandardDeviation / marketStandardDeviation);
  }
}
