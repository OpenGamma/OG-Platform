/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import com.opengamma.financial.interestrate.future.InterestRateFutureSecurity;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;

/**
 * Methods related to the Hull-White one factor (extended Vasicek) model with piecewise constant volatility.
 */
public class HullWhiteOneFactorPiecewiseConstantInterestRateModel {

  /**
   * Computes the future convexity factor used in future pricing.
   * TODO: add a reference.
   * @param future The future security.
   * @param data The Hull-White model paramters.
   * @return The factor.
   */
  public double futureConvexityFactor(final InterestRateFutureSecurity future, final HullWhiteOneFactorPiecewiseConstantDataBundle data) {
    double t0 = future.getLastTradingTime();
    double t1 = future.getFixingPeriodStartTime();
    double t2 = future.getFixingPeriodEndTime();
    double factor1 = Math.exp(-data.getMeanReversion() * t1) - Math.exp(-data.getMeanReversion() * t2);
    double numerator = 2 * data.getMeanReversion() * data.getMeanReversion() * data.getMeanReversion();
    int indexT0 = 1; // Period in which the time t0 is; _volatilityTime[i-1] <= t0 < _volatilityTime[i];
    while (t0 > data.getVolatilityTime()[indexT0]) {
      indexT0++;
    }
    double[] s = new double[indexT0 + 1];
    System.arraycopy(data.getVolatilityTime(), 0, s, 0, indexT0);
    s[indexT0] = t0;
    double factor2 = 0.0;
    for (int loopperiod = 0; loopperiod < indexT0; loopperiod++) {
      factor2 += data.getVolatility()[loopperiod] * data.getVolatility()[loopperiod] * (Math.exp(data.getMeanReversion() * s[loopperiod + 1]) - Math.exp(data.getMeanReversion() * s[loopperiod]))
          * (2 - Math.exp(-data.getMeanReversion() * (t2 - s[loopperiod + 1])) - Math.exp(-data.getMeanReversion() * (t2 - s[loopperiod])));
    }
    return Math.exp(factor1 / numerator * factor2);
  }

}
