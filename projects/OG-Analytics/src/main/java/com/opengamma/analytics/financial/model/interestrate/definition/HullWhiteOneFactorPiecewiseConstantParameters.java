/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;

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
  private DoubleArrayList _volatility;
  /**
   * The times separating the constant volatility periods. The time should be sorted by increasing order. The first time is 0 and the last time is 1000 (represents infinity).
   * The extra time are added in the constructor.
   */
  private final DoubleArrayList _volatilityTime;
  /**
   * The time used to represent infinity.
   */
  private static final double VOLATILITY_TIME_INFINITY = 1000.0;

  /**
   * Constructor from the model parameters.
   * @param meanReversion The mean reversion speed (a) parameter.
   * @param volatility The volatility parameters.
   * @param volatilityTime The times separating the constant volatility periods.
   */
  public HullWhiteOneFactorPiecewiseConstantParameters(final double meanReversion, final double[] volatility, final double[] volatilityTime) {
    ArgumentChecker.notNull(volatility, "volatility time");
    ArgumentChecker.notNull(volatilityTime, "volatility time");
    _meanReversion = meanReversion;
    _volatility = new DoubleArrayList(volatility);
    final double[] volatilityTimeArray = new double[volatilityTime.length + 2];
    volatilityTimeArray[0] = 0.0;
    volatilityTimeArray[volatilityTime.length + 1] = VOLATILITY_TIME_INFINITY;
    System.arraycopy(volatilityTime, 0, volatilityTimeArray, 1, volatilityTime.length);
    _volatilityTime = new DoubleArrayList(volatilityTimeArray);
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
    return _volatility.toDoubleArray();
  }

  /**
   * Sets the volatility parameters.
   * @param volatility The volatility parameters.
   */
  public void setVolatility(final double[] volatility) {
    _volatility = new DoubleArrayList(volatility);
  }

  /**
   * Gets the times separating the constant volatility periods.
   * @return The times.
   */
  public double[] getVolatilityTime() {
    return _volatilityTime.toDoubleArray();
  }

  /**
   * Gets the last volatility of the volatility list.
   * @return The last volatility.
   */
  public double getLastVolatility() {
    return _volatility.get(_volatility.size() - 1);
  }

  /**
   * Sets the last volatility of the volatility list.
   * @param volatility The replacing volatility.
   */
  public void setLastVolatility(final double volatility) {
    _volatility.set(_volatility.size() - 1, volatility);
  }

  /**
   * Add an extra volatility and volatility time at the end of the list.
   * @param volatility The volatility.
   * @param volatilityTime The times separating the constant volatility periods. Must be larger than the previous one.
   */
  public void addVolatility(final double volatility, final double volatilityTime) {
    ArgumentChecker.isTrue(volatilityTime > _volatilityTime.get(_volatilityTime.size() - 2), "Volatility times should be increasing");
    _volatility.add(volatility);
    _volatilityTime.add(VOLATILITY_TIME_INFINITY);
    _volatilityTime.set(_volatilityTime.size() - 2, volatilityTime);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_meanReversion);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _volatility.hashCode();
    result = prime * result + _volatilityTime.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HullWhiteOneFactorPiecewiseConstantParameters)) {
      return false;
    }
    final HullWhiteOneFactorPiecewiseConstantParameters other = (HullWhiteOneFactorPiecewiseConstantParameters) obj;
    if (Double.doubleToLongBits(_meanReversion) != Double.doubleToLongBits(other._meanReversion)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatility, other._volatility)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatilityTime, other._volatilityTime)) {
      return false;
    }
    return true;
  }

}
