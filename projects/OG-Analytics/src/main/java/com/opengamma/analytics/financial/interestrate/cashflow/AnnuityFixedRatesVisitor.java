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
 * Gets the fixed rates for an annuity.
 */
public final class AnnuityFixedRatesVisitor extends InstrumentDerivativeVisitorAdapter<Void, Double[]> {
  /** The singleton instance */
  private static final InstrumentDerivativeVisitor<Void, Double[]> INSTANCE = new AnnuityFixedRatesVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDerivativeVisitor<Void, Double[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityFixedRatesVisitor() {
  }

  @Override
  public Double[] visitGenericAnnuity(final Annuity<? extends Payment> annuity) {
    final int n = annuity.getNumberOfPayments();
    final Double[] ca = new Double[n];
    for (int i = 0; i < n; i++) {
      try {
        ca[i] = annuity.getNthPayment(i).accept(CouponFixedRateVisitor.getInstance());
      } catch (final UnsupportedOperationException e) {
        // expected in the case where the coupon is floating
        ca[i] = null;
      }
    }
    return ca;
  }

  @Override
  public Double[] visitFixedCouponAnnuity(final AnnuityCouponFixed annuity) {
    return visitGenericAnnuity(annuity);
  }

  @Override
  public Double[] visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity) {
    return visitGenericAnnuity(annuity);
  }
}
