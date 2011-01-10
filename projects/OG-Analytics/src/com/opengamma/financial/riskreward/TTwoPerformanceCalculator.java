/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskreward;

/**
 * By analogy to the M<sup>2</sup> measure, the T<sup>2</sup> measure gives the excess market-risk-adjusted performance (MRAP) of an asset over the market:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{eqnarray*}
 * T^2 = MRAP_i - MRAP_M
 * \\end{eqnarray*}} 
 * where {@latex.inline $MRAP_i$} is the market-risk-adjusted performance of the asset and {@latex.inline $MRAP_M$} is the market-risk-adjusted performance of the market.
 */
public class TTwoPerformanceCalculator {
  private static final MarketRiskAdjustedPerformanceCalculator MRAP = new MarketRiskAdjustedPerformanceCalculator();

  /**
   * Calculates the T<sup>2</sup>
   * @param assetReturn The return of the asset
   * @param riskFreeReturn The risk-free return
   * @param marketReturn The return of the market
   * @param beta The beta of the asset
   * @return The T<sup>2</sup>
   */
  public double calculate(final double assetReturn, final double riskFreeReturn, final double marketReturn, final double beta) {
    return MRAP.calculate(assetReturn, riskFreeReturn, beta) - marketReturn;
  }

}
