/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Generic pricing method of CMS coupon as a CMS cap with strike 0.
 * @deprecated {@link PricingMethod} is deprecated
 */
@Deprecated
public class CouponCMSGenericMethod implements PricingMethod {

  /**
   * The pricing method for the CMS cap/floor.
   */
  private final PricingMethod _methodCap;

  /**
   * Constructor with the CMS cap/floor method.
   * @param methodCap The method.
   */
  public CouponCMSGenericMethod(final PricingMethod methodCap) {
    _methodCap = methodCap;
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof CouponCMS, "CMS coupon");
    final CouponCMS coupon = (CouponCMS) instrument;
    final CapFloorCMS cap0 = CapFloorCMS.from(coupon, 0.0, true);
    return _methodCap.presentValue(cap0, curves);
  }

}
