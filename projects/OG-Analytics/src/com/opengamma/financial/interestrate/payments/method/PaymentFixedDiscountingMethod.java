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

/**
 * Methods related to fixed payments.
 */
public class PaymentFixedDiscountingMethod implements PricingMethod {

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

}
