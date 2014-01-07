/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.parameters;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.definition.InflationYearOnYearCapFloorParameters;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 *  Class describing the Black volatility surface used in inflation year on year cap/floor modeling. The inflation year on year rate (\{CPI(T_{i+1})}{CPI(T_{i})})rate is assumed to be normal.
 */
public class BlackSmileCapInflationYearOnYearParameters implements VolatilityModel<double[]> {

  /**
   * The volatility surface. The dimensions are the expiration and the strike. Not null.
   */
  private final InterpolatedDoublesSurface _volatility;
  /**
   * The index price for which the volatility is valid. Not null.
   */
  private final IndexPrice _index;

  /**
   * Constructor from the parameter surfaces.
   * @param volatility The Black volatility curve.
   * @param index The index price for which the volatility is valid.
   */
  public BlackSmileCapInflationYearOnYearParameters(final InterpolatedDoublesSurface volatility, final IndexPrice index) {
    ArgumentChecker.notNull(volatility, "volatility");
    ArgumentChecker.notNull(index, "index");
    _volatility = volatility;
    _index = index;
  }

  /**
   * Constructor from the parameter surfaces.
   * @param expiryTimes The Black volatility curve.
   * @param strikes The Black volatility curve.
   * @param volatility The Black volatility cube.
   * @param interpolator The interpolator necessary to Black volatility surface from the black volatility cube.
   * @param index The index price for which the volatility is valid.
   */
  public BlackSmileCapInflationYearOnYearParameters(final double[] expiryTimes, final double[] strikes, final double[][] volatility, final Interpolator2D interpolator, final IndexPrice index) {
    ArgumentChecker.notNull(volatility, "volatility");
    ArgumentChecker.notNull(expiryTimes, "expiryTimes");
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isTrue(expiryTimes.length == volatility.length, null);
    ArgumentChecker.isTrue(strikes.length == volatility[0].length, null);

    final DoublesPair[] xyData = new DoublesPair[expiryTimes.length * strikes.length];
    final double[] volatilityVector = new double[expiryTimes.length * strikes.length];
    for (int i = 0; i < expiryTimes.length; i++) {
      for (int j = 0; j < strikes.length; j++) {
        xyData[i + j * expiryTimes.length] = DoublesPair.of(expiryTimes[i], strikes[j]);
        volatilityVector[i + j * expiryTimes.length] = volatility[i][j];
      }
    }
    _volatility = InterpolatedDoublesSurface.from(xyData, volatilityVector, interpolator);
    _index = index;
  }

  /**
   * Constructor from the parameter surfaces.
   * @param parameters The Black volatility curve.
   * @param interpolator The Black volatility curve.
   */
  public BlackSmileCapInflationYearOnYearParameters(final InflationYearOnYearCapFloorParameters parameters, final Interpolator2D interpolator) {
    ArgumentChecker.notNull(interpolator, "interpolator");

    final double[] expiryTimes = parameters.getExpiryTimes();
    final double[] strikes = parameters.getStrikes();
    final double[][] volatility = parameters.getVolatility();

    final DoublesPair[] xyData = new DoublesPair[expiryTimes.length * strikes.length];
    final double[] volatilityVector = new double[expiryTimes.length * strikes.length];
    for (int i = 0; i < expiryTimes.length; i++) {
      for (int j = 0; j < strikes.length; j++) {
        xyData[i + j * expiryTimes.length] = DoublesPair.of(expiryTimes[i], strikes[j]);
        volatilityVector[i + j * expiryTimes.length] = volatility[i][j];
      }
    }
    _volatility = InterpolatedDoublesSurface.from(xyData, volatilityVector, interpolator);
    _index = parameters.getIndex();

  }

  /**
   * Constructor from the parameter surfaces and default interpolator.
   * @param parameters The Black volatility curve.
   */
  public BlackSmileCapInflationYearOnYearParameters(final InflationYearOnYearCapFloorParameters parameters) {

    final double[] expiryTimes = parameters.getExpiryTimes();
    final double[] strikes = parameters.getStrikes();
    final double[][] volatility = parameters.getVolatility();

    final DoublesPair[] xyData = new DoublesPair[expiryTimes.length * strikes.length];
    final double[] volatilityVector = new double[expiryTimes.length * strikes.length];
    for (int i = 0; i < expiryTimes.length; i++) {
      for (int j = 0; j < strikes.length; j++) {
        xyData[i + j * expiryTimes.length] = DoublesPair.of(expiryTimes[i], strikes[j]);
        volatilityVector[i + j * expiryTimes.length] = volatility[i][j];
      }
    }

    final Interpolator1D linearFlat = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    final GridInterpolator2D interpolator = new GridInterpolator2D(linearFlat, linearFlat);
    _volatility = InterpolatedDoublesSurface.from(xyData, volatilityVector, interpolator);
    _index = parameters.getIndex();

  }

  /**
   * Return the volatility for a time to expiration and strike.
   * @param expiration The time to expiration.
   * @param strike The strike.
   * @return The volatility.
   */
  public double getVolatility(final double expiration, final double strike) {
    return _volatility.getZValue(expiration, strike);
  }

  /**
   * Return the volatility surface.
   * @return The volatility surface.
   */
  public InterpolatedDoublesSurface getVolatilitySurface() {
    return _volatility;
  }

  @Override
  /**
   * Return the volatility for a expiration tenor array.
   * @param data An array of one doubles with the expiration.
   * @return The volatility.
   */
  public Double getVolatility(final double[] data) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(data.length == 2, "data should have two components (expiration and strike)");
    return getVolatility(data[0], data[1]);
  }

  /**
   * Gets the Ibor index for which the volatility is valid.
   * @return The index.
   */
  public IndexPrice getIndex() {
    return _index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _index.hashCode();
    result = prime * result + _volatility.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackSmileCapInflationYearOnYearParameters)) {
      return false;
    }
    final BlackSmileCapInflationYearOnYearParameters other = (BlackSmileCapInflationYearOnYearParameters) obj;
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatility, other._volatility)) {
      return false;
    }
    return true;
  }

}
