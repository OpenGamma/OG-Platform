/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import com.opengamma.financial.instrument.payment.CouponDefinition;

/**
 * A wrapper class for a AnnuityDefinition containing CouponDefinition.
 * @param <P> The coupon type.
 */
public class AnnuityCouponDefinition<P extends CouponDefinition> extends AnnuityDefinition<P> {

  /**
   * Constructor from a list of coupons.
   * @param coupons The coupons.
   */
  public AnnuityCouponDefinition(P[] coupons) {
    super(coupons);
  }

}
