/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponIborRatchet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;

/**
 * Gets the forward rates for an annuity given a curve provider.
 */
public final class AnnuityForwardRatesVisitor extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, Double[]> {
  /** Gets the fixed rates for coupons */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> COUPON_VISITOR = new CouponForwardRateVisitor();
  /** The singleton instance */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double[]> INSTANCE = new AnnuityForwardRatesVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDerivativeVisitor<MulticurveProviderInterface, Double[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityForwardRatesVisitor() {
  }

  @Override
  public Double[] visitGenericAnnuity(final Annuity<? extends Payment> annuity, final MulticurveProviderInterface curves) {
    final int n = annuity.getNumberOfPayments();
    final List<Double> ca = new ArrayList<>();
    int count = 0;
    for (int i = 0; i < n; i++) {
      try {
        ca.add(annuity.getNthPayment(i).accept(COUPON_VISITOR, curves));
      } catch (final UnsupportedOperationException e) {
        // expected if the coupon has fixed
        ca.add(null);
      }
      count++;
    }
    return ca.toArray(new Double[count]);
  }

  @Override
  public Double[] visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final MulticurveProviderInterface curves) {
    return visitGenericAnnuity(annuity);
  }

  @Override
  public Double[] visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final MulticurveProviderInterface curves) {
    return visitGenericAnnuity(annuity);
  }
}
