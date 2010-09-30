/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public final class PaymentPresentValueCalculator implements PaymentVisitor<YieldCurveBundle, Double> {

  private static final PaymentPresentValueCalculator s_instance = new PaymentPresentValueCalculator();

  public static PaymentPresentValueCalculator getInstance() {
    return s_instance;
  }

  private PaymentPresentValueCalculator() {
  }

  @Override
  public Double calculate(Payment p, YieldCurveBundle data) {
    return p.accept(this, data);
  }

  @Override
  public Double visitFixedPayment(FixedPayment payment, YieldCurveBundle data) {
    YieldAndDiscountCurve fundingCurve = data.getCurve(payment.getFundingCurveName());
    return payment.getAmount() * fundingCurve.getDiscountFactor(payment.getPaymentTime());
  }

  @Override
  public Double visitForwardLiborPayment(ForwardLiborPayment payment, YieldCurveBundle data) {
    YieldAndDiscountCurve fundingCurve = data.getCurve(payment.getFundingCurveName());
    final YieldAndDiscountCurve liborCurve = data.getCurve(payment.getLiborCurveName());
    double forward = (liborCurve.getDiscountFactor(payment.getLiborFixingTime()) / liborCurve.getDiscountFactor(payment.getLiborMaturityTime()) - 1) / payment.getForwardYearFraction();
    return payment.getNotional() * (forward + payment.getSpread()) * payment.getPaymentYearFraction() * fundingCurve.getDiscountFactor(payment.getPaymentTime());
  }

}
