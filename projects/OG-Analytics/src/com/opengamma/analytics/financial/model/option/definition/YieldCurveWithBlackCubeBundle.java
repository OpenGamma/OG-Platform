/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;

/**
 * Class describing the data required to price instruments by using a Black volatility cube and curves.
 */
public class YieldCurveWithBlackCubeBundle extends YieldCurveBundle {

  /**
   * The Black volatility cube. Not null.
   */
  private final InterpolatedDoublesSurface _parameters;

  /**
   * Constructor from Black volatility curve and curve bundle.
   * @param parameters The Black volatility cube.
   * @param curves Curve bundle.
   */
  public YieldCurveWithBlackCubeBundle(final InterpolatedDoublesSurface parameters, final YieldCurveBundle curves) {
    super(curves);
    Validate.notNull(parameters, "Volatility surface");
    _parameters = parameters;
  }

  @Override
  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names. The same BlackSwaptionParameters is used.
   * @return The bundle.
   */
  public YieldCurveWithBlackCubeBundle copy() {
    return new YieldCurveWithBlackCubeBundle(_parameters, this);
  }

  /**
   * Gets the Black volatility surface.
   * @return The surface.
   */
  public InterpolatedDoublesSurface getBlackParameters() {
    return _parameters;
  }

  /**
   * Gets the Black volatility at a given expiry-strike-other point. The meaning of "other' with depend on the underlying.
   * @param expiry The time to expiration.
   * @param strike The strike.
   * @return The volatility.
   */
  public double getVolatility(final double expiry, final double strike) {
    return _parameters.getZValue(expiry, strike);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _parameters.hashCode();
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
    YieldCurveWithBlackCubeBundle other = (YieldCurveWithBlackCubeBundle) obj;
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
