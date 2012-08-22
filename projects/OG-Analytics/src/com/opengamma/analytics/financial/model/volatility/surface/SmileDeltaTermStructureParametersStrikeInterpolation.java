/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.volatility.SmileAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Triple;

/**
 * Class describing the data required to describe a delta and expiration dependent smile from ATM, risk reversal and strangle as used in Forex market.
 * The delta used is the delta with respect to forward.
 */
public class SmileDeltaTermStructureParametersStrikeInterpolation extends SmileDeltaTermStructureParameters {

  /**
   * The interpolator/extrapolator used in the strike dimension.
   */
  private final Interpolator1D _strikeInterpolator;

  /**
   * The default interpolator: linear with flat extrapolation.
   */
  private static final Interpolator1D DEFAULT_INTERPOLATOR_STRIKE = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  /**
   * Constructor from volatility term structure. The default interpolator is used to interpolate in the strike dimension. The default interpolator is linear with flat extrapolation.
   * @param volatilityTerm The volatility description at the different expiration.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation(final SmileDeltaParameters[] volatilityTerm) {
    this(volatilityTerm, DEFAULT_INTERPOLATOR_STRIKE);
  }

  /**
   * Constructor from volatility term structure.
   * @param volatilityTerm The volatility description at the different expiration.
   * @param strikeInterpolator The interpolator used in the strike dimension.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation(final SmileDeltaParameters[] volatilityTerm, final Interpolator1D strikeInterpolator) {
    super(volatilityTerm);
    _strikeInterpolator = strikeInterpolator;
  }

  /**
   * Constructor from volatility term structure.
   * @param volatilityTerm The volatility description at the different expiration.
   * @param strikeInterpolator The interpolator used in the strike dimension.
   * @param timeInterpolator The interpolator used in the time dimension.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation(final SmileDeltaParameters[] volatilityTerm, final Interpolator1D strikeInterpolator, final Interpolator1D timeInterpolator) {
    super(volatilityTerm, timeInterpolator);
    ArgumentChecker.notNull(strikeInterpolator, "strike interpolator");
    _strikeInterpolator = strikeInterpolator;
  }

  /**
   * Constructor from market data. The default interpolator is used to interpolate in the strike dimension. The default interpolator is linear with flat extrapolation.
   * @param timeToExpiration The time to expiration of each volatility smile.
   * @param delta The delta at which the volatilities are given. Must be positive and sorted in ascending order. The put will have as delta the opposite of the numbers.
   * Common to all time to expiration.
   * @param volatility The volatilities at each delta.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation(final double[] timeToExpiration, final double[] delta, final double[][] volatility) {
    this(timeToExpiration, delta, volatility, DEFAULT_INTERPOLATOR_STRIKE);
  }

  /**
   * Constructor from market data.
   * @param timeToExpiration The time to expiration of each volatility smile.
   * @param delta The delta at which the volatilities are given. Must be positive and sorted in ascending order. The put will have as delta the opposite of the numbers.
   * Common to all time to expiration.
   * @param volatility The volatilities at each delta.
   * @param strikeInterpolator The interpolator used in the strike dimension.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation(final double[] timeToExpiration, final double[] delta, final double[][] volatility, final Interpolator1D strikeInterpolator) {
    super(timeToExpiration, delta, volatility);
    ArgumentChecker.notNull(strikeInterpolator, "strike interpolator");
    _strikeInterpolator = strikeInterpolator;
  }

  /**
   * Constructor from market data. The default interpolator is used to interpolate in the strike dimension. The default interpolator is linear with flat extrapolation.
   * @param timeToExpiration The time to expiration of each volatility smile.
   * @param delta The delta at which the volatilities are given. Common to all time to expiration.
   * @param atm The ATM volatilities for each time to expiration. The length should be equal to the length of timeToExpiration.
   * @param riskReversal The risk reversal figures.
   * @param strangle The strangle figures.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation(final double[] timeToExpiration, final double[] delta, final double[] atm, final double[][] riskReversal, final double[][] strangle) {
    this(timeToExpiration, delta, atm, riskReversal, strangle, DEFAULT_INTERPOLATOR_STRIKE);
  }

  /**
   * Constructor from market data.
   * @param timeToExpiration The time to expiration of each volatility smile.
   * @param delta The delta at which the volatilities are given. Common to all time to expiration.
   * @param atm The ATM volatilities for each time to expiration. The length should be equal to the length of timeToExpiration.
   * @param riskReversal The risk reversal figures.
   * @param strangle The strangle figures.
   * @param strikeInterpolator The interpolator used in the strike dimension.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation(final double[] timeToExpiration, final double[] delta, final double[] atm, final double[][] riskReversal, final double[][] strangle,
      final Interpolator1D strikeInterpolator) {
    super(timeToExpiration, delta, atm, riskReversal, strangle);
    ArgumentChecker.notNull(strikeInterpolator, "strike interpolator");
    _strikeInterpolator = strikeInterpolator;
  }

  /**
   * Constructor from market data.
   * @param timeToExpiration The time to expiration of each volatility smile.
   * @param delta The delta at which the volatilities are given. Common to all time to expiration.
   * @param atm The ATM volatilities for each time to expiration. The length should be equal to the length of timeToExpiration.
   * @param riskReversal The risk reversal figures.
   * @param strangle The strangle figures.
   * @param strikeInterpolator The interpolator used in the strike dimension.
   * @param timeInterpolator The interpolator used in the time dimension.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation(final double[] timeToExpiration, final double[] delta, final double[] atm, final double[][] riskReversal, final double[][] strangle,
      final Interpolator1D strikeInterpolator, final Interpolator1D timeInterpolator) {
    super(timeToExpiration, delta, atm, riskReversal, strangle, timeInterpolator);
    ArgumentChecker.notNull(strikeInterpolator, "strike interpolator");
    _strikeInterpolator = strikeInterpolator;
  }

  /**
   * Create a copy of the bundle
   * @return A copy of the bundle
   */
  @Override
  public SmileDeltaTermStructureParametersStrikeInterpolation copy() {
    return new SmileDeltaTermStructureParametersStrikeInterpolation(getVolatilityTerm(), getStrikeInterpolator(), getTimeInterpolator());
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
    final SmileDeltaParameters smile = getSmileForTime(time);
    final double[] strikes = smile.getStrike(forward);
    final Interpolator1DDataBundle volatilityInterpolation = _strikeInterpolator.getDataBundle(strikes, smile.getVolatility());
    final double volatility = _strikeInterpolator.interpolate(volatilityInterpolation, strike);
    return volatility;
  }

