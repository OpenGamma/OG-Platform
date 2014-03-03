/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.cashflow;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;

/**
 * Gets the fixing period accrual factor for a coupon.
 */
public final class CouponFixingAccrualFactorVisitor extends InstrumentDerivativeVisitorAdapter<Void, Double> {
  /** A static instance */
  private static final InstrumentDerivativeVisitor<Void, Double> INSTANCE = new CouponFixingAccrualFactorVisitor();

  /**
   * Gets a static instance.
   * @return The instance.
   */
  public static InstrumentDerivativeVisitor<Void, Double> getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private CouponFixingAccrualFactorVisitor() {
  }

  @Override
  public Double visitCouponIbor(final CouponIbor payment) {
    return payment.getFixingAccrualFactor();
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread payment) {
    return payment.getFixingAccrualFactor();
  }

  @Override
  public Double visitCouponIborGearing(final CouponIborGearing payment) {
    return payment.getFixingAccrualFactor();
  }

}
