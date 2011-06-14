/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.volatility.VolatilityModel;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.util.tuple.Triple;

/**
 * Class describing the data required to describe a delta and expiration dependent smile from ATM, risk reversal and strangle as used in Forex market.
 * The delta used is the delta with respect to forward. 
 */
public class SmileDeltaTermStructureParameter implements VolatilityModel<Triple<Double, Double, Double>> {

  /**
   * The time to expiration in the term structure.
   */
  private final double[] _timeToExpiration;
  /**
   * The smile description at the different time to expiration. All item should have the same deltas.
   */
  private final SmileDeltaParameter[] _volatilityTerm;

  /**
   * Constructor from volatility term structure.
   * @param volatilityTerm The volatility description at the different expiration.
   */
  public SmileDeltaTermStructureParameter(final SmileDeltaParameter[] volatilityTerm) {
    Validate.notNull(volatilityTerm, "Volatility term structure");
    _volatilityTerm = volatilityTerm;
    int nbExp = volatilityTerm.length;
    _timeToExpiration = new double[nbExp];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      _timeToExpiration[loopexp] = _volatilityTerm[loopexp].getTimeToExpiry();
    }
  }

  /**
   * Get the volatility at a given time/strike/forward from the term structure. The volatility at a given delta are interpolated linearly on the total variance (s^2*t).
   * The volatility are then linearly interpolated in the strike dimension and extrapolated flat.
   * @param time The time to expiry.
   * @param strike The strike.
   * @param forward The forward.
   * @return The volatility.
   */
  double getVolatility(double time, double strike, double forward) {
    int nbVol = _volatilityTerm[0].getVolatility().length;
    int nbTime = _timeToExpiration.length;
    ArrayInterpolator1DDataBundle interpData = new ArrayInterpolator1DDataBundle(_timeToExpiration, new double[nbTime]);
    int indexLower = interpData.getLowerBoundIndex(time);
    double[] variancePeriodT = new double[nbVol];
    double[] volatilityT = new double[nbVol];
    double[] variancePeriod0 = new double[nbVol];
    double[] variancePeriod1 = new double[nbVol];
    for (int loopvol = 0; loopvol < nbVol; loopvol++) { // Linear interpolation on variance over the period (s^2*t).
      variancePeriod0[loopvol] = _volatilityTerm[indexLower].getVolatility()[loopvol] * _volatilityTerm[indexLower].getVolatility()[loopvol] * _timeToExpiration[indexLower];
      variancePeriod1[loopvol] = _volatilityTerm[indexLower + 1].getVolatility()[loopvol] * _volatilityTerm[indexLower + 1].getVolatility()[loopvol] * _timeToExpiration[indexLower + 1];
      variancePeriodT[loopvol] = variancePeriod0[loopvol] + (variancePeriod1[loopvol] - variancePeriod0[loopvol]) * (time - _timeToExpiration[indexLower])
          / (_timeToExpiration[indexLower + 1] - _timeToExpiration[indexLower]);
      volatilityT[loopvol] = Math.sqrt(variancePeriodT[loopvol] / time);
    }
    SmileDeltaParameter smile = new SmileDeltaParameter(time, _volatilityTerm[0].getDelta(), volatilityT);
    double[] strikes = smile.getStrike(forward);
    double[] strikesExtra = new double[nbVol + 2]; // Extended strikes for flat extrapolation.
    strikesExtra[0] = 0;
    strikesExtra[nbVol + 1] = strikes[nbVol - 1] * 10.0;
    System.arraycopy(strikes, 0, strikesExtra, 1, nbVol);
    double[] volatilityExtra = new double[nbVol + 2];
    volatilityExtra[0] = volatilityT[0];
    volatilityExtra[nbVol + 1] = volatilityT[nbVol - 1];
    System.arraycopy(volatilityT, 0, volatilityExtra, 1, nbVol);
    ArrayInterpolator1DDataBundle volatilityInterpolation = new ArrayInterpolator1DDataBundle(strikesExtra, volatilityExtra);
    LinearInterpolator1D interpolator = new LinearInterpolator1D();
    double volatility = interpolator.interpolate(volatilityInterpolation, strike);
    return volatility;
  }

  /**
   * Get the volatility from a triple.
   * @param tsf The Time, Strike, Forward triple.
   * @return The volatility.
   */
  @Override
  public Double getVolatility(final Triple<Double, Double, Double> tsf) {
    return getVolatility(tsf.getFirst(), tsf.getSecond(), tsf.getThird());
  }

  /**
   * Gets the times to expiration.
   * @return The times.
   */
  public double[] getTimeToExpiration() {
    return _timeToExpiration;
  }

  /**
   * Gets the volatility smiles from delta.
   * @return The volatility smiles.
   */
  public SmileDeltaParameter[] getVolatilityTerm() {
    return _volatilityTerm;
  }

}
