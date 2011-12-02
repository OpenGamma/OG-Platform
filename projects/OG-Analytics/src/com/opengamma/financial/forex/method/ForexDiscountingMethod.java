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
import com.opengamma.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.method.PaymentFixedDiscountingMethod;
import com.opengamma.util.money.CurrencyAmount;
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
   * Fixed payments method.
   */
  private static final PaymentFixedDiscountingMethod METHOD_PAY = new PaymentFixedDiscountingMethod();
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
    CurrencyAmount pv1 = METHOD_PAY.presentValue(fx.getPaymentCurrency1(), curves);
    CurrencyAmount pv2 = METHOD_PAY.presentValue(fx.getPaymentCurrency2(), curves);
    return MultipleCurrencyAmount.of(pv1, pv2);
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
   * Computes the forward exchange rate associated to the Forex instrument (1 Cyy1 = fwd Cyy2).
   * @param fx The Forex derivative.
   * @param curves The curve bundle (with FX rates).
   * @return The forward rate.
   */
  public double forwardForexRate(final Forex fx, final YieldCurveWithFXBundle curves) {
    final double dfDomestic = curves.getCurve(fx.getPaymentCurrency2().getFundingCurveName()).getDiscountFactor(fx.getPaymentTime());
    final double dfForeign = curves.getCurve(fx.getPaymentCurrency1().getFundingCurveName()).getDiscountFactor(fx.getPaymentTime());
    final double spot = curves.getFxRate(fx.getCurrency1(), fx.getCurrency2());
    return spot * dfForeign / dfDomestic;
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
