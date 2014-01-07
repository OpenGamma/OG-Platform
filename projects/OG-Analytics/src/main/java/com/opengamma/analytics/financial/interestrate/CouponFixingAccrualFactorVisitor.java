/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;

/**
 *
 */
public class CouponFixingAccrualFactorVisitor extends InstrumentDerivativeVisitorAdapter<Void, Double> {

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
