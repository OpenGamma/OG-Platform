/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackTermStructureMethod;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value for Forex derivatives in the Black (Garman-Kohlhagen) world.
 * The volatilities are given by a term-structure of implied vol.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public final class PresentValueBlackTermStructureForexCalculator extends PresentValueMCACalculator {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackTermStructureForexCalculator s_instance = new PresentValueBlackTermStructureForexCalculator();

  /**
   * Get the unique calculator instance.
   * @return The instance.
   */
  public static PresentValueBlackTermStructureForexCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PresentValueBlackTermStructureForexCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackTermStructureMethod METHOD_FXOPTION = ForexOptionVanillaBlackTermStructureMethod.getInstance();

  @Override
  public MultipleCurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTION.presentValue(derivative, data);
  }

}
