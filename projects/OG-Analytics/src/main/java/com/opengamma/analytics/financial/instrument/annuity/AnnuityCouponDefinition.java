/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * A wrapper class for a AnnuityDefinition containing CouponDefinition.
 * @param <P> The coupon type.
 */
public class AnnuityCouponDefinition<P extends CouponDefinition> extends AnnuityDefinition<P> {

  /**
   * Constructor from a list of coupons.
   * @param coupons The coupons.
   * @param calendar The holiday calendar, not null
   */
  public AnnuityCouponDefinition(final P[] coupons, final Calendar calendar) {
    super(coupons, calendar);
  }

}
