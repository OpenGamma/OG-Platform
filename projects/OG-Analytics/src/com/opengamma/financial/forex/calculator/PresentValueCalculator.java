/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.method.ForexDiscountingMethod;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class PresentValueCalculator extends AbstractForexDerivativeVisitor<YieldCurveBundle, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCalculator s_instance = new PresentValueCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  PresentValueCalculator() {
  }

  @Override
  public MultipleCurrencyAmount visitForex(Forex derivative, YieldCurveBundle data) {
    ForexDiscountingMethod method = new ForexDiscountingMethod();
    return method.presentValue(derivative, data);
  }

}
