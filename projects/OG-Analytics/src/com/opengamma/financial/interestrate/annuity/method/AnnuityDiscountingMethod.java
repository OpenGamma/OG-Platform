/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.method.CouponFixedDiscountingMethod;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Class used to compute values related to annuities.
 */
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

}
