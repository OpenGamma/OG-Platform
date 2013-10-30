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
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class CouponProjectedPaymentVisitor extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, CurrencyAmount> {

  @Override
  public CurrencyAmount visitCouponIbor(final CouponIbor payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(payment.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(payment.getFixingPeriodEndTime()) - 1) /
        payment.getFixingAccrualFactor();
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional() * payment.getPaymentYearFraction() * forward);
  }

  @Override
  public CurrencyAmount visitCouponIborSpread(final CouponIborSpread payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(payment.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(payment.getFixingPeriodEndTime()) - 1) /
        payment.getFixingAccrualFactor();
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional() * payment.getPaymentYearFraction() * forward);
  }

  @Override
  public CurrencyAmount visitCouponIborGearing(final CouponIborGearing payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(payment.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(payment.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(payment.getFixingPeriodEndTime()) - 1) /
        payment.getFixingAccrualFactor();
    return CurrencyAmount.of(payment.getCurrency(), payment.getNotional() * payment.getPaymentYearFraction() * forward);
  }

}
