/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

/**
 * The Treynor ratio is a measure of the excess return with respect to the risk-free rate per unit of systematic risk. The systematic risk is the beta of the 
 * asset or portfolio with respect to the market.
 * <p>
 * The Treynor ratio is defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * T = \\frac{R_i - R_f}{\\beta_i}
 * \\end{eqnarray*}}   
 * where {@latex.inline $R_i$} is the asset return, {@latex.inline $R_f$} is the risk-free return and {@latex.inline $\\beta_i$} is the portfolio's beta.
 */
public class TreynorRatioCalculator {

  /**
   * Calculates the Treynor ratio
   * @param assetReturn The return of the asset
   * @param riskFreeReturn The risk-free return
   * @param beta The beta of the asset
   * @return The Treynor ratio
   */
  public double calculator(final double assetReturn, final double riskFreeReturn, final double beta) {
    return (assetReturn - riskFreeReturn) / beta;
  }
}
