/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity.provider;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

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
   * @param multicurves The multi-curves provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final AnnuityCouponFixed annuity, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(annuity, "Annuity");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    MultipleCurrencyAmount pv = MultipleCurrencyAmount.of(annuity.getCurrency(), 0);
    for (final CouponFixed cpn : annuity.getPayments()) {
      pv = pv.plus(METHOD_CPN_FIXED.presentValue(cpn, multicurves));
    }
    return pv;
  }

  /**
   * Computes the present value of an annuity of fixed coupons with positive notional (abs(notional) is used for each coupon).
   * @param annuity The annuity.
   * @param multicurves The multi-curves provider.
   * @return The present value.
   */
  public CurrencyAmount presentValuePositiveNotional(final AnnuityCouponFixed annuity, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(annuity, "Annuity");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    CurrencyAmount pv = CurrencyAmount.of(annuity.getCurrency(), 0);
    for (final CouponFixed cpn : annuity.getPayments()) {
      pv = pv.plus(METHOD_CPN_FIXED.presentValuePositiveNotional(cpn, multicurves));
    }
    return pv;
  }

}
