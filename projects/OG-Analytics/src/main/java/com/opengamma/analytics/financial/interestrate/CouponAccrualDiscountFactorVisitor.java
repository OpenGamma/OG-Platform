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
public class CouponAccrualDiscountFactorVisitor extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, double[]> {

  @Override
  public double[] visitCouponIbor(final CouponIbor payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    return new double[] {forwardCurve.getDiscountFactor(payment.getFixingPeriodStartTime()), forwardCurve.getDiscountFactor(payment.getFixingPeriodEndTime())};
  }

  @Override
  public double[] visitCouponIborSpread(final CouponIborSpread payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    return new double[] {forwardCurve.getDiscountFactor(payment.getFixingPeriodStartTime()), forwardCurve.getDiscountFactor(payment.getFixingPeriodEndTime())};
  }

  @Override
  public double[] visitCouponIborGearing(final CouponIborGearing payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    return new double[] {forwardCurve.getDiscountFactor(payment.getFixingPeriodStartTime()), forwardCurve.getDiscountFactor(payment.getFixingPeriodEndTime())};
  }

}
