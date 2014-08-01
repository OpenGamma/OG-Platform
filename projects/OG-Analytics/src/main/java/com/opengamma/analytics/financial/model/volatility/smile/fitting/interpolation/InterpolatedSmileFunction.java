/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborInArrearsSmileModelCapGenericReplicationMethod;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Wrapping {@link GeneralSmileInterpolator} for {@link CapFloorIborInArrearsSmileModelCapGenericReplicationMethod}
 */
public class InterpolatedSmileFunction {
  private final GeneralSmileInterpolator _interpolator;
  private final Function1D<Double, Double> _smileFunction;

  /**
   * Constructor where smile function is computed from specified interpolator and a set of data
   * @param interpolator The interpolator
   * @param forward The forward
   * @param strikes The strikes
   * @param expiry The expiry
   * @param impliedVols The volatilities
   */
  public InterpolatedSmileFunction(final GeneralSmileInterpolator interpolator, final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
    _interpolator = interpolator;
    _smileFunction = interpolator.getVolatilityFunction(forward, strikes, expiry, impliedVols);
  }

  /**
   * Get volatility for a given strike
   * @param strike The strike
   * @return The volatility
   */
  public Double getVolatility(final Double strike) {
    return _smileFunction.evaluate(strike);
  }

  /**
   * Access GeneralSmileInterpolator
   * @return _interpolator
   */
  public GeneralSmileInterpolator getInterpolator() {
    return _interpolator;
  }

}
