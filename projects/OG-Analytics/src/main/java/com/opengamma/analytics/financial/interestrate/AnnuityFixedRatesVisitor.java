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
 *
 */
public final class AnnuityFixedRatesVisitor extends InstrumentDerivativeVisitorAdapter<Void, double[]> {
  private static final InstrumentDerivativeVisitor<Void, Double> COUPON_VISITOR = new CouponFixedRateVisitor();
  private static final InstrumentDerivativeVisitor<Void, double[]> INSTANCE = new AnnuityFixedRatesVisitor();

  public static InstrumentDerivativeVisitor<Void, double[]> getInstance() {
    return INSTANCE;
  }

  private AnnuityFixedRatesVisitor() {
  }

  @Override
  public double[] visitGenericAnnuity(final Annuity<? extends Payment> annuity) {
    final int n = annuity.getNumberOfPayments();
    final double[] ca = new double[n];
    for (int i = 0; i < n; i++) {
      ca[i] = annuity.getNthPayment(i).accept(COUPON_VISITOR);
    }
    return ca;
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
