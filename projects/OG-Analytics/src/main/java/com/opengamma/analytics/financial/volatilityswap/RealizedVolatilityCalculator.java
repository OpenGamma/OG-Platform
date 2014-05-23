/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.volatilityswap;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class RealizedVolatilityCalculator {

  /**
   * @param spotValues Spot values
   * @return realized variance
   */
  public double getRealizedVariance(final double[] spotValues) {
    ArgumentChecker.notNull(spotValues, "spotValues");
    final int nSpots = spotValues.length;
    ArgumentChecker.isTrue(nSpots > 1, "Number of spot values should be greater than 1");

    double res = 0.0;
    for (int i = 1; i < nSpots; ++i) {
      res += Math.pow(Math.log(spotValues[i] / spotValues[i - 1]), 2.0);
    }
    final double factor = 252.0 * 1.e4 / (nSpots - 1.0);
    res *= factor;
    return res;
  }

  /**
   * @param spotValues Spot Values
   * @return realized volatility
   */
  public double getRealizedVolatility(final double[] spotValues) {
    final double var = getRealizedVariance(spotValues);
    return Math.sqrt(var);
  }
}