  /**
   * Computes the volatility and the volatility sensitivity with respect to the volatility data points.
   * @param time The time to expiration.
   * @param strike The strike.
   * @param forward The forward.
   * After the methods, it contains the volatility sensitivity to the data points.
   * Only the lines of impacted dates are changed. The input data on the other lines will not be changed.
   * @return The volatility.
   */
  public VolatilityAndBucketedSensitivities getVolatilityAndSensitivities(final double time, final double strike, final double forward) {
    ArgumentChecker.isTrue(time >= 0, "Positive time");
    final SmileDeltaParameters smile = getSmileForTime(time);
    final double[] strikes = smile.getStrike(forward);
    final Interpolator1DDataBundle volatilityInterpolation = _strikeInterpolator.getDataBundle(strikes, smile.getVolatility());
    final double volatility = _strikeInterpolator.interpolate(volatilityInterpolation, strike);
    // Backward sweep
    final double[] smileVolatilityBar = _strikeInterpolator.getNodeSensitivitiesForValue(volatilityInterpolation, strike);
    final SmileAndBucketedSensitivities smileAndSensitivities = getSmileAndSensitivitiesForTime(time, smileVolatilityBar);
    return new VolatilityAndBucketedSensitivities(volatility, smileAndSensitivities.getBucketedSensitivities());
  }

  /**
   * Get the volatility from a triple.
   * @param tsf The Time, Strike, Forward triple, not null
   * @return The volatility.
   */
  @Override
  public Double getVolatility(final Triple<Double, Double, Double> tsf) {
    ArgumentChecker.notNull(tsf, "time/strike/forward triple");
    return getVolatility(tsf.getFirst(), tsf.getSecond(), tsf.getThird());
  }

  @Override
  public VolatilityAndBucketedSensitivities getVolatilityAndSensitivities(final Triple<Double, Double, Double> tsf) {
    ArgumentChecker.notNull(tsf,  "time/strike/forward triple");
    return getVolatilityAndSensitivities(tsf.getFirst(), tsf.getSecond(), tsf.getThird());
  }
  /**
   * Gets the interpolator
   * @return The interpolator
   */
  public Interpolator1D getStrikeInterpolator() {
    return _strikeInterpolator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _strikeInterpolator.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SmileDeltaTermStructureParametersStrikeInterpolation other = (SmileDeltaTermStructureParametersStrikeInterpolation) obj;
    if (!ObjectUtils.equals(_strikeInterpolator, other._strikeInterpolator)) {
      return false;
    }
    return true;
  }

}
