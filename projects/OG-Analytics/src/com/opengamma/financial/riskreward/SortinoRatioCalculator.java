/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

import com.opengamma.math.statistics.descriptive.SemiStandardDeviationCalculator;

/**
 * The Sortino ratio is an extension of the Sharpe ratio (see {@link SharpeRatioCalculator}) that does not penalize an asset or portfolio for upside volatility. It is the actual rate
 * of return in excess of the benchmark rate of return per unit of downside risk.
 * <p>
 * It is defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * S = \\frac{R_i - R_f}{\\sigma_{down}}
 * \\end{eqnarray*}}
 * where {@latex.inline $R_i$} is the return of the asset, {@latex.inline $R_f$} is the return on a benchmark asset and {@latex.inline $\\sigma_{down}$} is the downside
 * volatility (semi-standard deviation - see {@link SemiStandardDeviationCalculator}). 
 */
public class SortinoRatioCalculator {

  /**
   * Calculates the Sortino ratio
   * @param assetReturn The return of the asset
   * @param benchmarkReturn The return of the benchmark asset
   * @param downsideVolatility The downside volatility
   * @return The Sortino ratio
   */
  public double calculate(final double assetReturn, final double benchmarkReturn, final double downsideVolatility) {
    return (assetReturn - benchmarkReturn) / downsideVolatility;
  }
}
