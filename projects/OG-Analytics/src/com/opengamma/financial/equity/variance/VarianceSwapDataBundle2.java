/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.StrikeType;

/**
 * Market data required to price a Variance Swap
 */
public class VarianceSwapDataBundle2<T extends StrikeType> {

  private final YieldAndDiscountCurve _discountCurve;
  private final ForwardCurve _forwardCurve;
  private final BlackVolatilitySurface<T> _volatilitySurface;

  /**
   * @param volSurf The volatility surface,
   * @param discCrv YieldAndDiscountCurve used to discount payments and in this case, also to compute the forward value of the underlying
   * @param forwardCurve
   */
  public VarianceSwapDataBundle2(final BlackVolatilitySurface<T> volSurf, final YieldAndDiscountCurve discCrv, final ForwardCurve forwardCurve) {
    Validate.notNull(discCrv, "discountCurve");
    Validate.notNull(volSurf, "volatilitySurface");
    Validate.notNull(forwardCurve, "forwardCurve");
    _discountCurve = discCrv;
    _volatilitySurface = volSurf;
    _forwardCurve = forwardCurve;
  }

  /**
   * Gets the discountCurve.
   * @return the discountCurve
   */
  public YieldAndDiscountCurve getDiscountCurve() {
    return _discountCurve;
  }

  /**
   * Gets the volatilitySurface.
   * @return the volatilitySurface
   */
  public BlackVolatilitySurface<T> getVolatilitySurface() {
    return _volatilitySurface;
  }

  /**
   * Gets the forwardUnderlying.
   * @return the forwardUnderlying
   */
  public final ForwardCurve getForwardCurve() {
    return _forwardCurve;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_discountCurve == null) ? 0 : _discountCurve.hashCode());
    result = prime * result + ((_forwardCurve == null) ? 0 : _forwardCurve.hashCode());
    result = prime * result + ((_volatilitySurface == null) ? 0 : _volatilitySurface.hashCode());
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    VarianceSwapDataBundle2<?> other = (VarianceSwapDataBundle2<?>) obj;
    if (_discountCurve == null) {
      if (other._discountCurve != null) {
        return false;
      }
    } else if (!_discountCurve.equals(other._discountCurve)) {
      return false;
    }
    if (_forwardCurve == null) {
      if (other._forwardCurve != null) {
        return false;
      }
    } else if (!_forwardCurve.equals(other._forwardCurve)) {
      return false;
    }
    if (_volatilitySurface == null) {
      if (other._volatilitySurface != null) {
        return false;
      }
    } else if (!_volatilitySurface.equals(other._volatilitySurface)) {
      return false;
    }
    return true;
  }

}
