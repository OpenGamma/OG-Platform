/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap.pricing;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

import org.apache.commons.lang.Validate;

/**
 * Market data required to price a Variance Swap
 * TODO Add hashcode and all that
 */
public class VarianceSwapDataBundle {

  private final YieldAndDiscountCurve _discountCurve;
  private final double _spotUnderlying;
  private final VolatilitySurface _volatilitySurface;

  /**
   * @param discCrv YieldAndDiscountCurve used to discount payments and in this case, also to compute the forward value of the underlying
   * @param spotUnderlying The current value of the underlying
   * @param volSurf The volatility surface, 
   */
  public VarianceSwapDataBundle(VolatilitySurface volSurf, YieldAndDiscountCurve discCrv, double spotUnderlying) {
    Validate.notNull(discCrv, "discountCurve");
    Validate.notNull(volSurf, "volatilitySurface");
    _discountCurve = discCrv;
    _spotUnderlying = spotUnderlying;
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
  public VolatilitySurface getVolatilitySurface() {
    return _volatilitySurface;
  }

}
