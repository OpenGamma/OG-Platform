/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedStrikeFromDeltaFunction;

/**
 * Class describing the data required to describe a delta dependent smile from ATM, risk reversal and strangle as used in Forex market.
 * The delta used is the delta with respect to forward. 
 */
public class SmileDeltaParameter {

  /**
   * The time to expiry associated to the data.
   */
  private final double _timeToExpiry;
  /**
   * Delta of the different data points. Must be positive and sorted in ascending order. The put will have as delta the opposite of the numbers.
   */
  private final double[] _delta;
  /**
   * The volatilities associated to the strikes,
   */
  private final double[] _volatility;

  /**
   * Constructor from volatility
   * @param timeToExpiration The time to expiration associated to the data.
   * @param delta Delta of the different data points. Must be positive and sorted in ascending order. The put will have as delta the opposite of the numbers.
   * @param volatility The volatilities.
   */
  public SmileDeltaParameter(final double timeToExpiration, final double[] delta, final double[] volatility) {
    Validate.notNull(delta, "Delta");
    Validate.notNull(volatility, "Volatility");
    Validate.isTrue(2 * delta.length + 1 == volatility.length, "Length of delta should be coherent with volatility length");
    _timeToExpiry = timeToExpiration;
    _delta = delta;
    _volatility = volatility;
  }

  /**
   * Constructor from market data ATM, RR, Strangle.
   * @param timeToExpiry The time to expiration associated to the data.
   * @param atm The ATM volatility.
   * @param delta Delta of the different data points. Must be positive and sorted in ascending order. The put will have as delta the opposite of the numbers.
   * @param riskReversal The risk reversal volatility figures, in the same order as the delta.
   * @param strangle The strangle volatility figures, in the same order as the delta.
   */
  public SmileDeltaParameter(final double timeToExpiry, final double atm, final double[] delta, final double[] riskReversal, final double[] strangle) {
    Validate.notNull(delta, "Delta");
    Validate.notNull(riskReversal, "Risk Reversal");
    Validate.notNull(strangle, "Strangle");
    Validate.isTrue(delta.length == riskReversal.length, "Length of delta should be equal to length of risk reversal");
    Validate.isTrue(delta.length == strangle.length, "Length of delta should be equal to length of strangle");
    //TODO: check that delta is sorted (ascending).
    //REVIEW 6-7-2011 emcleod better to do a parallel sort of delta, risk reversal and strangle and have another constructor
    // with an isSorted parameter that will not attempt the sort
    this._timeToExpiry = timeToExpiry;
    this._delta = delta;
    final int nbDelta = delta.length;
    _volatility = new double[2 * nbDelta + 1];
    _volatility[nbDelta] = atm;
    for (int loopdelta = 0; loopdelta < nbDelta; loopdelta++) {
      _volatility[loopdelta] = strangle[loopdelta] + atm - riskReversal[loopdelta] / 2.0; // Put
      _volatility[2 * nbDelta - loopdelta] = strangle[loopdelta] + atm + riskReversal[loopdelta] / 2.0; // Call
    }
  }

  /**
   * Computes the strikes in ascending order. Put with lower delta (in absolute value) first, ATM and call with larger delta first 
   * @param forward The forward.
   * @return The strikes.
   */
  public double[] getStrike(final double forward) {
    final int nbDelta = _delta.length;
    final double[] strike = new double[2 * nbDelta + 1];
    strike[nbDelta] = forward * Math.exp(_volatility[nbDelta] * _volatility[nbDelta] * _timeToExpiry / 2.0);
    for (int loopdelta = 0; loopdelta < nbDelta; loopdelta++) {
      strike[loopdelta] = BlackImpliedStrikeFromDeltaFunction.impliedStrike(-_delta[loopdelta], false, forward, _timeToExpiry, _volatility[loopdelta]); // Put
      strike[2 * nbDelta - loopdelta] = BlackImpliedStrikeFromDeltaFunction.impliedStrike(_delta[loopdelta], true, forward, _timeToExpiry, _volatility[2 * nbDelta - loopdelta]); // Call
    }
    return strike;
  }

  /**
   * Gets the time to expiry associated to the data.
   * @return The time to expiry.
   */
  public double getTimeToExpiry() {
    return _timeToExpiry;
  }

  /**
   * Gets the delta of the different data points. Must be positive and sorted in ascending order. The put will have as delta the opposite of the numbers.
   * @return The delta.
   */
  public double[] getDelta() {
    return _delta;
  }

  /**
   * Gets the volatilities associated to the strikes,
   * @return The volatilities,
   */
  public double[] getVolatility() {
    return _volatility;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_delta);
    long temp;
    temp = Double.doubleToLongBits(_timeToExpiry);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_volatility);
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
    final SmileDeltaParameter other = (SmileDeltaParameter) obj;
    if (!Arrays.equals(_delta, other._delta)) {
      return false;
    }
    if (Double.doubleToLongBits(_timeToExpiry) != Double.doubleToLongBits(other._timeToExpiry)) {
      return false;
    }
    if (!Arrays.equals(_volatility, other._volatility)) {
      return false;
    }
    return true;
  }

}
