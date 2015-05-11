/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class CouponPaymentVisitor extends InstrumentDerivativeVisitorAdapter<Void, CurrencyAmount> {

  @Override
  public CurrencyAmount visitCouponFixed(final CouponFixed payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional() * payment.getPaymentYearFraction() * payment.getFixedRate());
  }

  @Override
  public CurrencyAmount visitCouponFixedCompounding(final CouponFixedCompounding payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotionalAccrued() - payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponOIS(final CouponON payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotionalAccrued() - payment.getNotional());
  }

  @Override
  public CurrencyAmount visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment) {
    return CurrencyAmount.of(payment.getCurrency(), payment.getAmount());
  }
}
