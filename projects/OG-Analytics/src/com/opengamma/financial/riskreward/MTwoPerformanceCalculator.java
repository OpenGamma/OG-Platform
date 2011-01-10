/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

/**
 * The M<sup>2</sup> performance measure is the excess of the risk-adjusted performance measure (see {@link RiskAdjustedPerformanceCalculator} of the asset over that of the market.
 * <p>
 * It is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * M^2 = RAP_i - RAP_M
 * \\end{eqnarray*}}
 * where {@latex.inline $RAP_i$} is the risk-adjusted performance measure of the asset and {@latex.inline $RAP_M$} is the risk-adjusted performance measure of the market.
 */
public class MTwoPerformanceCalculator {
  private static final RiskAdjustedPerformanceCalculator RAP = new RiskAdjustedPerformanceCalculator();

  /**
   * Calculates the M<sup>2</sup>
   * @param assetReturn The return of the asset
   * @param riskFreeReturn The risk-free return
   * @param marketReturn The market return
   * @param assetStandardDeviation The standard deviation of the asset returns
   * @param marketStandardDeviation The standard deviation of the market returns
   * @return M<sup>2</sup>
   */
  public double calculate(final double assetReturn, final double riskFreeReturn, final double marketReturn, final double assetStandardDeviation, final double marketStandardDeviation) {
    return RAP.calculate(assetReturn, riskFreeReturn, assetStandardDeviation, marketStandardDeviation) - marketReturn;
  }
}
