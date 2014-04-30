/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 * Gets the discount factors for the payment times of the coupons in an annuity.
 */
public final class AnnuityDiscountFactorsVisitor extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, double[]> {
  /** Gets the discount factors for coupons */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> COUPON_VISITOR = new CouponPaymentDiscountFactorVisitor();
  /** A singleton instance */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, double[]> INSTANCE = new AnnuityDiscountFactorsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDerivativeVisitor<MulticurveProviderInterface, double[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityDiscountFactorsVisitor() {
  }

  @Override
  public double[] visitGenericAnnuity(final Annuity<? extends Payment> annuity, final MulticurveProviderInterface curves) {
    final int n = annuity.getNumberOfPayments();
    final double[] fractions = new double[n];
    for (int i = 0; i < n; i++) {
      fractions[i] = annuity.getNthPayment(i).accept(COUPON_VISITOR, curves);
    }
    return fractions;
  }

  @Override
  public double[] visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final MulticurveProviderInterface curves) {
    return visitGenericAnnuity(annuity, curves);
  }

  @Override
  public double[] visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final MulticurveProviderInterface curves) {
    return visitGenericAnnuity(annuity, curves);
  }
}
