/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.derivative.ForexSwap;
import com.opengamma.financial.forex.method.ForexDiscountingMethod;
import com.opengamma.financial.forex.method.ForexOptionVanillaBlackMethod;
import com.opengamma.financial.forex.method.ForexSwapDiscountingMethod;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * Calculator of the present value curve sensitivity for Forex derivatives.
 */
public class PresentValueCurveSensitivityForexCalculator extends AbstractForexDerivativeVisitor<YieldCurveBundle, PresentValueSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityForexCalculator s_instance = new PresentValueCurveSensitivityForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityForexCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  PresentValueCurveSensitivityForexCalculator() {
  }

  @Override
  public PresentValueSensitivity visitForex(final Forex derivative, final YieldCurveBundle data) {
    final ForexDiscountingMethod method = ForexDiscountingMethod.getInstance();
    return method.presentValueCurveSensitivity(derivative, data);
  }

  @Override
  public PresentValueSensitivity visitForexSwap(final ForexSwap derivative, final YieldCurveBundle data) {
    final ForexSwapDiscountingMethod method = ForexSwapDiscountingMethod.getInstance();
    return method.presentValueCurveSensitivity(derivative, data);
  }

  @Override
  public PresentValueSensitivity visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    final ForexOptionVanillaBlackMethod method = ForexOptionVanillaBlackMethod.getInstance();
    return method.presentValueCurveSensitivity(derivative, data);
  }

}
