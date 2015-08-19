/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.volatility.SmileAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivitiesModel;
import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Class describing the a term structure of smiles from ATM, risk reversal and strangle as used in Forex market.
 * The delta used is the delta with respect to forward.
 */
public class SmileDeltaTermStructureParameters implements VolatilityAndBucketedSensitivitiesModel<Triple<Double, Double, Double>> {

  /**
   * The time to expiration in the term structure.
   */
  private final double[] _timeToExpiration;
  /**
   * The smile description at the different time to expiration. All item should have the same deltas.
   */
  private final SmileDeltaParameters[] _volatilityTerm;
  /**
   * The interpolator/extrapolator used in the expiry dimension.
   */
  private final Interpolator1D _timeInterpolator;

  /**
   * The default interpolator: time square (total variance) with flat extrapolation.
   */
  private static final Interpolator1D DEFAULT_INTERPOLATOR_EXPIRY = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.TIME_SQUARE, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  /**
   * Constructor from volatility term structure.
   * @param volatilityTerm The volatility description at the different expiration.
   */
  public SmileDeltaTermStructureParameters(final SmileDeltaParameters[] volatilityTerm) {
    this(volatilityTerm, DEFAULT_INTERPOLATOR_EXPIRY);
  }

  /**
   * Constructor from volatility term structure.
   * @param volatilityTerm The volatility description at the different expiration.
   * @param interpolator The time interpolator
   */
  public SmileDeltaTermStructureParameters(final SmileDeltaParameters[] volatilityTerm, final Interpolator1D interpolator) {
    ArgumentChecker.notNull(volatilityTerm, "Volatility term structure");
    ArgumentChecker.notNull(interpolator, "interpolator");
    _volatilityTerm = volatilityTerm;
    final int nbExp = volatilityTerm.length;
    _timeToExpiration = new double[nbExp];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      _timeToExpiration[loopexp] = _volatilityTerm[loopexp].getTimeToExpiry();
    }
    _timeInterpolator = interpolator;
  }

  /**
   * Constructor from market data.
   * @param timeToExpiration The time to expiration of each volatility smile, not null
   * @param delta The delta at which the volatilities are given. Must be positive and sorted in ascending order. The put will have as delta the opposite of the numbers.
   * Common to all time to expiration. Not null
   * @param volatility The volatilities at each delta, not null
   */
  public SmileDeltaTermStructureParameters(final double[] timeToExpiration, final double[] delta, final double[][] volatility) {
    ArgumentChecker.notNull(timeToExpiration, "time to expiry");
    ArgumentChecker.notNull(delta, "delta");
    ArgumentChecker.notNull(volatility, "volatility");
    final int nbExp = timeToExpiration.length;
    ArgumentChecker.isTrue(volatility.length == nbExp, "Volatility array length {} should be equal to the number of expiries {}", volatility.length, nbExp);
    ArgumentChecker.isTrue(volatility[0].length == 2 * delta.length + 1, "Volatility array {} should be equal to (2 * number of deltas) + 1, have {}", volatility[0].length, 2 * delta.length + 1);
    _timeToExpiration = timeToExpiration;
    _volatilityTerm = new SmileDeltaParameters[nbExp];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      _volatilityTerm[loopexp] = new SmileDeltaParameters(timeToExpiration[loopexp], delta, volatility[loopexp]);
    }
    _timeInterpolator = DEFAULT_INTERPOLATOR_EXPIRY;
    ArgumentChecker.isTrue(_volatilityTerm[0].getVolatility().length > 1, "Need more than one volatility value to perform interpolation");
  }

  /**
   * Constructor from market data. The default interpolator is used for the time dimension.
   * @param timeToExpiration The time to expiration of each volatility smile.
   * @param delta The delta at which the volatilities are given. Common to all time to expiration.
   * @param atm The ATM volatilities for each time to expiration. The length should be equal to the length of timeToExpiration.
   * @param riskReversal The risk reversal figures.
   * @param strangle The strangle figures.
   */
  public SmileDeltaTermStructureParameters(final double[] timeToExpiration, final double[] delta, final double[] atm, final double[][] riskReversal, final double[][] strangle) {
    this(timeToExpiration, delta, atm, riskReversal, strangle, DEFAULT_INTERPOLATOR_EXPIRY);
  }

  /**
   * Constructor from market data.
   * @param timeToExpiration The time to expiration of each volatility smile, not null
   * @param delta The delta at which the volatilities are given. Common to all time to expiration. Not null
   * @param atm The ATM volatilities for each time to expiration. The length should be equal to the length of timeToExpiration. Not null
   * @param riskReversal The risk reversal figures, not null.
   * @param strangle The strangle figures, not null.
   * @param timeInterpolator The interpolator to be used in the time dimension, not null.
   */
  public SmileDeltaTermStructureParameters(final double[] timeToExpiration, final double[] delta, final double[] atm, final double[][] riskReversal, final double[][] strangle,
      final Interpolator1D timeInterpolator) {
    ArgumentChecker.notNull(timeToExpiration, "time to expiry");
    ArgumentChecker.notNull(delta, "delta");
    ArgumentChecker.notNull(atm, "ATM");
    ArgumentChecker.notNull(riskReversal, "risk reversal");
    ArgumentChecker.notNull(strangle, "strangle");
    ArgumentChecker.notNull(timeInterpolator, "time interpolator");
    final int nbExp = timeToExpiration.length;
    ArgumentChecker.isTrue(atm.length == nbExp, "ATM length should be coherent with time to expiration length");
    ArgumentChecker.isTrue(riskReversal.length == nbExp, "Risk reversal length should be coherent with time to expiration length");
    ArgumentChecker.isTrue(strangle.length == nbExp, "Risk reversal length should be coherent with time to expiration length");
    ArgumentChecker.isTrue(riskReversal[0].length == delta.length, "Risk reversal size should be coherent with time to delta length");
    ArgumentChecker.isTrue(strangle[0].length == delta.length, "Risk reversal size should be coherent with time to delta length");
    _timeToExpiration = timeToExpiration;
    _volatilityTerm = new SmileDeltaParameters[nbExp];
    for (int loopexp = 0; loopexp < nbExp; loopexp++) {
      _volatilityTerm[loopexp] = new SmileDeltaParameters(timeToExpiration[loopexp], atm[loopexp], delta, riskReversal[loopexp], strangle[loopexp]);
    }
    _timeInterpolator = timeInterpolator;
    ArgumentChecker.isTrue(_volatilityTerm[0].getVolatility().length > 1, "Need more than one volatility value to perform interpolation");
  }

  public SmileDeltaTermStructureParameters copy() {
    return new SmileDeltaTermStructureParameters(getVolatilityTerm(), getTimeInterpolator());
  }

  /**
   * Get smile at a given time. The smile is described by the volatilities at a given delta. The smile is obtained from the data by the given interpolator.
   * @param time The time to expiration.
   * @return The smile.
   */
  public SmileDeltaParameters getSmileForTime(final double time) {
    final int nbVol = _volatilityTerm[0].getVolatility().length;
    final int nbTime = _timeToExpiration.length;
    ArgumentChecker.isTrue(nbTime > 1, "Need more than one time value to perform interpolation");
    final double[] volatilityT = new double[nbVol];
    for (int loopvol = 0; loopvol < nbVol; loopvol++) {
      final double[] volDelta = new double[nbTime];
      for (int looptime = 0; looptime < nbTime; looptime++) {
        volDelta[looptime] = _volatilityTerm[looptime].getVolatility()[loopvol];
      }
      final ArrayInterpolator1DDataBundle interpData = new ArrayInterpolator1DDataBundle(_timeToExpiration, volDelta, true);
      volatilityT[loopvol] = _timeInterpolator.interpolate(interpData, time);
    }
    final SmileDeltaParameters smile = new SmileDeltaParameters(time, _volatilityTerm[0].getDelta(), volatilityT);
    return smile;
  }

  /**
   * Get the smile at a given time and the sensitivities with respect to the volatilities.
   * @param time The time to expiration.
   * @param volatilityAtTimeSensitivity The sensitivity to the volatilities of the smile at the given time.
   * After the methods, it contains the volatility sensitivity to the data points.
   * @return The smile
   */
  public SmileAndBucketedSensitivities getSmileAndSensitivitiesForTime(final double time, final double[] volatilityAtTimeSensitivity) {
    final int nbVol = _volatilityTerm[0].getVolatility().length;
    ArgumentChecker.isTrue(volatilityAtTimeSensitivity.length == nbVol, "Sensitivity with incorrect size");
    ArgumentChecker.isTrue(nbVol > 1, "Need more than one volatility value to perform interpolation");
    final int nbTime = _timeToExpiration.length;
    ArgumentChecker.isTrue(nbTime > 1, "Need more than one time value to perform interpolation");
    final double[] volatilityT = new double[nbVol];
    final double[][] volatilitySensitivity = new double[nbTime][nbVol];
    for (int loopvol = 0; loopvol < nbVol; loopvol++) {
      final double[] volDelta = new double[nbTime];
      for (int looptime = 0; looptime < nbTime; looptime++) {
        volDelta[looptime] = _volatilityTerm[looptime].getVolatility()[loopvol];
      }
      final ArrayInterpolator1DDataBundle interpData = new ArrayInterpolator1DDataBundle(_timeToExpiration, volDelta, true);
      final double[] volatilitySensitivityVol = _timeInterpolator.getNodeSensitivitiesForValue(interpData, time);
      for (int looptime = 0; looptime < nbTime; looptime++) {
        volatilitySensitivity[looptime][loopvol] = volatilitySensitivityVol[looptime] * volatilityAtTimeSensitivity[loopvol];
      }
      volatilityT[loopvol] = _timeInterpolator.interpolate(interpData, time);
    }
    final SmileDeltaParameters smile = new SmileDeltaParameters(time, _volatilityTerm[0].getDelta(), volatilityT);
    return new SmileAndBucketedSensitivities(smile, volatilitySensitivity);
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
   * Gets the time interpolator
   * @return The time interpolator
   */
  public Interpolator1D getTimeInterpolator() {
    return _timeInterpolator;
  }

  /**
   * Gets the volatility smiles from delta.
   * @return The volatility smiles.
   */
  public SmileDeltaParameters[] getVolatilityTerm() {
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
   * Gets put delta absolute value for all strikes. The ATM is 0.50 delta and the x call are transformed in 1-x put.
   * The output is in ascending order. 
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

  /**
   * Gets put delta absolute value for all strikes. The ATM is 0.50 delta and the x call are transformed in 1-x put.
   * The output is in descending order. 
   * @return The delta.
   */
  public double[] getDeltaFullReverse() {
    final int nbDelta = _volatilityTerm[0].getDelta().length;
    final double[] result = new double[2 * nbDelta + 1];
    for (int loopdelta = 0; loopdelta < nbDelta; loopdelta++) {
      result[2 * nbDelta - loopdelta] = _volatilityTerm[0].getDelta()[loopdelta];
      result[nbDelta - 1 - loopdelta] = 1.0 - _volatilityTerm[0].getDelta()[nbDelta - 1 - loopdelta];
    }
    result[nbDelta] = 0.50;
    return result;
  }

  /**
   * Get the volatility from a triple.
   * @param tsf The Time, Strike, Forward triple, not null
   * @return The volatility.
   */
  @Override
  public Double getVolatility(final Triple<Double, Double, Double> tsf) {
    throw new NotImplementedException();
  }

  @Override
  public VolatilityAndBucketedSensitivities getVolatilityAndSensitivities(final Triple<Double, Double, Double> tsf) {
    throw new NotImplementedException();
  }

  public BlackForexTermStructureParameters toTermStructureOnlyData(final Interpolator1D interpolator) {
    ArgumentChecker.notNull(interpolator, "interpolator");
    final int n = _timeToExpiration.length;
    final double[] timesToExpiry = new double[n];
    System.arraycopy(_timeToExpiration, 0, timesToExpiry, 0, n);
    final double[] vols = new double[n];
    final int atmIndex = (_volatilityTerm[0].getVolatility().length - 1) / 2;
    for (int i = 0; i < n; i++) {
      vols[i] = _volatilityTerm[i].getVolatility()[atmIndex];
    }
    return new BlackForexTermStructureParameters(InterpolatedDoublesCurve.fromSorted(timesToExpiry, vols, interpolator));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_timeToExpiration);
    result = prime * result + Arrays.hashCode(_volatilityTerm);
    result = prime * result + _timeInterpolator.hashCode();
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
    final SmileDeltaTermStructureParameters other = (SmileDeltaTermStructureParameters) obj;
    if (!Arrays.equals(_timeToExpiration, other._timeToExpiration)) {
      return false;
    }
    if (!Arrays.equals(_volatilityTerm, other._volatilityTerm)) {
      return false;
    }
    if (!ObjectUtils.equals(_timeInterpolator, other._timeInterpolator)) {
      return false;
    }
    return true;
  }

}
