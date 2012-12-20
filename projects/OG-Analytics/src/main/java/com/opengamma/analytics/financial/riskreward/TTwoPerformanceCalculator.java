/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskreward;

/**
 * By analogy to the $M^2$ measure, the $T^2$ measure gives the excess
 * market-risk-adjusted performance ($MRAP$) of an asset over the market:
 * $$
 * \begin{eqnarray*}
 * T^2 = MRAP_i - MRAP_M
 * \end{eqnarray*}
 * $$
 * where $MRAP_i$ is the market-risk-adjusted performance of the asset and
 * $MRAP_M$ is the market-risk-adjusted performance of the market.
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
