/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

/**
 * The Sharpe ratio is a measure of the excess return with respect to a benchmark per unit of risk in an asset or portfolio. It uses the standard deviation as the measure
 * of total risk. 
 * <p>
 * The Sharpe ratio is defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * S = \\frac{R - R_f}{\\sigma} = \\frac{E[R - R_f]}{\\sqrt{var[R - R_f]}}
 * \\end{eqnarray*}}
 * where {@latex.inline $R$} is the asset return, {@latex.inline $R_f$} is the return on the benchmark asset, {@latex.inline $E[R - R_f]$} is the expected value of the excess
 * of the asset return over the benchmark return and {@latex.inline $\\sigma$} is the standard deviation of the asset.
 */
public class SharpeRatioCalculator {

  /**
   * Calculates the Sharpe ratio
   * @param assetReturn The return of the asset
   * @param benchmarkReturn The return of the benchmark asset
   * @param standardDeviation The standard deviation of the return of the asset
   * @return The Sharpe ratio
   */
  public double calculate(final double assetReturn, final double benchmarkReturn, final double standardDeviation) {
    return (assetReturn - benchmarkReturn) / standardDeviation;
  }
}
