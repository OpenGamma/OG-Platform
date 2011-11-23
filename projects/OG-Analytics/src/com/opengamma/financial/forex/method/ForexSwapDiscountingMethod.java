/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.derivative.ForexSwap;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Pricing method for Forex swap transactions by discounting each payment.
 */
public final class ForexSwapDiscountingMethod implements ForexPricingMethod {

  /**
   * The method unique instance.
   */
  private static final ForexSwapDiscountingMethod INSTANCE = new ForexSwapDiscountingMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ForexSwapDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ForexSwapDiscountingMethod() {
  }

  /**
   * Forex method by discounting.
   */
  private static final ForexDiscountingMethod FX_METHOD = ForexDiscountingMethod.getInstance();

  /**
   * Compute the present value by discounting the payments in their own currency.
   * @param fx The Forex swap.
   * @param curves The curve bundle containing the discounting curves.
   * @return The multi-currency present value.
   */
  public MultipleCurrencyAmount presentValue(final ForexSwap fx, final YieldCurveBundle curves) {
    final MultipleCurrencyAmount pv = FX_METHOD.presentValue(fx.getNearLeg(), curves);
    return pv.plus(FX_METHOD.presentValue(fx.getFarLeg(), curves));
  }

  @Override
  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexSwap, "Instrument should be ForexSwap");
    return presentValue((ForexSwap) instrument, curves);
  }

  @Override
  public MultipleCurrencyAmount currencyExposure(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    return presentValue(instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a forex swap transaction.
   * @param fx The forex swap transaction.
   * @param curves The curves.
   * @return The sensitivity.
   */
  public MultipleCurrencyInterestRateCurveSensitivity presentValueCurveSensitivity(final ForexSwap fx, final YieldCurveBundle curves) {
    MultipleCurrencyInterestRateCurveSensitivity result = FX_METHOD.presentValueCurveSensitivity(fx.getNearLeg(), curves);
    result = result.plus(FX_METHOD.presentValueCurveSensitivity(fx.getFarLeg(), curves));
    return result;
  }

}
