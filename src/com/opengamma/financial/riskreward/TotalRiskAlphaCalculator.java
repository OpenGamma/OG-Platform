/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

/**
 * The total risk alpha measures the performance of an asset by comparing its returns with those of a benchmark portfolio. The benchmark portfolio represents the
 * market risk matched to the total risk of the fund.
 * <p>
 * The total risk alpha is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * TRA_i = R_i - \\left(R_f + \\frac{\\mu_M - R_f}{\\sigma_M}\\sigma_i\\right)
 * \\end{eqnarray*}}
 * where {@latex.inline $R_i$} is the asset return, {@latex.inline $R_f$} is the risk-free return, {@latex.inline $\\mu_M$} is the market return, {@latex.inline $\\sigma_M$} is
 * the standard deviation of market returns and {@latex.inline $\\sigma_i$} is the standard deviation of the asset returns.
 */
public class TotalRiskAlphaCalculator {

  /**
   * Calculates the total risk alpha.
   * @param assetReturn The return of the asset
   * @param riskFreeReturn The risk-free return
   * @param marketReturn The return of the market
   * @param assetStandardDeviation The standard deviation of the asset returns
   * @param marketStandardDeviation The standard deviation of the market returns
   * @return The total risk alpha
   */
  public double calculate(final double assetReturn, final double riskFreeReturn, final double marketReturn, final double assetStandardDeviation, final double marketStandardDeviation) {
    return assetReturn - (riskFreeReturn + (marketReturn - riskFreeReturn) * assetStandardDeviation / marketStandardDeviation);
  }
}
