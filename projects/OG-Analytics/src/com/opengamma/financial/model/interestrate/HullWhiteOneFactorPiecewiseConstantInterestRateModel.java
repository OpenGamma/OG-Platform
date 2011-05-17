/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.future.InterestRateFutureSecurity;

/**
 * Description of the parameters used in the Hull-White one factor model with piecewise constant volatility.
 */
public class HullWhiteOneFactorPiecewiseConstantInterestRateModel {

  /**
   * The mean reversion speed (a) parameter.
   */
  private final double _meanReversion;
  /**
   * The volatility parameters. The volatility is constant between the volatility times. Volatility in t is _volatility[i] for t between _volatilityTime[i] and _volatilityTime[i+1].
   */
  private final double[] _volatility;
  /**
   * The times separating the constant volatility periods. The time should be sorted by increasing order. The first time is 0 and the last time is 1000 (represents infinity). 
   * The extra time are added in the constructor.
   */
  private final double[] _volatilityTime;

  /**
   * Constructor from the model parameters.
   * @param meanReversion The mean reversion speed (a) parameter.
   * @param volatility The volatility parameters. 
   * @param volatilityTime The times separating the constant volatility periods.
   */
  public HullWhiteOneFactorPiecewiseConstantInterestRateModel(final double meanReversion, final double[] volatility, final double[] volatilityTime) {
    Validate.notNull(volatility, "volatility time");
    Validate.notNull(volatilityTime, "volatility time");
    this._meanReversion = meanReversion;
    this._volatility = volatility;
    this._volatilityTime = new double[volatilityTime.length + 2];
    _volatilityTime[0] = 0.0;
    System.arraycopy(volatilityTime, 0, _volatilityTime, 1, volatilityTime.length);
    _volatilityTime[volatilityTime.length + 1] = 1000.0;
    // TODO: check that the time are increasing.
  }

  /**
   * Gets the mean reversion speed (a) parameter.
   * @return The mean reversion speed (a) parameter.
   */
  public double getMeanReversion() {
    return _meanReversion;
  }

  /**
   * Gets the volatility parameters. 
   * @return The volatility parameters. 
   */
  public double[] getVolatility() {
    return _volatility;
  }

  /**
   * Gets the times separating the constant volatility periods.
   * @return The times.
   */
  public double[] getVolatilityTime() {
    return _volatilityTime;
  }

  /**
   * Computes the future convexity factor (called gamma in the article) used in future pricing.
   * @param future The future security.
   * @return The factor.
   */
  public double futureConvexityFactor(InterestRateFutureSecurity future) {
    double t0 = future.getLastTradingTime();
    double t1 = future.getFixingPeriodStartTime();
    double t2 = future.getFixingPeriodEndTime();
    double factor1 = Math.exp(-_meanReversion * t1) - Math.exp(-_meanReversion * t2);
    double numerator = 2 * _meanReversion * _meanReversion * _meanReversion;
    int indexT0 = 1; // Period in which the time t0 is; _volatilityTime[i-1] <= t0 < _volatilityTime[i];
    while (t0 > _volatilityTime[indexT0]) {
      indexT0++;
    }
    double[] s = new double[indexT0 + 1];
    System.arraycopy(_volatilityTime, 0, s, 0, indexT0);
    s[indexT0] = t0;
    double factor2 = 0.0;
    for (int loopperiod = 0; loopperiod < indexT0; loopperiod++) {
      factor2 += _volatility[loopperiod] * _volatility[loopperiod] * (Math.exp(_meanReversion * s[loopperiod + 1]) - Math.exp(_meanReversion * s[loopperiod]))
          * (2 - Math.exp(-_meanReversion * (t2 - s[loopperiod + 1])) - Math.exp(-_meanReversion * (t2 - s[loopperiod])));
    }
    return Math.exp(factor1 / numerator * factor2);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_meanReversion);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_volatility);
    result = prime * result + Arrays.hashCode(_volatilityTime);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    HullWhiteOneFactorPiecewiseConstantInterestRateModel other = (HullWhiteOneFactorPiecewiseConstantInterestRateModel) obj;
    if (Double.doubleToLongBits(_meanReversion) != Double.doubleToLongBits(other._meanReversion)) {
      return false;
    }
    if (!Arrays.equals(_volatility, other._volatility)) {
      return false;
    }
    if (!Arrays.equals(_volatilityTime, other._volatilityTime)) {
      return false;
    }
    return true;
  }

}
