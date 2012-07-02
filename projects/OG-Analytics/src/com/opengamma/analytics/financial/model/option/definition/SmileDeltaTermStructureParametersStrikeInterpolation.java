/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
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
public class SmileDeltaTermStructureParametersStrikeInterpolation extends SmileDeltaTermStructureParameters implements VolatilityModel<Triple<Double, Double, Double>> {

  /**
   * The interpolator/extrapolator used in the strike dimension.
   */
  private final Interpolator1D _interpolatorStrike;

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
   * @param interpolator The interpolator used in the strike dimension.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation(final SmileDeltaParameters[] volatilityTerm, final Interpolator1D interpolator) {
    super(volatilityTerm);
    _interpolatorStrike = interpolator;
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
   * @param interpolator The interpolator used in the strike dimension.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation(final double[] timeToExpiration, final double[] delta, final double[][] volatility, final Interpolator1D interpolator) {
    super(timeToExpiration, delta, volatility);
    _interpolatorStrike = interpolator;
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
   * @param interpolatorStrike The interpolator used in the strike dimension.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation(final double[] timeToExpiration, final double[] delta, final double[] atm, final double[][] riskReversal, final double[][] strangle,
      final Interpolator1D interpolatorStrike) {
    super(timeToExpiration, delta, atm, riskReversal, strangle);
    _interpolatorStrike = interpolatorStrike;
  }

  /**
   * Constructor from market data.
   * @param timeToExpiration The time to expiration of each volatility smile.
   * @param delta The delta at which the volatilities are given. Common to all time to expiration.
   * @param atm The ATM volatilities for each time to expiration. The length should be equal to the length of timeToExpiration.
   * @param riskReversal The risk reversal figures.
   * @param strangle The strangle figures.
   * @param interpolatorStrike The interpolator used in the strike dimension.
   * @param interpolatorTime The interpolator used in the time dimension.
   */
  public SmileDeltaTermStructureParametersStrikeInterpolation(final double[] timeToExpiration, final double[] delta, final double[] atm, final double[][] riskReversal, final double[][] strangle,
      final Interpolator1D interpolatorStrike, final Interpolator1D interpolatorTime) {
    super(timeToExpiration, delta, atm, riskReversal, strangle, interpolatorTime);
    _interpolatorStrike = interpolatorStrike;
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
    final SmileDeltaParameters smile = smile(time);
    final double[] strikes = smile.getStrike(forward);
    final Interpolator1DDataBundle volatilityInterpolation = _interpolatorStrike.getDataBundle(strikes, smile.getVolatility());
    final double volatility = _interpolatorStrike.interpolate(volatilityInterpolation, strike);
    return volatility;
  }

  /**
   * Computes the volatility and the volatility sensitivity with respect to the volatility data points.
   * @param time The time to expiration.
   * @param strike The strike.
   * @param forward The forward.
   * @param bucketSensitivity The array is changed by the method. The array should have the correct size (nbExpiry x nbVolatility). 
   * After the methods, it contains the volatility sensitivity to the data points.
   * Only the lines of impacted dates are changed. The input data on the other lines will not be changed.
   * @return The volatility.
   */
  public double getVolatility(final double time, final double strike, final double forward, final double[][] bucketSensitivity) {
    ArgumentChecker.isTrue(time >= 0, "Positive time");
    SmileDeltaParameters smile = smile(time);
    final double[] strikes = smile.getStrike(forward);
    final Interpolator1DDataBundle volatilityInterpolation = _interpolatorStrike.getDataBundle(strikes, smile.getVolatility());
    final double volatility = _interpolatorStrike.interpolate(volatilityInterpolation, strike);
    // Backward sweep
    final double[] smileVolatilityBar = _interpolatorStrike.getNodeSensitivitiesForValue(volatilityInterpolation, strike);
    smile = smile(time, smileVolatilityBar, bucketSensitivity);
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
   * Gets the interpolator
   * @return The interpolator
   */
  public Interpolator1D getInterpolator() {
    return _interpolatorStrike;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _interpolatorStrike.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SmileDeltaTermStructureParametersStrikeInterpolation other = (SmileDeltaTermStructureParametersStrikeInterpolation) obj;
    if (!ObjectUtils.equals(_interpolatorStrike, other._interpolatorStrike)) {
      return false;
    }
    return true;
  }

}
