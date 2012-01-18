/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceOld;

/**
 * Market data required to price a Variance Swap
 */
public class VarianceSwapDataBundle {

  private final YieldAndDiscountCurve _discountCurve;
  private final double _spotUnderlying;
  private final double _forwardUnderlying;
  private final BlackVolatilitySurfaceOld _volatilitySurface;

  /**
   * @param volSurf The volatility surface, 
   * @param discCrv YieldAndDiscountCurve used to discount payments and in this case, also to compute the forward value of the underlying
   * @param spotUnderlying The current value of the underlying
   * @param forwardUnderlying The current value of the underlying's forward. !This is the important one!
   */
  public VarianceSwapDataBundle(BlackVolatilitySurfaceOld volSurf, YieldAndDiscountCurve discCrv, double spotUnderlying, double forwardUnderlying) {
    Validate.notNull(discCrv, "discountCurve");
    Validate.notNull(volSurf, "volatilitySurface");
    _discountCurve = discCrv;
    _spotUnderlying = spotUnderlying;
    _forwardUnderlying = forwardUnderlying;
    _volatilitySurface = volSurf;
  }

  /**
   * Gets the discountCurve.
   * @return the discountCurve
   */
  public YieldAndDiscountCurve getDiscountCurve() {
    return _discountCurve;
  }

  /**
   * Gets the spotUnderlying.
   * @return the spotUnderlying
   */
  public double getSpotUnderlying() {
    return _spotUnderlying;
  }

  /**
   * Gets the volatilitySurface.
   * @return the volatilitySurface
   */
  public BlackVolatilitySurfaceOld getVolatilitySurface() {
    return _volatilitySurface;
  }

  /**
   * Gets the forwardUnderlying.
   * @return the forwardUnderlying
   */
  public final double getForwardUnderlying() {
    return _forwardUnderlying;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_discountCurve == null) ? 0 : _discountCurve.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_forwardUnderlying);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_spotUnderlying);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_volatilitySurface == null) ? 0 : _volatilitySurface.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VarianceSwapDataBundle)) {
      return false;
    }
    VarianceSwapDataBundle other = (VarianceSwapDataBundle) obj;
    if (_discountCurve == null) {
      if (other._discountCurve != null) {
        return false;
      }
    } else if (!_discountCurve.equals(other._discountCurve)) {
      return false;
    }
    if (Double.doubleToLongBits(_forwardUnderlying) != Double.doubleToLongBits(other._forwardUnderlying)) {
      return false;
    }
    if (Double.doubleToLongBits(_spotUnderlying) != Double.doubleToLongBits(other._spotUnderlying)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatilitySurface, other._volatilitySurface)) {
      return false;
    }
    return true;
  }

}
