/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.method;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Class to compute the quantities related to swaps fixed / Ibor with spread (annuity, PVBP, coupon equivalent).
 * Both legs should be in the same currency.
 * The methods check that the coupons on the non-fixed leg are of the type CouponIborSpread.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedIborSpreadDiscountingMethod}
 */
@Deprecated
public final class SwapFixedIborSpreadDiscountingMethod extends SwapFixedCouponDiscountingMethod {

  /**
   * The method unique instance.
   */
  private static final SwapFixedIborSpreadDiscountingMethod INSTANCE = new SwapFixedIborSpreadDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwapFixedIborSpreadDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private SwapFixedIborSpreadDiscountingMethod() {
  }

  /**
   * The methods.
   */
  private static final CouponIborSpreadDiscountingMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingMethod.getInstance();

  /**
   * Computes the coupon equivalent of a swap with margins (all coupons on the non-fixed leg should be CouponIborSpread).
   * @param fixedCouponSwap The underlying swap.
   * @param pvbp The swap PVBP.
   * @param curves The curves.
   * @return The coupon equivalent.
   */
  public double couponEquivalentSpreadModified(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final double pvbp, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(fixedCouponSwap.getFirstLeg().getCurrency() == fixedCouponSwap.getSecondLeg().getCurrency(), "Both legs should be in the same currency");
    final double pvFixed = METHOD_ANNUITY.presentValuePositiveNotional(fixedCouponSwap.getFixedLeg(), curves).getAmount();
    final double pvSpread = presentValueSpreadPositiveNotional(fixedCouponSwap.getSecondLeg(), curves).getAmount();
    return (pvFixed - pvSpread) / pvbp;
  }

  /**
   * Computes the spread-modified swap forward rate, i.e. the pv of the floating leg without spread divided by the convention-modified PVBP.
   * <p> Reference: Swaption pricing, OG-Notes, version 1.4, August 2012.
   * @param fixedCouponSwap The underlying swap.
   * @param pvbp The swap PVBP.
   * @param curves The curves.
   * @return The spread-modified forward.
   */
  public double forwardSwapSpreadModified(final SwapFixedCoupon<? extends Payment> fixedCouponSwap, final double pvbp, final YieldCurveBundle curves) {
    final double pvFloatNoSpread = presentValueIborNoSpreadPositiveNotional(fixedCouponSwap.getSecondLeg(), curves).getAmount();
    return pvFloatNoSpread / pvbp;
  }

  /**
   * Computes the present value of the spreads in a leg made of CouponIborSpread. The absolute value of the notional is used.
   * @param leg The leg (or annuity).
   * @param curves The curves.
   * @return The present value.
   */
  public CurrencyAmount presentValueSpreadPositiveNotional(final Annuity<? extends Payment> leg, final YieldCurveBundle curves) {
    double pv = 0.0;
    for (int loopcpn = 0; loopcpn < leg.getNumberOfPayments(); loopcpn++) {
      ArgumentChecker.isTrue(leg.getNthPayment(loopcpn) instanceof CouponIborSpread, "Coupon should be Ibor with spread");
      final CouponIborSpread cpn = (CouponIborSpread) leg.getNthPayment(loopcpn);
      pv += Math.abs(cpn.getNotional()) * cpn.getSpread() * cpn.getPaymentYearFraction()
          * curves.getCurve(cpn.getFundingCurveName()).getDiscountFactor(cpn.getPaymentTime());
    }
    return CurrencyAmount.of(leg.getCurrency(), pv);
  }

  /**
   * Computes the present value of the Ibor leg made of CouponIborSpread without the spread (only the Ibor is valued). The absolute value of the notional is used.
   * @param leg The leg (or annuity).
   * @param curves The curves.
   * @return The present value.
   */
  public CurrencyAmount presentValueIborNoSpreadPositiveNotional(final Annuity<? extends Payment> leg, final YieldCurveBundle curves) {
    double pv = 0.0;
    for (int loopcpn = 0; loopcpn < leg.getNumberOfPayments(); loopcpn++) {
      ArgumentChecker.isTrue(leg.getNthPayment(loopcpn) instanceof CouponIborSpread, "Coupon should be Ibor with spread");
      pv += METHOD_CPN_IBOR_SPREAD.presentValueNoSpreadPositiveNotional((CouponIborSpread) leg.getNthPayment(loopcpn), curves).getAmount();
    }
    return CurrencyAmount.of(leg.getCurrency(), pv);
  }

}
