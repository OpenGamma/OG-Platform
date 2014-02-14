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
import com.opengamma.util.money.CurrencyAmount;

/**
 * Gets the forward rates for an annuity given a bundle of yield curves.
 */
public final class AnnuityProjectedPaymentsVisitor extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, CurrencyAmount[]> {
  /** Gets the fixed rates for coupons */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, CurrencyAmount> COUPON_VISITOR = new CouponProjectedPaymentVisitor();
  /** The singleton instance */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, CurrencyAmount[]> INSTANCE = new AnnuityProjectedPaymentsVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDerivativeVisitor<MulticurveProviderInterface, CurrencyAmount[]> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private AnnuityProjectedPaymentsVisitor() {
  }

  @Override
  public CurrencyAmount[] visitGenericAnnuity(final Annuity<? extends Payment> annuity, final MulticurveProviderInterface curves) {
    final int n = annuity.getNumberOfPayments();
    final List<CurrencyAmount> ca = new ArrayList<>();
    int count = 0;
    for (int i = 0; i < n; i++) {
      final Payment payment = annuity.getNthPayment(i);
      try {
        ca.add(payment.accept(COUPON_VISITOR, curves));
      } catch (final UnsupportedOperationException e) {
        // for the case where the coupon has fixed
        ca.add(null);
      }
      count++;
    }
    return ca.toArray(new CurrencyAmount[count]);
  }

  @Override
  public CurrencyAmount[] visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final MulticurveProviderInterface curves) {
    return visitGenericAnnuity(annuity);
  }

  @Override
  public CurrencyAmount[] visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final MulticurveProviderInterface curves) {
    return visitGenericAnnuity(annuity);
  }
}
