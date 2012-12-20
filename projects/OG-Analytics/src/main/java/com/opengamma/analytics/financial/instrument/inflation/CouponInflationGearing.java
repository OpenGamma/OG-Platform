/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.inflation;

/**
 * Interface to inflation coupons with gearing factors.
 */
public interface CouponInflationGearing {

  /**
   * Gets the multiplicative (gearing) factor.
   * @return The factor.
   */
  double getFactor();

}
