/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;

/**
 * Gets the accrual year fractions for the coupons in an annuity.
 */
public final class AnnuityAccrualYearFractionsVisitor extends InstrumentDerivativeVisitorAdapter<Void, double[]> {
  /** A singleton instance */
  private static final InstrumentDerivativeVisitor<Void, double[]> INSTANCE = new AnnuityAccrualYearFractionsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDerivativeVisitor<Void, double[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityAccrualYearFractionsVisitor() {
  }

  @Override
  public double[] visitGenericAnnuity(final Annuity<? extends Payment> annuity) {
    final int n = annuity.getNumberOfPayments();
    final double[] fractions = new double[n];
    for (int i = 0; i < n; i++) {
      fractions[i] = annuity.getNthPayment(i).accept(CouponFixingAccrualFactorVisitor.getInstance());
    }
    return fractions;
  }

  @Override
  public double[] visitFixedCouponAnnuity(final AnnuityCouponFixed annuity) {
    return visitGenericAnnuity(annuity);
  }

  @Override
  public double[] visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity) {
    return visitGenericAnnuity(annuity);
  }

}
