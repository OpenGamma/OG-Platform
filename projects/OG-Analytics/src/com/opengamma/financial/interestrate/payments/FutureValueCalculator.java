/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public final class FutureValueCalculator implements PaymentVisitor<YieldCurveBundle, Pair<Double, Double>> {

  private static final FutureValueCalculator s_instance = new FutureValueCalculator();

  public static FutureValueCalculator getInstance() {
    return s_instance;
  }

  private FutureValueCalculator() {
  }

  @Override
  public Pair<Double, Double> calculate(Payment p, YieldCurveBundle data) {
    return p.accept(this, data);
  }

  @Override
  public Pair<Double, Double> visitFixedPayment(FixedPayment payment, YieldCurveBundle data) {
    return new DoublesPair(payment.getPaymentTime(), payment.getAmount());
  }

  @Override
  public Pair<Double, Double> visitForwardLiborPayment(ForwardLiborPayment payment, YieldCurveBundle data) {
    final YieldAndDiscountCurve curve = data.getCurve(payment.getLiborCurveName());
    double forward = (curve.getDiscountFactor(payment.getLiborFixingTime()) / curve.getDiscountFactor(payment.getLiborMaturityTime()) - 1) / payment.getForwardYearFraction();
    return new DoublesPair(payment.getPaymentTime(), (forward + payment.getSpread()) * payment.getPaymentYearFraction());
  }

}
