/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedAccruedCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponFixedDiscountingMethod;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Class used to compute values related to annuities.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.annuity.provider.AnnuityDiscountingMethod}
 */
@Deprecated
public final class AnnuityDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final AnnuityDiscountingMethod INSTANCE = new AnnuityDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static AnnuityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityDiscountingMethod() {
  }

  /**
   * Methods.
   */
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();
  private static final CouponFixedAccruedCompoundingDiscountingMethod METHOD_CPN_ACCRUED = CouponFixedAccruedCompoundingDiscountingMethod.getInstance();

  /**
   * Computes the present value of an annuity of fixed coupons.
   * @param annuity The annuity.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final AnnuityCouponFixed annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    CurrencyAmount pv = CurrencyAmount.of(annuity.getCurrency(), 0);
    for (final CouponFixed cpn : annuity.getPayments()) {
      pv = pv.plus(METHOD_CPN_FIXED.presentValue(cpn, curves));
    }
    return pv;
  }

  /**
   * Computes the present value of an annuity of fixed coupons with positive notional (abs(notional) is used for each coupon).
   * @param annuity The annuity.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public CurrencyAmount presentValuePositiveNotional(final AnnuityCouponFixed annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    CurrencyAmount pv = CurrencyAmount.of(annuity.getCurrency(), 0);
    for (final CouponFixed cpn : annuity.getPayments()) {
      pv = pv.plus(METHOD_CPN_FIXED.presentValuePositiveNotional(cpn, curves));
    }
    return pv;
  }

  /**
   * Computes the present value of an annuity of fixed coupons with positive notional (abs(notional) is used for each coupon).
   * @param annuity The annuity.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public CurrencyAmount presentValuePositiveNotional(final Annuity<CouponFixedAccruedCompounding> annuity, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(annuity);
    CurrencyAmount pv = CurrencyAmount.of(annuity.getCurrency(), 0);
    for (final Payment cpn : annuity.getPayments()) {
      pv = pv.plus(METHOD_CPN_ACCRUED.presentValuePositiveNotional((CouponFixedAccruedCompounding) cpn, curves));
    }
    return pv;
  }
}
