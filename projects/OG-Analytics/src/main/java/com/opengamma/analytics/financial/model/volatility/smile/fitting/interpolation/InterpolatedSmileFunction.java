/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborInArrearsSmileModelCapGenericReplicationMethod;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Interpolated (extrapolated) smile function returning volatility value for a given strike, used for {@link CapFloorIborInArrearsSmileModelCapGenericReplicationMethod}. 
 * Wrapping volatility function part of {@link GeneralSmileInterpolator} for type safety.
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
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNull(impliedVols, "impliedVols");

    _interpolator = interpolator;
    _smileFunction = interpolator.getVolatilityFunction(forward, strikes, expiry, impliedVols);
  }

  /**
   * Get volatility for a given strike
   * @param strike The strike
   * @return The volatility
   */
  public Double getVolatility(final Double strike) {
    ArgumentChecker.notNull(strike, "strike");
    return _smileFunction.evaluate(strike);
  }

  /**
   * Access GeneralSmileInterpolator
   * @return _interpolator
   */
  public GeneralSmileInterpolator getInterpolator() {
    return _interpolator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_interpolator == null) ? 0 : _interpolator.hashCode());
    result = prime * result + ((_smileFunction == null) ? 0 : _smileFunction.hashCode());
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
    if (!(obj instanceof InterpolatedSmileFunction)) {
      return false;
    }
    InterpolatedSmileFunction other = (InterpolatedSmileFunction) obj;
    if (_interpolator == null) {
      if (other._interpolator != null) {
        return false;
      }
    } else if (!_interpolator.equals(other._interpolator)) {
      return false;
    }
    if (_smileFunction == null) {
      if (other._smileFunction != null) {
        return false;
      }
    } else if (!_smileFunction.equals(other._smileFunction)) {
      return false;
    }
    return true;
  }

}
