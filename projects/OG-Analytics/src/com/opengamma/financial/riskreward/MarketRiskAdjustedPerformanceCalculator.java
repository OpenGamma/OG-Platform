/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

/**
 * The market-risk-adjusted performance (MRAP) measure is analogous to the risk-adjusted performance ratio (see {@link RiskAdjustedPerformanceCalculator}), 
 * with the risk measure changed to be the beta of the asset or portfolio to the market. 
 * <p>
 * This measure is defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * MRAP_i = R_i + \\left(\\frac{1}{\\beta_i} - 1\\right)(R_i - R_f)
 * \\end{eqnarray*}}
 * where {@latex.inline $R_i$} is the asset return, {@latex.inline $\\beta_i$} is the beta of the asset with respect to the market and {@latex.inline $R_f$}
 * is the risk-free return. 
 */
public class MarketRiskAdjustedPerformanceCalculator {

  /**
   * Calculates the market-risk-adjusted performance
   * @param assetReturn The return of the asset
   * @param riskFreeReturn The risk-free return
   * @param beta The beta of the asset with respect to the market
   * @return The market-risk-adjusted performance
   */
  public double calculate(final double assetReturn, final double riskFreeReturn, final double beta) {
    return assetReturn + (1. / beta - 1) * (assetReturn - riskFreeReturn);
  }
}
