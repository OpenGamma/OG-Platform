/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.timeseries.util.TimeSeriesDataTestUtils;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * The Treynor ratio is a measure of the excess return with respect to the risk-free rate per unit of systematic risk. The systematic risk is the beta of the 
 * asset or portfolio with respect to the asset.
 * <p>
 * The Treynor ratio is defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * T = \\frac{R_i - R_f}{\\beta_i}
 * \\end{eqnarray*}}   
 * where {@latex.inline $R_i$} is the asset return, {@latex.inline $R_f$} is the risk-free return and {@latex.inline $\\beta_i$} is the portfolio's beta.
 */
public class TreynorRatioCalculator {
  private final DoubleTimeSeriesStatisticsCalculator _expectedAssetReturnCalculator;
  private final DoubleTimeSeriesStatisticsCalculator _expectedRiskFreeReturnCalculator;

  public TreynorRatioCalculator(final DoubleTimeSeriesStatisticsCalculator expectedAssetReturnCalculator,
      final DoubleTimeSeriesStatisticsCalculator expectedRiskFreeReturnCalculator) {
    Validate.notNull(expectedAssetReturnCalculator, "expected asset return calculator");
    Validate.notNull(expectedRiskFreeReturnCalculator, "expected risk free return calculator");
    _expectedAssetReturnCalculator = expectedAssetReturnCalculator;
    _expectedRiskFreeReturnCalculator = expectedRiskFreeReturnCalculator;
  }

  /**
   * Calculates the Treynor ratio
   * @param assetReturnTS The asset price time series 
   * @param riskFreeReturnTS The risk-free return time series
   * @param beta The beta of the asset
   * @return The Treynor ratio
   */
  public double evaluate(final DoubleTimeSeries<?> assetReturnTS, final DoubleTimeSeries<?> riskFreeReturnTS, final double beta) {
    TimeSeriesDataTestUtils.testNotNullOrEmpty(assetReturnTS);
    TimeSeriesDataTestUtils.testNotNullOrEmpty(riskFreeReturnTS);
    final Double expectedAssetReturn = _expectedAssetReturnCalculator.evaluate(assetReturnTS);
    final Double expectedRiskFreeReturn = _expectedRiskFreeReturnCalculator.evaluate(riskFreeReturnTS);
    return (expectedAssetReturn - expectedRiskFreeReturn) / beta;
  }
}
