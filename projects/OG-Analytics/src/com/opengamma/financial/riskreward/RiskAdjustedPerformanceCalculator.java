/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

/**
 * The risk-adjusted performance (RAP) measure expresses the average return an asset or fund would have achieved if it had the same risk as the market.
 * The risk measure used is the standard deviation.
 * <p>
 * It is defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * RAP_i = \\frac{\\sigma_M}{\\sigma_i}(\\mu_i - R_f) + R_f
 * \\end{eqnarray*}}
 * where {@latex.inline $\\sigma_M$} is the standard deviation of the market returns, {@latex.inline $\\sigma_i$} is the standard deviation of the asset
 * returns, {@latex.inline $\\mu_i$} is the asset return and {@latex.inline $R_f$} is the risk-free return
 */
public class RiskAdjustedPerformanceCalculator {

  /**
   * Calculates the risk-adjusted performance
   * @param assetReturn The return of the asset
   * @param riskFreeReturn The risk-free return
   * @param assetStandardDeviation The standard deviation of the asset returns
   * @param marketStandardDeviation The standard deviation of the market returns
   * @return The risk-adjusted performance
   */
  public double calculate(final double assetReturn, final double riskFreeReturn, final double assetStandardDeviation, final double marketStandardDeviation) {
    return (assetReturn - riskFreeReturn) * marketStandardDeviation / assetStandardDeviation + riskFreeReturn;
  }
}
