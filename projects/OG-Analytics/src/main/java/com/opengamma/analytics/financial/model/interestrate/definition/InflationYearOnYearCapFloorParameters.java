/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.definition;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.util.ArgumentChecker;

/**
 *  Data bundle related to the year on year inflation cap/floor in price index market model.
 */
public class InflationYearOnYearCapFloorParameters {

  /**
   *  The expiry times of the caplet/floorlet(for inflation this is the fixing time of the underlying CPI, the one of the numerator).
   */
  private final double[] _expiryTimes;
  /**
   * The strikes.
   */
  private final double[] _strikes;

  /**
   * The volatilities. The dimensions of the volatility is respectively expiration and strikes.
   */
  private final double[][] _volatility;

  /**
   * The index price for which the volatility is valid. Not null.
   */
  private final IndexPrice _index;

  /**
   * The time tolerance between the dates given by the model and the dates of the instrument. To avoid rounding problems.
   */
  private static final double TIME_TOLERANCE = 1.0E-3;

  /**
   * Sets the expiry time to zero and initialises other parameters with empty arrays.
   */
  public InflationYearOnYearCapFloorParameters() {
    _expiryTimes = new double[1];
    _strikes = new double[0];
    _volatility = new double[0][0];
    _index = new IndexPrice("index", null);
  }

  /**
   * Constructor from the model details.
   * @param expiryTimes The expiry times of the caplet/floorlet(for inflation this is the fixing time of the underlying CPI, the one of the numerator).
   * @param strikes The strikes of the caplet/floorlet.
   * @param volatility The volatility of the year on year caplet/floorlet.
   * @param index The price index.
   */
  public InflationYearOnYearCapFloorParameters(final double[] expiryTimes, final double[] strikes, final double[][] volatility, final IndexPrice index) {
    ArgumentChecker.notNull(expiryTimes, "Inflation year on year options expiry times");
    ArgumentChecker.notNull(strikes, "Inflation year on year options strikes");
    ArgumentChecker.notNull(volatility, "Inflation year on year options volatilities");
    ArgumentChecker.isTrue(expiryTimes.length == volatility.length, "number of expiry should be the same in the volatility matrix and in the expiry vector");
    ArgumentChecker.isTrue(strikes.length == volatility[0].length, "number of strikes should be the same in the volatility matrix and in the strikes vector");
    _expiryTimes = expiryTimes;
    _strikes = strikes;
    _volatility = volatility;
    _index = index;
  }

  /**
   * Create a new copy of the object with the same data. All the arrays are cloned.
   * @return The Inflation year on year option parameters.
   */
  public InflationYearOnYearCapFloorParameters copy() {
    final double[][] vol = new double[_volatility.length][];
    for (int loopperiod = 0; loopperiod < _volatility.length; loopperiod++) {
      vol[loopperiod] = _volatility[loopperiod].clone();
    }
    return new InflationYearOnYearCapFloorParameters(_expiryTimes.clone(), _strikes.clone(), vol, _index);
  }

  /**
   * Gets the expiry tumes vector.
   * @return the _expiryTime
   */
  public double[] getExpiryTimes() {
    return _expiryTimes;
  }

  /**
   * Gets the strikes vector.
   * @return the _strikes
   */
  public double[] getStrikes() {
    return _strikes;
  }

  /**
   * Gets the index.
   * @return the _index
   */
  public IndexPrice getIndex() {
    return _index;
  }

  /**
   * Gets the Number Of Expiry Times.
   * @return the _expiryTime.length
   */
  public int getNumberOfExpiryTimes() {
    return _expiryTimes.length;
  }

  /**
   * Gets the Number Of Strikes.
   * @return the _strikes.length
   */
  public int getNumberOfStrikes() {
    return _strikes.length;
  }

  /**
  * Gets the _volatility field.
  * @return the _volatility
  */
  public double[][] getVolatility() {
    return _volatility;
  }

  /**
   * Gets the time tolerance.
   * @return The time tolerance
   */
  public double getTimeTolerance() {
    return TIME_TOLERANCE;
  }

  /**
   * Change the model volatility in a block to a given volatility matrix.
   * @param volatility The changed volatility.
   * @param expiryIndex The start index for the block to change.
   */
  public final void setVolatility(final double[][] volatility, final int expiryIndex) {
    ArgumentChecker.notNull(volatility, "LMM volatility");
    ArgumentChecker.isTrue(volatility[0].length == _strikes.length, "LMM: incorrect number of factors");
    for (int loopperiod = 0; loopperiod < volatility.length; loopperiod++) {
      System.arraycopy(volatility[loopperiod], 0, _volatility[expiryIndex + loopperiod], 0, volatility[loopperiod].length);
    }
  }

  /**
   * Change the model volatility in a block to a given volatility matrix.
   * @param volatility The changed volatility.
   * @param expiryIndex The index for the value to change.
   * @param strikeIndex The index for the value to change.
   */
  public final void setVolatility(final double volatility, final int expiryIndex, final int strikeIndex) {
    Arrays.fill(_volatility[expiryIndex], strikeIndex, strikeIndex + 1, volatility);
  }

  /**
   * Change the model displacement in a block to a given displacement vector.
   * @param expiryTimes The change displacement.
   * @param startIndex The start index for the block to change.
   */
  public final void setExpiryTimes(final double[] expiryTimes, final int startIndex) {
    ArgumentChecker.notNull(expiryTimes, "Inflation year on year options expiry times");
    System.arraycopy(expiryTimes, 0, _expiryTimes, startIndex, expiryTimes.length);
  }

  /**
   * Change the model displacement in a block to a given displacement vector.
   * @param strikes The change displacement.
   * @param startIndex The start index for the block to change.
   */
  public final void setStrikes(final double[] strikes, final int startIndex) {
    ArgumentChecker.notNull(_strikes, "Inflation year on year options strikes");
    System.arraycopy(strikes, 0, _strikes, startIndex, strikes.length);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_expiryTimes);
    result = prime * result + _index.hashCode();
    result = prime * result + Arrays.hashCode(_strikes);
    result = prime * result + Arrays.hashCode(_volatility);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InflationYearOnYearCapFloorParameters)) {
      return false;
    }
    final InflationYearOnYearCapFloorParameters other = (InflationYearOnYearCapFloorParameters) obj;
    if (!Arrays.equals(_expiryTimes, other._expiryTimes)) {
      return false;
    }
    if (!Arrays.equals(_strikes, other._strikes)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!Arrays.deepEquals(_volatility, other._volatility)) {
      return false;
    }
    return true;
  }

}
