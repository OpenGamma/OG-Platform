/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Pricing method for forex transactions by discounting each payment.
 */
public class ForexDiscountingMethod implements ForexPricingMethod {

  /**
   * Interest rate present value calculator by discounting.
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueSensitivityCalculator PVSC = PresentValueSensitivityCalculator.getInstance();

  /**
   * Compute the present value by discounting in payment in its own currency.
   * @param fx The Forex derivative.
   * @param curves The curve bundle containing the discounting curves.
   * @return The multi-currency present value.
   */
  public MultipleCurrencyAmount presentValue(final Forex fx, final YieldCurveBundle curves) {
    double pv1 = PVC.visit(fx.getPaymentCurrency1(), curves);
    MultipleCurrencyAmount pv = new MultipleCurrencyAmount(fx.getCurrency1(), pv1);
    double pv2 = PVC.visit(fx.getPaymentCurrency2(), curves);
    pv.add(fx.getCurrency2(), pv2);
    return pv;
  }

  @Override
  public MultipleCurrencyAmount presentValue(final ForexDerivative derivative, final YieldCurveBundle curves) {
    Validate.isTrue(derivative instanceof Forex, "Derivative should be Forex");
    return presentValue((Forex) derivative, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a forex transaction.
   * @param fx The Forex transaction.
   * @param curves The curves.
   * @return The sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final Forex fx, final YieldCurveBundle curves) {
    PresentValueSensitivity result = new PresentValueSensitivity(PVSC.visit(fx.getPaymentCurrency1(), curves));
    result = result.add(new PresentValueSensitivity(PVSC.visit(fx.getPaymentCurrency2(), curves)));
    return result;
  }

}
