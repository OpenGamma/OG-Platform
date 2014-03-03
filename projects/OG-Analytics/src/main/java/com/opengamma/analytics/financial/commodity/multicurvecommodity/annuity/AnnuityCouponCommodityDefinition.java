/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.annuity;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.definition.CouponCommodityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * A wrapper class for a AnnuityDefinition containing CouponCommodityDefinition.
 * @param <P> The coupon type.
 */
public class AnnuityCouponCommodityDefinition<P extends CouponCommodityDefinition> extends AnnuityDefinition<P> {
  /**
   * Constructor from a list of coupons.
   * @param coupons The coupons.
   * @param calendar The holiday calendar, not null
   */
  public AnnuityCouponCommodityDefinition(final P[] coupons, final Calendar calendar) {
    super(coupons, calendar);
  }

}
