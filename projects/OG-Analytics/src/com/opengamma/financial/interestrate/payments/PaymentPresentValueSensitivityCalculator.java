/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public final class PaymentPresentValueSensitivityCalculator implements PaymentVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> {

  private static final PaymentPresentValueSensitivityCalculator s_instance = new PaymentPresentValueSensitivityCalculator();

  public static PaymentPresentValueSensitivityCalculator getInstance() {
    return s_instance;
  }

  private PaymentPresentValueSensitivityCalculator() {
  }

  @Override
  public Map<String, List<DoublesPair>> calculate(Payment p, YieldCurveBundle data) {
    return p.accept(this, data);
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedPayment(FixedPayment payment, YieldCurveBundle data) {
    String curveName = payment.getFundingCurveName();
    YieldAndDiscountCurve curve = data.getCurve(curveName);
    double t = payment.getPaymentTime();

    final DoublesPair s = new DoublesPair(t, -t * payment.getAmount() * curve.getDiscountFactor(t));
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(s);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, list);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitForwardLiborPayment(ForwardLiborPayment payment, YieldCurveBundle data) {
    final String fundingCurveName = payment.getFundingCurveName();
    final String liborCurveName = payment.getLiborCurveName();
    final YieldAndDiscountCurve fundCurve = data.getCurve(fundingCurveName);
    final YieldAndDiscountCurve liborCurve = data.getCurve(liborCurveName);

    final double tPay = payment.getPaymentTime();
    final double ta = payment.getLiborFixingTime();
    final double tb = payment.getLiborMaturityTime();
    final double dfPay = fundCurve.getDiscountFactor(tPay);
    final double dfa = liborCurve.getDiscountFactor(ta);
    final double dfb = liborCurve.getDiscountFactor(tb);
    final double forward = (dfa / dfb - 1) / payment.getForwardYearFraction();
    final double notional = payment.getNotional();

    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();

    List<DoublesPair> temp = new ArrayList<DoublesPair>();
    DoublesPair s;
    s = new DoublesPair(tPay, -tPay * dfPay * notional * (forward + payment.getSpread()) * payment.getPaymentYearFraction());
    temp.add(s);

    if (!liborCurveName.equals(fundingCurveName)) {
      result.put(fundingCurveName, temp);
      temp = new ArrayList<DoublesPair>();
    }

    final double ratio = notional * dfPay * dfa / dfb * payment.getPaymentYearFraction() / payment.getForwardYearFraction();
    s = new DoublesPair(ta, -ta * ratio);
    temp.add(s);
    s = new DoublesPair(tb, tb * ratio);
    temp.add(s);

    result.put(liborCurveName, temp);

    return result;
  }

}
