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
 * The Jensen alpha computes the abnormal return of an asset or portfolio over
 * the theoretical expected return i.e. the difference between the average
 * return of the asset and the average return of a benchmark portfolio whose
 * market risk is the same as that of the fund.
 * <p>
 * The Jensen alpha is defined as:
 * $$
 * \begin{eqnarray*}
 * \alpha_J = R_i - [R_f + \beta_{iM}(R_M - R_f)]
 * \end{eqnarray*}
 * $$
 * where $R_i$ is the asset return, $R_f$ is the risk-free rate, $\beta_{iM}$
 * is the beta of the asset with respect to the market and $R_M$ is the market
 * return. 
 */
public class JensenAlphaCalculator {
  private final DoubleTimeSeriesStatisticsCalculator _expectedAssetReturnCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _expectedRiskFreeReturnCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _expectedMarketReturnCalculator;

  public JensenAlphaCalculator(final DoubleTimeSeriesStatisticsCalculator expectedAssetReturnCalculator,
      final DoubleTimeSeriesStatisticsCalculator expectedRiskFreeReturnCalculator, final DoubleTimeSeriesStatisticsCalculator expectedMarketReturnCalculator) {
    Validate.notNull(expectedAssetReturnCalculator, "expected asset return calculator");
    Validate.notNull(expectedRiskFreeReturnCalculator, "expected risk free return calculator");
    Validate.notNull(expectedMarketReturnCalculator, "expected market return calculator");
    _expectedAssetReturnCalculator = expectedAssetReturnCalculator;
    _expectedRiskFreeReturnCalculator = expectedRiskFreeReturnCalculator;
    _expectedMarketReturnCalculator = expectedMarketReturnCalculator;
  }

  /**
   * Calculates Jensen's alpha
   * @param assetReturnTS The return time series of the asset
   * @param riskFreeReturnTS The risk-free return series 
   * @param beta The beta of the asset to the market
   * @param marketReturnTS The market return series
   * @return The Jensen alpha
   */
  public double evaluate(final DoubleTimeSeries<?> assetReturnTS, final DoubleTimeSeries<?> riskFreeReturnTS, final double beta, final DoubleTimeSeries<?> marketReturnTS) {
    Validate.notNull(assetReturnTS, "asset return time series");
    Validate.notNull(riskFreeReturnTS, "risk-free return time series");
    Validate.notNull(marketReturnTS, "market return time series");
    final double assetReturn = _expectedAssetReturnCalculator.evaluate(assetReturnTS);
    final double riskFreeReturn = _expectedRiskFreeReturnCalculator.evaluate(riskFreeReturnTS);
    final double marketReturn = _expectedMarketReturnCalculator.evaluate(marketReturnTS);
    return assetReturn - riskFreeReturn * (1 - beta) - beta * marketReturn;
  }
}
