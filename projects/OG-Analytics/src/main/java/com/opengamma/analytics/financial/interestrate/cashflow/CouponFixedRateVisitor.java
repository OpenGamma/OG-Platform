/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;

/**
 * Gets the fixed rate for a coupon.
 */
public final class CouponFixedRateVisitor extends InstrumentDerivativeVisitorAdapter<Void, Double> {
  /** The singleton instance */
  private static final InstrumentDerivativeVisitor<Void, Double> INSTANCE = new CouponFixedRateVisitor();

  /**
   * Gets the singleton instance.
   * @return The instance
   */
  public static InstrumentDerivativeVisitor<Void, Double> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponFixedRateVisitor() {
  }

  @Override
  public Double visitCouponFixed(final CouponFixed payment) {
    return payment.getFixedRate();
  }

  @Override
  public Double visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment) {
    return payment.getFixedRate();
  }

  @Override
  public Double visitCouponFixedCompounding(final CouponFixedCompounding payment) {
    return payment.getFixedRate();
  }

}
