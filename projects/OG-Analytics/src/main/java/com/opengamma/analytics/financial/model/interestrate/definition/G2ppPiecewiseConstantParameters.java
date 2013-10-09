/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * Parameters related to the G2++ model (equivalent to Hull-White two factors) with piecewise constant volatility.
 * Reference: Brigo D. anf Mercurio F. Interest Rate Models: Theory and practice. 2001 - Section 4.2.
 */
public class G2ppPiecewiseConstantParameters {

  /**
   * The mean reversion speed parameters (two parameters).
   */
  private final double[] _meanReversion;
  /**
   * The volatility parameters. The volatility is constant between the volatility times. Volatility in t is _volatility[i] for t between _volatilityTime[i] and _volatilityTime[i+1].
   * There are two volatility list, one for each factor.
   */
  private final DoubleArrayList[] _volatility = new DoubleArrayList[2];
  /**
   * The times separating the constant volatility periods. The time should be sorted by increasing order. The first time is 0 and the last time is 1000 (represents infinity).
   * The extra time are added in the constructor.
   */
  private final DoubleArrayList _volatilityTime;
  /**
   * The model correlation.
   */
  private final double _correlation;
  /**
   * The time used to represent infinity.
   */
  private static final double VOLATILITY_TIME_INFINITY = 1000.0;

  /**
   * Constructor from the model parameters.
   * @param meanReversion The mean reversion speed (2) parameters.
   * @param volatility The volatility parameters. There are two volatility list, one for each factor.
   * @param volatilityTime The times separating the constant volatility periods.
   * @param correlation The model correlation.
   */
  public G2ppPiecewiseConstantParameters(final double[] meanReversion, final double[][] volatility, final double[] volatilityTime, final double correlation) {
    ArgumentChecker.notNull(meanReversion, "mean reversion");
    ArgumentChecker.notNull(volatility, "volatility");
    ArgumentChecker.notNull(volatilityTime, "volatility time");
    ArgumentChecker.isTrue(meanReversion.length == 2, "Two mean reversions required");
    ArgumentChecker.isTrue(volatility.length == 2, "Two volatility arrays required");
    ArgumentChecker.isTrue(volatility[0].length == volatility[1].length, "Volatility length");
    ArgumentChecker.isTrue(volatility[0].length == volatilityTime.length + 1, "Number of times incorrect; had {}, need {}", volatilityTime.length + 1, volatility[0].length);
    _meanReversion = meanReversion;
    _volatility[0] = new DoubleArrayList(volatility[0]);
    _volatility[1] = new DoubleArrayList(volatility[1]);
    final double[] volatilityTimeArray = new double[volatilityTime.length + 2];
    volatilityTimeArray[0] = 0.0;
    volatilityTimeArray[volatilityTime.length + 1] = VOLATILITY_TIME_INFINITY;
    System.arraycopy(volatilityTime, 0, volatilityTimeArray, 1, volatilityTime.length);
    _volatilityTime = new DoubleArrayList(volatilityTimeArray);
    // TODO: check that the time are increasing.
    _correlation = correlation;
  }

  /**
   * Gets the mean reversion speed parameters.
   * @return The mean reversion speed parameters.
   */
  public double[] getMeanReversion() {
    return _meanReversion;
  }

  /**
   * Gets the volatility parameters.
   * @return The volatility parameters.
   */
  public DoubleArrayList[] getVolatility() {
    return _volatility;
  }

  /**
   * Sets the volatility parameters.
   * @param volatility The volatility parameters.
   */
  public void setVolatility(final double[][] volatility) {
    ArgumentChecker.isTrue(volatility.length == 2, "Two volatility arrays required");
    ArgumentChecker.isTrue(volatility[0].length == volatility[1].length, "Volatility length");
    ArgumentChecker.isTrue(volatility[0].length == _volatilityTime.size() - 1, "Volatility length");
    _volatility[0] = new DoubleArrayList(volatility[0]);
    _volatility[1] = new DoubleArrayList(volatility[1]);
  }

  /**
   * Gets the correlation.
   * @return The correlation
   */
  public double getCorrelation() {
    return _correlation;
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
  public double[] getLastVolatilities() {
    return new double[] {_volatility[0].get(_volatility[0].size() - 1), _volatility[1].get(_volatility[1].size() - 1)};
  }

  /**
   * Sets the last volatilities of the volatility lists.
   * @param volatility The replacing volatility.
   */
  public void setLastVolatilities(final double[] volatility) {
    ArgumentChecker.isTrue(volatility.length == 2, "Two volatilities required");
    _volatility[0].set(_volatility[0].size() - 1, volatility[0]);
    _volatility[1].set(_volatility[1].size() - 1, volatility[1]);
  }

  /**
   * Add an extra volatilities and volatility time at the end of the lists.
   * @param volatility The volatilities. Array of dimension 2.
   * @param volatilityTime The times separating the constant volatility periods. Must be larger than the previous one.
   */
  public void addVolatility(final double[] volatility, final double volatilityTime) {
    ArgumentChecker.isTrue(volatility.length == 2, "Two volatilities required");
    ArgumentChecker.isTrue(volatilityTime > _volatilityTime.get(_volatilityTime.size() - 2), "Volatility times should be increasing");
    _volatility[0].add(volatility[0]);
    _volatility[1].add(volatility[1]);
    _volatilityTime.set(_volatilityTime.size() - 1, volatilityTime);
    _volatilityTime.add(VOLATILITY_TIME_INFINITY);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_correlation);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_meanReversion);
    result = prime * result + Arrays.hashCode(_volatility);
    result = prime * result + _volatilityTime.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final G2ppPiecewiseConstantParameters other = (G2ppPiecewiseConstantParameters) obj;
    if (Double.doubleToLongBits(_correlation) != Double.doubleToLongBits(other._correlation)) {
      return false;
    }
    if (!Arrays.equals(_meanReversion, other._meanReversion)) {
      return false;
    }
    if (!Arrays.equals(_volatility, other._volatility)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatilityTime, other._volatilityTime)) {
      return false;
    }
    return true;
  }

}
