/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Gets the payment amount for a coupon.
 */
public final class CouponPaymentVisitor extends InstrumentDerivativeVisitorAdapter<Void, CurrencyAmount> {
  /** The singleton instance */
  private static final InstrumentDerivativeVisitor<Void, CurrencyAmount> INSTANCE = new CouponPaymentVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDerivativeVisitor<Void, CurrencyAmount> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponPaymentVisitor() {
  }

  @Override
  public CurrencyAmount visitCouponFixed(final CouponFixed payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional() * payment.getPaymentYearFraction() * payment.getFixedRate());
  }

  @Override
  public CurrencyAmount visitCouponFixedCompounding(final CouponFixedCompounding payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotionalAccrued() - payment.getNotional());
  }

}
