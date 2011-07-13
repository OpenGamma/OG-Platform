/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * Data bundle related to the Hull-White one factor (extended Vasicek) model with piecewise constant volatility.
 */
public class HullWhiteOneFactorPiecewiseConstantParameters {

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
  public HullWhiteOneFactorPiecewiseConstantParameters(final double meanReversion, final double[] volatility, final double[] volatilityTime) {
    Validate.notNull(volatility, "volatility time");
    Validate.notNull(volatilityTime, "volatility time");
    _meanReversion = meanReversion;
    _volatility = volatility;
    _volatilityTime = new double[volatilityTime.length + 2];
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
    HullWhiteOneFactorPiecewiseConstantParameters other = (HullWhiteOneFactorPiecewiseConstantParameters) obj;
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
