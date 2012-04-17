/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBlackMethod;

/**
 * Present value calculator for interest rate instruments using Black model with implied volatilities.
 */
public final class ImpliedVolatilityBlackCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {

  /**
   * The method unique instance.
   */
  private static final ImpliedVolatilityBlackCalculator INSTANCE = new ImpliedVolatilityBlackCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ImpliedVolatilityBlackCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  ImpliedVolatilityBlackCalculator() {
  }

  /**
   * The methods used in the calculator.
   */
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWAPTION_PHYSICAL = SwaptionPhysicalFixedIborBlackMethod.getInstance();

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    return METHOD_SWAPTION_PHYSICAL.impliedVolatility(swaption, curves);
  }

}
