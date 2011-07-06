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
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * Calculator of the present value for Forex derivatives in the Black (Garman-Kohlhagen) world. The volatilities are given by delta-smile descriptions.
 * To compute the curve sensitivity, the Black volatility is kept constant; the volatility is not recomputed for curve and forward changes.
 */
public class PresentValueCurveSensitivityBlackForexCalculator extends PresentValueCurveSensitivityForexCalculator {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityBlackForexCalculator s_instance = new PresentValueCurveSensitivityBlackForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityBlackForexCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  PresentValueCurveSensitivityBlackForexCalculator() {
  }

  @Override
  public PresentValueSensitivity visitForexOptionVanilla(ForexOptionVanilla derivative, YieldCurveBundle data) {
    ForexOptionVanillaBlackMethod method = new ForexOptionVanillaBlackMethod();
    return method.presentValueCurveSensitivity(derivative, data);
  }

  @Override
  public PresentValueSensitivity visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative, YieldCurveBundle data) {
    ForexOptionSingleBarrierBlackMethod method = new ForexOptionSingleBarrierBlackMethod();
    return method.presentValueCurveSensitivity(derivative, data);
  }

}
