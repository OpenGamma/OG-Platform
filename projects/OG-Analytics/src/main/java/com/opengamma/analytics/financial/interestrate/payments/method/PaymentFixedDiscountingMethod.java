/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.util.amount.StringAmount;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Methods related to fixed payments.
 */
public final class PaymentFixedDiscountingMethod implements PricingMethod {

  /**
   * The method unique instance.
   */
  private static final PaymentFixedDiscountingMethod INSTANCE = new PaymentFixedDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PaymentFixedDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private PaymentFixedDiscountingMethod() {
  }

  /**
   * Compute the the present value of a fixed payment by discounting to a parallel curve movement.
   * @param payment The payment.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public CurrencyAmount presentValue(PaymentFixed payment, YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(payment);
    final YieldAndDiscountCurve fundingCurve = curves.getCurve(payment.getFundingCurveName());
    double pv = payment.getAmount() * fundingCurve.getDiscountFactor(payment.getPaymentTime());
    return CurrencyAmount.of(payment.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(InstrumentDerivative instrument, YieldCurveBundle curves) {
    Validate.notNull(instrument);
    Validate.isTrue(instrument instanceof PaymentFixed, "Payment Fixed");
    return presentValue((PaymentFixed) instrument, curves);
  }

  /**
   * Computes the present value curve sensitivity of a fixed payment by discounting.
   * @param pay The fixed payment.
   * @param curves The curve bundle.
   * @return The sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(PaymentFixed pay, YieldCurveBundle curves) {
    final String curveName = pay.getFundingCurveName();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(curveName);
    final double time = pay.getPaymentTime();
    final DoublesPair s = new DoublesPair(time, -time * pay.getAmount() * discountingCurve.getDiscountFactor(time));
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(s);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, list);
    return new InterestRateCurveSensitivity(result);
  }

  /**
   * Compute the the present value curve sensitivity of a fixed payment by discounting to a parallel curve movement.
   * @param payment The payment.
   * @param curves The curve bundle.
   * @return The sensitivity.
   */
  public StringAmount presentValueParallelCurveSensitivity(PaymentFixed payment, YieldCurveBundle curves) {
    final String curveName = payment.getFundingCurveName();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(curveName);
    final double time = payment.getPaymentTime();
    double sensitivity = -time * payment.getAmount() * discountingCurve.getDiscountFactor(time);
    return StringAmount.from(curveName, sensitivity);
  }

}
