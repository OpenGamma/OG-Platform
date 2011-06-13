/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.derivative.ForexSwap;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Pricing method for Forex swap transactions by discounting each payment.
 */
public class ForexSwapDiscountingMethod implements ForexPricingMethod {

  /**
   * Forex method by discounting.
   */
  private static final ForexDiscountingMethod FX_METHOD = new ForexDiscountingMethod();

  /**
   * Compute the present value by discounting the payments in their own currency.
   * @param fx The Forex swap.
   * @param curves The curve bundle containing the discounting curves.
   * @return The multi-currency present value.
   */
  public MultipleCurrencyAmount presentValue(final ForexSwap fx, final YieldCurveBundle curves) {
    MultipleCurrencyAmount pv = FX_METHOD.presentValue(fx.getNearLeg(), curves);
    return pv.plus(FX_METHOD.presentValue(fx.getFarLeg(), curves));
  }

  @Override
  public MultipleCurrencyAmount presentValue(ForexDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof ForexSwap, "Instrument should be ForexSwap");
    return presentValue((ForexSwap) instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a forex swap transaction.
   * @param fx The forex swap transaction.
   * @param curves The curves.
   * @return The sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final ForexSwap fx, final YieldCurveBundle curves) {
    PresentValueSensitivity result = FX_METHOD.presentValueCurveSensitivity(fx.getNearLeg(), curves);
    result = result.add(FX_METHOD.presentValueCurveSensitivity(fx.getFarLeg(), curves));
    return result;
  }

}
