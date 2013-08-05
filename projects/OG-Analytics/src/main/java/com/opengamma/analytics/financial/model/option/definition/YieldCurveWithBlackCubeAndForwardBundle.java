/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.surface.Surface;

/**
 * Class describing the data required to price instruments by using a Black volatility cube and curves.
 * @deprecated The parent class is deprecated
 */
@Deprecated
public class YieldCurveWithBlackCubeAndForwardBundle extends YieldCurveBundle {

  public static YieldCurveWithBlackCubeAndForwardBundle from(final YieldCurveWithBlackCubeBundle data, final double forward) {
    return new YieldCurveWithBlackCubeAndForwardBundle(data.getBlackParameters(), data, forward);
  }

  /**
   * The Black volatility cube. Not null.
   */
  private final Surface<Double, Double, Double> _parameters;
  private final double _forward;

  /**
   * Constructor from Black volatility curve and curve bundle.
   * @param parameters The Black volatility cube.
   * @param curves Curve bundle.
   * @param forward The forward.
   */
  public YieldCurveWithBlackCubeAndForwardBundle(final Surface<Double, Double, Double> parameters, final YieldCurveBundle curves, final double forward) {
    super(curves);
    Validate.notNull(parameters, "Volatility surface");
    _parameters = parameters;
    _forward = forward;
  }

  @Override
  /**
   * Create a new copy of the bundle using a new map and the same curve and curve names. The same BlackSwaptionParameters is used.
   * @return The bundle.
   */
  public YieldCurveWithBlackCubeAndForwardBundle copy() {
    return new YieldCurveWithBlackCubeAndForwardBundle(_parameters, this, _forward);
  }

  /**
   * Gets the Black volatility surface.
   * @return The surface.
   */
  public Surface<Double, Double, Double> getBlackParameters() {
    return _parameters;
  }

  public double getForward() {
    return _forward;
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
    //TODO
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
    final YieldCurveWithBlackCubeAndForwardBundle other = (YieldCurveWithBlackCubeAndForwardBundle) obj;
    //TODO
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
