/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 *
 */
public class CouponForwardRateVisitor extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  @Override
  public Double visitCouponIbor(final CouponIbor payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    return (forwardCurve.getDiscountFactor(payment.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(payment.getFixingPeriodEndTime()) - 1) / payment.getFixingAccrualFactor();
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    return (forwardCurve.getDiscountFactor(payment.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(payment.getFixingPeriodEndTime()) - 1) / payment.getFixingAccrualFactor();
  }

  @Override
  public Double visitCouponIborGearing(final CouponIborGearing payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    return (forwardCurve.getDiscountFactor(payment.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(payment.getFixingPeriodEndTime()) - 1) / payment.getFixingAccrualFactor();
  }

}
