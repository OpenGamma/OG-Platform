/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Methods related to fixed coupons.
 */
public final class CouponFixedDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final CouponFixedDiscountingMethod INSTANCE = new CouponFixedDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static CouponFixedDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponFixedDiscountingMethod() {
  }

  /**
   * Computes the present value of a fixed coupon by discounting.
   * @param cpn The coupon.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public CurrencyAmount presentValue(CouponFixed cpn, YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(cpn);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(cpn.getFundingCurveName());
    double pv = cpn.getAmount() * fundingCurve.getDiscountFactor(cpn.getPaymentTime());
    return CurrencyAmount.of(cpn.getCurrency(), pv);
  }

}
