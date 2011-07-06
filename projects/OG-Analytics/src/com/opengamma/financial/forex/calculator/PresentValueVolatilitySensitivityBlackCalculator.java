/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.method.ForexOptionSingleBarrierBlackMethod;
import com.opengamma.financial.forex.method.ForexOptionVanillaBlackMethod;
import com.opengamma.financial.forex.method.PresentValueVolatilitySensitivityDataBundle;
import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * Calculator of the volatility sensitivity for Forex derivatives in the Black (Garman-Kohlhagen) world.
 */
public class PresentValueVolatilitySensitivityBlackCalculator extends AbstractForexDerivativeVisitor<YieldCurveBundle, PresentValueVolatilitySensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueVolatilitySensitivityBlackCalculator s_instance = new PresentValueVolatilitySensitivityBlackCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueVolatilitySensitivityBlackCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  PresentValueVolatilitySensitivityBlackCalculator() {
  }

  @Override
  public PresentValueVolatilitySensitivityDataBundle visitForexOptionVanilla(ForexOptionVanilla derivative, YieldCurveBundle data) {
    ForexOptionVanillaBlackMethod method = new ForexOptionVanillaBlackMethod();
    return method.presentValueVolatilitySensitivity(derivative, data);
  }

  @Override
  public PresentValueVolatilitySensitivityDataBundle visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative, YieldCurveBundle data) {
    ForexOptionSingleBarrierBlackMethod method = new ForexOptionSingleBarrierBlackMethod();
    return method.presentValueVolatilitySensitivity(derivative, data);
  }

}
