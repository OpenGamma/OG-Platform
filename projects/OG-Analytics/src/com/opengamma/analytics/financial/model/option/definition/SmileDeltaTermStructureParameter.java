/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

/**
 * Class describing the data required to describe a delta and expiration dependent smile from ATM, risk reversal and strangle as used in Forex market.
 * The delta used is the delta with respect to forward.
 */
public class SmileDeltaTermStructureParameter implements VolatilityModel<Triple<Double, Double, Double>> {

  /**
   * The interpolator/extrapolator used in the strike dimension.
   */
  private final Interpolator1D _interpolator;
  /**
   * The time to expiration in the term structure.
   */
  private final double[] _timeToExpiration;
  /**
   * The smile description at the different time to expiration. All item should have the same deltas.
   */
  private final SmileDeltaParameter[] _volatilityTerm;

  /**
   * The default interpolator: linear with flat extrapolation.
   */
  private static final Interpolator1D DEFAULT_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  /**
   * Constructor from volatility term structure. The default interpolator is used to interpolate in the strike dimension. The default interpolator is linear with flat extrapolation.
   * @param volatilityTerm The volatility description at the different expiration.
   */
  public SmileDeltaTermStructureParameter(final SmileDeltaParameter[] volatilityTerm) {
    this(volatilityTerm, DEFAULT_INTERPOLATOR);
  }

