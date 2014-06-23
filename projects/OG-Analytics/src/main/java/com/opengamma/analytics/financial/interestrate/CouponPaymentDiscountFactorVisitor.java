/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageSinglePeriod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFlatCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 *
 */
public class CouponPaymentDiscountFactorVisitor extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  @Override
  public Double visitCouponFixed(final CouponFixed payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIbor(final CouponIbor payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborAverage(final CouponIborAverage payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborSpread(final CouponIborSpread payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborGearing(final CouponIborGearing payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborCompounding(final CouponIborCompounding payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponOIS(final CouponON payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponONCompounded(final CouponONCompounded payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponCMS(final CouponCMS payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponFixedCompounding(final CouponFixedCompounding payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponONArithmeticAverageSpread(final CouponONArithmeticAverageSpread payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborAverageSinglePeriod(final CouponIborAverageSinglePeriod payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborAverageCompounding(final CouponIborAverageCompounding payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitCouponIborFlatCompoundingSpread(final CouponIborFlatCompoundingSpread payment, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(payment.getFundingCurveName());
    return curve.getDiscountFactor(payment.getPaymentTime());
  }
}
