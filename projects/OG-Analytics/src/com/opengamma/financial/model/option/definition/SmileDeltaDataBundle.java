/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedStrikeFromDeltaFunction;

/**
 * Class describing the data required to describe a delta dependent smile from ATM, risk reversal and strangle as used in Forex market.
 * The delta used is the delta with respect to forward. 
 */
public class SmileDeltaDataBundle {

  /**
   * The time to expiry associated to the data.
   */
  private final double _timeToExpiry;
  /**
   * The ATM volatility.
   */
  private final double _atm;
  /**
   * Delta of the different data points. Must be positive and sorted in ascending order. The put will have as delta the opposite of the numbers.
   */
  private final double[] _delta;
  /**
   * The risk reversal volatility figures, in the same order as the delta.
   */
  private final double[] _riskReversal;
  /**
   * The strangle volatility figures, in the same order as the delta.
   */
  private final double[] _strangle;
  /**
   * Strikes in ascending order. Put with lower delta (in absolute value) first, ATM and call with larger delta first 
   */
  private final double[] _strike;
  /**
   * The volatilities associated to the strikes,
   */
  private final double[] _volatility;
  /**
   * The forward rate associated to the data.
   */
  private final double _forward;

  /**
   * @param timeToMaturity The time to maturity associated to the data.
   * @param forward The forward rate associated to the data.
   * @param atm The ATM volatility.
   * @param delta Delta of the different data points. Must be positive and sorted in ascending order. The put will have as delta the opposite of the numbers.
   * @param riskReversal The risk reversal volatility figures, in the same order as the delta.
   * @param strangle The strangle volatility figures, in the same order as the delta.
   */
  public SmileDeltaDataBundle(double timeToMaturity, double forward, double atm, double[] delta, double[] riskReversal, double[] strangle) {
    Validate.notNull(delta, "Delta");
    Validate.notNull(riskReversal, "Risk Reversal");
    Validate.notNull(strangle, "Strangle");
    Validate.isTrue(delta.length == riskReversal.length, "Length of delta should be equal to length of risk reversal");
    Validate.isTrue(delta.length == strangle.length, "Length of delta should be equal to length of strangle");
    //TODO: check that delta is sorted (ascending).
    this._timeToExpiry = timeToMaturity;
    _atm = atm;
    this._delta = delta;
    this._riskReversal = riskReversal;
    this._strangle = strangle;
    this._forward = forward;
    int nbDelta = delta.length;
    _strike = new double[2 * nbDelta + 1];
    _strike[nbDelta] = _forward * Math.exp(_atm * _atm * _timeToExpiry / 2.0);
    _volatility = new double[2 * nbDelta + 1];
    _volatility[nbDelta] = atm;
    for (int loopdelta = 0; loopdelta < nbDelta; loopdelta++) {
      _volatility[loopdelta] = strangle[loopdelta] + atm - riskReversal[loopdelta] / 2.0; // Put
      _strike[loopdelta] = BlackImpliedStrikeFromDeltaFunction.impliedStrike(-_delta[loopdelta], false, forward, timeToMaturity, _volatility[loopdelta]);
      _volatility[2 * nbDelta - loopdelta] = strangle[loopdelta] + atm + riskReversal[loopdelta] / 2.0; // Call
      _strike[2 * nbDelta - loopdelta] = BlackImpliedStrikeFromDeltaFunction.impliedStrike(_delta[loopdelta], true, forward, timeToMaturity, _volatility[2 * nbDelta - loopdelta]);
    }
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
   * Gets the strikes in ascending order. Put with lower delta (in absolute value) first, ATM and call with larger delta first 
   * @return The strikes.
   */
  public double[] getStrike() {
    return _strike;
  }

  /**
   * Gets the volatilities associated to the strikes,
   * @return The volatilities,
   */
  public double[] getVolatility() {
    return _volatility;
  }

  /**
   * Gets the forward rate associated to the data.
   * @return The forward rate associated to the data.
   */
  public double getForward() {
    return _forward;
  }

  /**
   * Gets the ATM volatility.
   * @return The ATM volatility.
   */
  public double getAtm() {
    return _atm;
  }

  /**
   * Gets the risk reversal volatility figures, in the same order as the delta.
   * @return The risk reversal volatility figures.
   */
  public double[] getRiskReversal() {
    return _riskReversal;
  }

  /**
   * Gets the strangle volatility figures, in the same order as the delta.
   * @return The strangle volatility figures.
   */
  public double[] getStrangle() {
    return _strangle;
  }

}
