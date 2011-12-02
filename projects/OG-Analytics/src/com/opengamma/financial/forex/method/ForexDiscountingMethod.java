/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Pricing method for Forex transactions (spot or forward) by discounting each payment.
 */
public final class ForexDiscountingMethod implements ForexPricingMethod {

  /**
   * The method unique instance.
   */
  private static final ForexDiscountingMethod INSTANCE = new ForexDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexDiscountingMethod() {
  }

  /**
   * Interest rate present value calculator by discounting.
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  /**
   * Interest rate present value rate sensitivity by discounting.
   */
  private static final PresentValueCurveSensitivityCalculator PVSC = PresentValueCurveSensitivityCalculator.getInstance();

  /**
   * Compute the present value by discounting in payment in its own currency.
   * @param fx The Forex derivative.
   * @param curves The curve bundle containing the discounting curves.
   * @return The multi-currency present value.
   */
  public MultipleCurrencyAmount presentValue(final Forex fx, final YieldCurveBundle curves) {
    final double pv1 = PVC.visit(fx.getPaymentCurrency1(), curves);
    final MultipleCurrencyAmount pv = MultipleCurrencyAmount.of(fx.getCurrency1(), pv1);
    final double pv2 = PVC.visit(fx.getPaymentCurrency2(), curves);
    return pv.plus(fx.getCurrency2(), pv2);
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.isTrue(derivative instanceof Forex, "Derivative should be Forex");
    return presentValue((Forex) derivative, curves);
  }

  @Override
  public MultipleCurrencyAmount currencyExposure(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    return presentValue(instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a forex transaction.
   * @param fx The Forex transaction.
   * @param curves The curves.
   * @return The sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final Forex fx, final YieldCurveBundle curves) {
    InterestRateCurveSensitivity result1 = new InterestRateCurveSensitivity(PVSC.visit(fx.getPaymentCurrency1(), curves));
    InterestRateCurveSensitivity result2 = new InterestRateCurveSensitivity(PVSC.visit(fx.getPaymentCurrency2(), curves));
    MultipleCurrencyInterestRateCurveSensitivity result = MultipleCurrencyInterestRateCurveSensitivity.of(fx.getCurrency1(), result1);
    result = result.plus(fx.getCurrency2(), result2);
    return result;
  }

}
