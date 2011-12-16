/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.surface.StringValue;

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
   * Compute the the present value curve sensitivity of a fixed payment by discounting to a parallel curve movement.
   * @param payment The payment.
   * @param curves The curve bundle.
   * @return The sensitivity.
   */
  public StringValue presentValueParallelCurveSensitivity(PaymentFixed payment, YieldCurveBundle curves) {
    final String curveName = payment.getFundingCurveName();
    final YieldAndDiscountCurve discountingCurve = curves.getCurve(curveName);
    final double time = payment.getPaymentTime();
    double sensitivity = -time * payment.getAmount() * discountingCurve.getDiscountFactor(time);
    return StringValue.from(curveName, sensitivity);
  }

}
