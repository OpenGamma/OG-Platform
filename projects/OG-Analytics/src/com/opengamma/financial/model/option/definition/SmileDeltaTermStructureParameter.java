/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.volatility.VolatilityModel;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.LinearInterpolator1DNodeSensitivityCalculator;
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
   * Constructor from market data.
   * @param timeToExpiration The time to expiration of each volatility smile.
   * @param delta The delta at which the volatilities are given. Common to all time to expiration.
   * @param atm The ATM volatilities for each time to expiration. The length should be equal to the length of timeToExpiration.
   * @param riskReversal The risk reversal figures.
   * @param strangle The strangle figures.
   */
  public SmileDeltaTermStructureParameter(final double[] timeToExpiration, final double[] delta, double[] atm, double[][] riskReversal, double[][] strangle) {
    int nbExp = timeToExpiration.length;
    Validate.isTrue(atm.length == nbExp, "ATM length should be coherent with time to expiration length");
    Validate.isTrue(riskReversal.length == nbExp, "Risk reversal length should be coherent with time to expiration length");
    Validate.isTrue(strangle.length == nbExp, "Risk reversal length should be coherent with time to expiration length");
    Validate.isTrue(riskReversal[0].length == delta.length, "Risk reversal size should be coherent with time to delta length");
    Validate.isTrue(strangle[0].length == delta.length, "Risk reversal size should be coherent with time to delta length");
    _timeToExpiration = timeToExpiration;
    _volatilityTerm = new SmileDeltaParameter[nbExp];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      _volatilityTerm[loopexp] = new SmileDeltaParameter(timeToExpiration[loopexp], atm[loopexp], delta, riskReversal[loopexp], strangle[loopexp]);
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
  public double getVolatility(double time, double strike, double forward) {
    Validate.isTrue(time >= 0, "Positive time");
    int nbVol = _volatilityTerm[0].getVolatility().length;
    int nbTime = _timeToExpiration.length;
    ArrayInterpolator1DDataBundle interpData = new ArrayInterpolator1DDataBundle(_timeToExpiration, new double[nbTime]);
    int indexLower = interpData.getLowerBoundIndex(time);
    double[] variancePeriodT = new double[nbVol];
    double[] volatilityT = new double[nbVol];
    double[] variancePeriod0 = new double[nbVol];
    double[] variancePeriod1 = new double[nbVol];
    if (time < 1.0E-10) {
      volatilityT = _volatilityTerm[indexLower].getVolatility();
    } else {
      double weight0 = (_timeToExpiration[indexLower + 1] - time) / (_timeToExpiration[indexLower + 1] - _timeToExpiration[indexLower]);
      // Implementation note: Linear interpolation on variance over the period (s^2*t).
      for (int loopvol = 0; loopvol < nbVol; loopvol++) {
        variancePeriod0[loopvol] = _volatilityTerm[indexLower].getVolatility()[loopvol] * _volatilityTerm[indexLower].getVolatility()[loopvol] * _timeToExpiration[indexLower];
        variancePeriod1[loopvol] = _volatilityTerm[indexLower + 1].getVolatility()[loopvol] * _volatilityTerm[indexLower + 1].getVolatility()[loopvol] * _timeToExpiration[indexLower + 1];
        variancePeriodT[loopvol] = weight0 * variancePeriod0[loopvol] + (1 - weight0) * variancePeriod1[loopvol];
        volatilityT[loopvol] = Math.sqrt(variancePeriodT[loopvol] / time);
      }
    }
    SmileDeltaParameter smile = new SmileDeltaParameter(time, _volatilityTerm[0].getDelta(), volatilityT);
    double[] strikes = smile.getStrike(forward);
    double[] strikesExtra = new double[nbVol + 2]; // Extended strikes for flat extrapolation.
    strikesExtra[0] = 0;
    strikesExtra[nbVol + 1] = strikes[nbVol - 1] * 100.0; // TODO: better figure than 100*upper strike?
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
   * Computes the volatility and the volatility sensitivity with respect to the data points.
   * @param time The time to expiry.
   * @param strike The strike.
   * @param forward The forward.
   * @param bucketSensitivity The array is changed by the method. The array should have the correct size. After the methods, it contains the volatility sensitivity to the data points. 
   * Only the lines of impacted dates are changed. The input data on the other lines will not be changed.
   * @return The volatility.
   */
  public double getVolatilityAdjoint(final double time, final double strike, final double forward, double[][] bucketSensitivity) {
    int nbVol = _volatilityTerm[0].getVolatility().length;
    int nbTime = _timeToExpiration.length;
    ArrayInterpolator1DDataBundle interpData = new ArrayInterpolator1DDataBundle(_timeToExpiration, new double[nbTime]);
    int indexLower = interpData.getLowerBoundIndex(time);
    // Forward sweep
    double[] variancePeriodT = new double[nbVol];
    double[] volatilityT = new double[nbVol];
    double[] variancePeriod0 = new double[nbVol];
    double[] variancePeriod1 = new double[nbVol];
    double weight0 = (_timeToExpiration[indexLower + 1] - time) / (_timeToExpiration[indexLower + 1] - _timeToExpiration[indexLower]);
    // Implementation note: Linear interpolation on variance over the period (s^2*t).
    for (int loopvol = 0; loopvol < nbVol; loopvol++) {
      variancePeriod0[loopvol] = _volatilityTerm[indexLower].getVolatility()[loopvol] * _volatilityTerm[indexLower].getVolatility()[loopvol] * _timeToExpiration[indexLower];
      variancePeriod1[loopvol] = _volatilityTerm[indexLower + 1].getVolatility()[loopvol] * _volatilityTerm[indexLower + 1].getVolatility()[loopvol] * _timeToExpiration[indexLower + 1];
      variancePeriodT[loopvol] = weight0 * variancePeriod0[loopvol] + (1 - weight0) * variancePeriod1[loopvol];
      volatilityT[loopvol] = Math.sqrt(variancePeriodT[loopvol] / time);
    }
    SmileDeltaParameter smile = new SmileDeltaParameter(time, _volatilityTerm[0].getDelta(), volatilityT);
    double[] volatilityExtra = new double[nbVol + 2];
    volatilityExtra[0] = volatilityT[0];
    volatilityExtra[nbVol + 1] = volatilityT[nbVol - 1];
    System.arraycopy(volatilityT, 0, volatilityExtra, 1, nbVol);
    double[] strikes = smile.getStrike(forward);
    double[] strikesExtra = new double[nbVol + 2]; // Extended strikes for flat extrapolation.
    strikesExtra[0] = 0;
    strikesExtra[nbVol + 1] = strikes[nbVol - 1] * 100.0;
    System.arraycopy(strikes, 0, strikesExtra, 1, nbVol);
    ArrayInterpolator1DDataBundle volatilityInterpolation = new ArrayInterpolator1DDataBundle(strikesExtra, volatilityExtra);
    LinearInterpolator1D interpolator = new LinearInterpolator1D();
    double volatility = interpolator.interpolate(volatilityInterpolation, strike);
    // Backward sweep    
    double volBar = 1.0;
    LinearInterpolator1DNodeSensitivityCalculator interpolatorDerivative = new LinearInterpolator1DNodeSensitivityCalculator();
    //    double[] strikeExtraBar = ?;
    // FIXME: the strike sensitivity to volatility is missing. The sensitivity to x data in interpolation is required [PLAT-1396]
    double[] volatilityExtraBar = interpolatorDerivative.calculate(volatilityInterpolation, strike);
    double[] volatilityTBar = new double[nbVol];
    double[] variancePeriodTBar = new double[nbVol];
    double[] variancePeriod0Bar = new double[nbVol];
    double[] variancePeriod1Bar = new double[nbVol];
    System.arraycopy(volatilityExtraBar, 1, volatilityTBar, 0, nbVol);
    volatilityTBar[0] += volatilityExtraBar[0]; // Adding the sensitivity to the artificial initial point.
    volatilityTBar[nbVol - 1] += volatilityExtraBar[nbVol + 1]; // Adding the sensitivity to the artificial final point.
    for (int loopvol = 0; loopvol < nbVol; loopvol++) {
      volatilityTBar[loopvol] *= volBar;
      variancePeriodTBar[loopvol] = Math.pow(variancePeriodT[loopvol] / time, -0.5) / time / 2 * volatilityTBar[loopvol];
      variancePeriod0Bar[loopvol] = variancePeriodTBar[loopvol] * weight0;
      variancePeriod1Bar[loopvol] = variancePeriodTBar[loopvol] * (1 - weight0);
      bucketSensitivity[indexLower][loopvol] = 2.0 * _volatilityTerm[indexLower].getVolatility()[loopvol] * _timeToExpiration[indexLower] * variancePeriod0Bar[loopvol];
      bucketSensitivity[indexLower + 1][loopvol] = 2.0 * _volatilityTerm[indexLower + 1].getVolatility()[loopvol] * _timeToExpiration[indexLower + 1] * variancePeriod1Bar[loopvol];
    }
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
   * Gets the number of expirations.
   * @return The number of expirations.
   */
  public int getNumberExpiration() {
    return _timeToExpiration.length;
  }

  /**
   * Gets the volatility smiles from delta.
   * @return The volatility smiles.
   */
  public SmileDeltaParameter[] getVolatilityTerm() {
    return _volatilityTerm;
  }

  /**
   * Gets the number of strikes (common to all dates).
   * @return The number of strikes.
   */
  public int getNumberStrike() {
    return _volatilityTerm[0].getVolatility().length;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_timeToExpiration);
    result = prime * result + Arrays.hashCode(_volatilityTerm);
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
    SmileDeltaTermStructureParameter other = (SmileDeltaTermStructureParameter) obj;
    if (!Arrays.equals(_timeToExpiration, other._timeToExpiration)) {
      return false;
    }
    if (!Arrays.equals(_volatilityTerm, other._volatilityTerm)) {
      return false;
    }
    return true;
  }

}
