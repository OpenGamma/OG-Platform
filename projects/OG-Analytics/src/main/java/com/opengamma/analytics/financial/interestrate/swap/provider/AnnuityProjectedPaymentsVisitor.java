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
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Returns the projected amounts of the annuity.
 */
public final class AnnuityProjectedPaymentsVisitor extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, CurrencyAmount[]> {
  /** Returns the present value of the coupons */
  private static final InstrumentDerivativeVisitor<ParameterProviderInterface, MultipleCurrencyAmount> COUPON_VISITOR =
      PresentValueDiscountingCalculator.getInstance();

  /** Returns the discount factor of the coupons */
  private static final CouponPaymentDiscountFactorVisitor DISCOUNT_FACTOR_VISITOR = new CouponPaymentDiscountFactorVisitor();
  
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
        double df = payment.accept(DISCOUNT_FACTOR_VISITOR, curves);
        ca.add(payment.accept(COUPON_VISITOR, curves).getCurrencyAmount(payment.getCurrency()).multipliedBy(1 / df));
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
    return visitGenericAnnuity(annuity, curves);
  }

  @Override
  public CurrencyAmount[] visitAnnuityCouponIborRatchet(final AnnuityCouponIborRatchet annuity, final MulticurveProviderInterface curves) {
    return visitGenericAnnuity(annuity);
  }
}
