/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.parameters;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.volatility.VolatilityModel;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing the Black volatility surface and shift curve used in cap/floor modeling.
 * The underlying model is a shifted log-normal model, with the shift expiry dependent.
 */
public class BlackSmileShiftCapParameters implements VolatilityModel<double[]> {

  /**
   * The volatility surface. The dimensions are the expiration and the strike. Not null.
   */
  private final Surface<Double, Double, Double> _volatility;
  /**
   * The shift curve. The dimension is the expiration. Not null.
   */
  private final Curve<Double, Double> _shift;
  /**
   * The Ibor index for which the volatility is valid. Not null.
   */
  private final IborIndex _index;

  /**
   * Constructor from the parameter surfaces. The default SABR volatility formula is HaganVolatilityFunction.
   * @param volatility The Black volatility surface.
   * @param shift The shift curve.
   * @param index The Ibor index for which the volatility is valid.
   */
  public BlackSmileShiftCapParameters(final Surface<Double, Double, Double> volatility, final Curve<Double, Double> shift, final IborIndex index) {
    ArgumentChecker.notNull(volatility, "volatility");
    ArgumentChecker.notNull(index, "index");
    _volatility = volatility;
    _shift = shift;
    _index = index;
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
   * Returns the shift for the given expiration time.
   * @param expiration The expiration.
   * @return The shift.
   */
  public double getShift(final double expiration) {
    return _shift.getYValue(expiration);
  }

  /**
   * Gets the Ibor index for which the volatility is valid.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _index.hashCode();
    result = prime * result + _shift.hashCode();
    result = prime * result + _volatility.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackSmileShiftCapParameters)) {
      return false;
    }
    final BlackSmileShiftCapParameters other = (BlackSmileShiftCapParameters) obj;
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatility, other._volatility)) {
      return false;
    }
    if (!ObjectUtils.equals(_shift, other._shift)) {
      return false;
    }
    return true;
  }

}
