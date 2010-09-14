/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

/**
 * The Jensen alpha computes the abnormal return of an asset or portfolio over the theoretical expected return i.e. the difference between the average return
 * of the asset and the average return of a benchmark portfolio whose market risk is the same as that of the fund.
 * <p>
 * The Jensen alpha is defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * \\alpha_J = R_i - [R_f + \\beta_{iM}(R_M - R_f)]
 * \\end{eqnarray*}}
 * where {@latex.inline $R_i$} is the asset return, {@latex.inline $R_f$} is the risk-free rate, {@latex.inline $\\beta_{iM}$} is the beta of the asset with respect
 * to the market and {@latex.inline $R_M$} is the market return. 
 */
public class JensenAlphaCalculator {

  /**
   * Calculates Jensen's alpha
   * @param assetReturn The return of the asset
   * @param riskFreeReturn The risk-free return 
   * @param beta The beta of the asset to the market
   * @param marketReturn The market return
   * @return The Jensen alpha
   */
  public double calculate(final double assetReturn, final double riskFreeReturn, final double beta, final double marketReturn) {
    return assetReturn - riskFreeReturn * (1 - beta) - beta * marketReturn;
  }
}
