/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.calculator.PresentValueCurveSensitivityMCSCalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackTermStructureMethod;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;

/**
 * Calculator of the present value for Forex derivatives in the Black (Garman-Kohlhagen) world. A term structure of implied volatility is provided.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public class PresentValueCurveSensitivityBlackTermStructureForexCalculator extends PresentValueCurveSensitivityMCSCalculator {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityBlackTermStructureForexCalculator s_instance = new PresentValueCurveSensitivityBlackTermStructureForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityBlackTermStructureForexCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  PresentValueCurveSensitivityBlackTermStructureForexCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackTermStructureMethod METHOD_FXOPTION = ForexOptionVanillaBlackTermStructureMethod.getInstance();

  @Override
  public MultipleCurrencyInterestRateCurveSensitivity visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTION.presentValueCurveSensitivity(derivative, data);
  }

}
