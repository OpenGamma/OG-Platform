/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.inflation;

/**
 * Interface to inflation coupons with margin factors.
 */
public interface CouponInflationWithMargin {

  /**
   * Gets the additive (margin) factor.
   * @return The factor.
   */
  double getFactor();

}