  /**
   * Constructor from volatility term structure.
   * @param volatilityTerm The volatility description at the different expiration.
   * @param interpolator The interpolator used in the strike dimension.
   */
  public SmileDeltaTermStructureParameter(final SmileDeltaParameter[] volatilityTerm, final Interpolator1D interpolator) {
    ArgumentChecker.notNull(volatilityTerm, "Volatility term structure");
    _volatilityTerm = volatilityTerm;
    final int nbExp = volatilityTerm.length;
    _timeToExpiration = new double[nbExp];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      _timeToExpiration[loopexp] = _volatilityTerm[loopexp].getTimeToExpiry();
    }
    _interpolator = interpolator;
  }

  /**
   * Constructor from market data. The default interpolator is used to interpolate in the strike dimension. The default interpolator is linear with flat extrapolation.
   * @param timeToExpiration The time to expiration of each volatility smile.
   * @param delta The delta at which the volatilities are given. Must be positive and sorted in ascending order. The put will have as delta the opposite of the numbers.
   * Common to all time to expiration.
   * @param volatility The volatilities at each delta.
   */
  public SmileDeltaTermStructureParameter(final double[] timeToExpiration, final double[] delta, final double[][] volatility) {
    this(timeToExpiration, delta, volatility, DEFAULT_INTERPOLATOR);
  }

  /**
   * Constructor from market data.
   * @param timeToExpiration The time to expiration of each volatility smile.
   * @param delta The delta at which the volatilities are given. Must be positive and sorted in ascending order. The put will have as delta the opposite of the numbers.
   * Common to all time to expiration.
   * @param volatility The volatilities at each delta.
   * @param interpolator The interpolator used in the strike dimension.
   */
  public SmileDeltaTermStructureParameter(final double[] timeToExpiration, final double[] delta, final double[][] volatility, final Interpolator1D interpolator) {
    final int nbExp = timeToExpiration.length;
    ArgumentChecker.isTrue(volatility.length == nbExp, "Volatility length should be coherent with time to expiration length");
    ArgumentChecker.isTrue(volatility[0].length == 2 * delta.length + 1, "Risk volatility size should be coherent with time to delta length");
    _timeToExpiration = timeToExpiration;
    _volatilityTerm = new SmileDeltaParameter[nbExp];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      _volatilityTerm[loopexp] = new SmileDeltaParameter(timeToExpiration[loopexp], delta, volatility[loopexp]);
    }
    _interpolator = interpolator;
  }

  /**
   * Constructor from market data. The default interpolator is used to interpolate in the strike dimension. The default interpolator is linear with flat extrapolation.
   * @param timeToExpiration The time to expiration of each volatility smile.
   * @param delta The delta at which the volatilities are given. Common to all time to expiration.
   * @param atm The ATM volatilities for each time to expiration. The length should be equal to the length of timeToExpiration.
   * @param riskReversal The risk reversal figures.
   * @param strangle The strangle figures.
   */
  public SmileDeltaTermStructureParameter(final double[] timeToExpiration, final double[] delta, final double[] atm, final double[][] riskReversal, final double[][] strangle) {
    this(timeToExpiration, delta, atm, riskReversal, strangle, DEFAULT_INTERPOLATOR);
  }

  /**
   * Constructor from market data.
   * @param timeToExpiration The time to expiration of each volatility smile.
   * @param delta The delta at which the volatilities are given. Common to all time to expiration.
   * @param atm The ATM volatilities for each time to expiration. The length should be equal to the length of timeToExpiration.
   * @param riskReversal The risk reversal figures.
   * @param strangle The strangle figures.
   * @param interpolator The interpolator used in the strike dimension.
   */
  public SmileDeltaTermStructureParameter(final double[] timeToExpiration, final double[] delta, final double[] atm, final double[][] riskReversal, final double[][] strangle,
      final Interpolator1D interpolator) {
    final int nbExp = timeToExpiration.length;
    ArgumentChecker.isTrue(atm.length == nbExp, "ATM length should be coherent with time to expiration length");
    ArgumentChecker.isTrue(riskReversal.length == nbExp, "Risk reversal length should be coherent with time to expiration length");
    ArgumentChecker.isTrue(strangle.length == nbExp, "Risk reversal length should be coherent with time to expiration length");
    ArgumentChecker.isTrue(riskReversal[0].length == delta.length, "Risk reversal size should be coherent with time to delta length");
    ArgumentChecker.isTrue(strangle[0].length == delta.length, "Risk reversal size should be coherent with time to delta length");
    _timeToExpiration = timeToExpiration;
    _volatilityTerm = new SmileDeltaParameter[nbExp];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      _volatilityTerm[loopexp] = new SmileDeltaParameter(timeToExpiration[loopexp], atm[loopexp], delta, riskReversal[loopexp], strangle[loopexp]);
    }
    _interpolator = interpolator;
  }

  /**
   * Get the volatility at a given time/strike/forward from the term structure. The volatility at a given delta are interpolated linearly on the total variance (s^2*t) and extrapolated flat.
   * The volatility are then linearly interpolated in the strike dimension and extrapolated flat.
   * @param time The time to expiry.
   * @param strike The strike.
   * @param forward The forward.
   * @return The volatility.
   */
  public double getVolatility(final double time, final double strike, final double forward) {
    ArgumentChecker.isTrue(time >= 0, "Positive time");
    final int nbVol = _volatilityTerm[0].getVolatility().length;
    ArgumentChecker.isTrue(nbVol > 1, "Need more than one volatility value to perform interpolation");
    final int nbTime = _timeToExpiration.length;
    ArgumentChecker.isTrue(nbTime > 1, "Need more than one time value to perform interpolation");
    double[] volatilityT = new double[nbVol];
    if (time <= _timeToExpiration[0]) {
      volatilityT = _volatilityTerm[0].getVolatility();
    } else {
      if (time >= _timeToExpiration[nbTime - 1]) {
        volatilityT = _volatilityTerm[nbTime - 1].getVolatility();
      } else {
        final ArrayInterpolator1DDataBundle interpData = new ArrayInterpolator1DDataBundle(_timeToExpiration, new double[nbTime]);
        final int indexLower = interpData.getLowerBoundIndex(time);
        final double[] variancePeriodT = new double[nbVol];
        final double[] variancePeriod0 = new double[nbVol];
        final double[] variancePeriod1 = new double[nbVol];
        final double weight0 = (_timeToExpiration[indexLower + 1] - time) / (_timeToExpiration[indexLower + 1] - _timeToExpiration[indexLower]);
        // Implementation note: Linear interpolation on variance over the period (s^2*t).
        for (int loopvol = 0; loopvol < nbVol; loopvol++) {
          variancePeriod0[loopvol] = _volatilityTerm[indexLower].getVolatility()[loopvol] * _volatilityTerm[indexLower].getVolatility()[loopvol] * _timeToExpiration[indexLower];
          variancePeriod1[loopvol] = _volatilityTerm[indexLower + 1].getVolatility()[loopvol] * _volatilityTerm[indexLower + 1].getVolatility()[loopvol] * _timeToExpiration[indexLower + 1];
          variancePeriodT[loopvol] = weight0 * variancePeriod0[loopvol] + (1 - weight0) * variancePeriod1[loopvol];
          volatilityT[loopvol] = Math.sqrt(variancePeriodT[loopvol] / time);
        }
      }
    }
    final SmileDeltaParameter smile = new SmileDeltaParameter(time, _volatilityTerm[0].getDelta(), volatilityT);
    final double[] strikes = smile.getStrike(forward);
    final Interpolator1DDataBundle volatilityInterpolation = _interpolator.getDataBundle(strikes, volatilityT);
    final double volatility = _interpolator.interpolate(volatilityInterpolation, strike);
    return volatility;
  }

  /**
   * Computes the volatility and the volatility sensitivity with respect to the volatility data points.
   * @param time The time to expiration.
   * @param strike The strike.
   * @param forward The forward.
   * @param bucketSensitivity The array is changed by the method. The array should have the correct size. After the methods, it contains the volatility sensitivity to the data points.
   * Only the lines of impacted dates are changed. The input data on the other lines will not be changed.
   * @return The volatility.
   */
  public double getVolatility(final double time, final double strike, final double forward, final double[][] bucketSensitivity) {
    final int nbVol = _volatilityTerm[0].getVolatility().length;
    ArgumentChecker.isTrue(nbVol > 1, "Need more than one volatility value to perform interpolation");
    final int nbTime = _timeToExpiration.length;
    ArgumentChecker.isTrue(nbTime > 1, "Need more than one time value to perform interpolation");

    final int indexLower;
    final double weightLow;

    // Long Expiry Extrapolation : Flat
    if (time >= _timeToExpiration[nbTime - 1]) {
      indexLower = nbTime - 2;
      weightLow = 0.0;
      // Short Expiry Extrapolation : Flat
    } else if (time < _timeToExpiration[0]) {
      indexLower = 0;;
      weightLow = 1.0;
    } else {
      final ArrayInterpolator1DDataBundle interpData = new ArrayInterpolator1DDataBundle(_timeToExpiration, new double[nbTime]);
      //TODO Do NOT use the interpolator to find the lower bound index
      indexLower = interpData.getLowerBoundIndex(time);
      weightLow = (_timeToExpiration[indexLower + 1] - time) / (_timeToExpiration[indexLower + 1] - _timeToExpiration[indexLower]);
    }
    final double weightHigh = 1.0 - weightLow;

    // Forward sweep
    final double[] variancePeriodT = new double[nbVol];
    final double[] volatilityT = new double[nbVol];
    final double[] variancePeriod0 = new double[nbVol];
    final double[] variancePeriod1 = new double[nbVol];

    // Implementation note: Linear interpolation on variance over the period (s^2*t).
    for (int loopvol = 0; loopvol < nbVol; loopvol++) {
      variancePeriod0[loopvol] = _volatilityTerm[indexLower].getVolatility()[loopvol] * _volatilityTerm[indexLower].getVolatility()[loopvol] * _timeToExpiration[indexLower];
      variancePeriod1[loopvol] = _volatilityTerm[indexLower + 1].getVolatility()[loopvol] * _volatilityTerm[indexLower + 1].getVolatility()[loopvol] * _timeToExpiration[indexLower + 1];
      variancePeriodT[loopvol] = weightLow * variancePeriod0[loopvol] + weightHigh * variancePeriod1[loopvol];
      volatilityT[loopvol] = Math.sqrt(variancePeriodT[loopvol] / time);
    }
    final SmileDeltaParameter smile = new SmileDeltaParameter(time, _volatilityTerm[0].getDelta(), volatilityT);
    final double[] strikes = smile.getStrike(forward);
    final Interpolator1DDataBundle volatilityInterpolation = _interpolator.getDataBundle(strikes, volatilityT);
    final double volatility = _interpolator.interpolate(volatilityInterpolation, strike);
    // Backward sweep
    final double volBar = 1.0;
    // FIXME: the strike sensitivity to volatility is missing. The sensitivity to x data in interpolation is required [PLAT-1396]
    final double[] volatilityTBar = _interpolator.getNodeSensitivitiesForValue(volatilityInterpolation, strike);
    final double[] variancePeriodTBar = new double[nbVol];
    final double[] variancePeriod0Bar = new double[nbVol];
    final double[] variancePeriod1Bar = new double[nbVol];
    for (int loopvol = 0; loopvol < nbVol; loopvol++) {
      volatilityTBar[loopvol] *= volBar;
      variancePeriodTBar[loopvol] = Math.pow(variancePeriodT[loopvol] / time, -0.5) / time / 2 * volatilityTBar[loopvol];
      variancePeriod0Bar[loopvol] = variancePeriodTBar[loopvol] * weightLow;
      variancePeriod1Bar[loopvol] = variancePeriodTBar[loopvol] * weightHigh;
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

  /**
   * Gets delta (common to all time to expiration).
   * @return The delta.
   */
  public double[] getDelta() {
    return _volatilityTerm[0].getDelta();
  }

  /**
   * Gets the interpolator
   * @return The interpolator
   */
  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  /**
   * Gets put delta absolute value for all strikes. The ATM is 0.50 delta and the x call are transformed in 1-x put.
   * @return The delta.
   */
  public double[] getDeltaFull() {
    final int nbDelta = _volatilityTerm[0].getDelta().length;
    final double[] result = new double[2 * nbDelta + 1];
    for (int loopdelta = 0; loopdelta < nbDelta; loopdelta++) {
      result[loopdelta] = _volatilityTerm[0].getDelta()[loopdelta];
      result[nbDelta + 1 + loopdelta] = 1.0 - _volatilityTerm[0].getDelta()[nbDelta - 1 - loopdelta];
    }
    result[nbDelta] = 0.50;
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_timeToExpiration);
    result = prime * result + Arrays.hashCode(_volatilityTerm);
    result = prime * result + _interpolator.hashCode();
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
    final SmileDeltaTermStructureParameter other = (SmileDeltaTermStructureParameter) obj;
    if (!Arrays.equals(_timeToExpiration, other._timeToExpiration)) {
      return false;
    }
    if (!Arrays.equals(_volatilityTerm, other._volatilityTerm)) {
      return false;
    }
    if (!ObjectUtils.equals(_interpolator, other._interpolator)) {
      return false;
    }
    return true;
  }

}
