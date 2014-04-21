/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;

/**
 * Gets the discount factors for the payment times of the coupons in an annuity.
 * @deprecated This class uses deprecated functionality.
 */
@Deprecated
public final class AnnuityDiscountFactorsVisitor extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, double[]> {
  /** Gets the discount factors for coupons */
  private static final InstrumentDerivativeVisitor<YieldCurveBundle, Double> COUPON_VISITOR = new CouponPaymentDiscountFactorVisitor();
  /** A singleton instance */
  private static final InstrumentDerivativeVisitor<YieldCurveBundle, double[]> INSTANCE = new AnnuityDiscountFactorsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDerivativeVisitor<YieldCurveBundle, double[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityDiscountFactorsVisitor() {
  }

  @Override
  public double[] visitGenericAnnuity(final Annuity<? extends Payment> annuity, final YieldCurveBundle curves) {
    final int n = annuity.getNumberOfPayments();
    final double[] fractions = new double[n];
    for (int i = 0; i < n; i++) {
      fractions[i] = annuity.getNthPayment(i).accept(COUPON_VISITOR, curves);
    }
    return fractions;
  }

  @Override
  public double[] visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }

  @Override
  public double[] visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final YieldCurveBundle curves) {
    return visitGenericAnnuity(annuity, curves);
  }
}
