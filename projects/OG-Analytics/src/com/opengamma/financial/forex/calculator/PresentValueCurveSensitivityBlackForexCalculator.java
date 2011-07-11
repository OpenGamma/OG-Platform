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
  public PresentValueSensitivity visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    final ForexOptionVanillaBlackMethod method = ForexOptionVanillaBlackMethod.getInstance();
    return method.presentValueCurveSensitivity(derivative, data);
  }

  @Override
  public PresentValueSensitivity visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final YieldCurveBundle data) {
    final ForexOptionSingleBarrierBlackMethod method = ForexOptionSingleBarrierBlackMethod.getInstance();
    return method.presentValueCurveSensitivity(derivative, data);
  }

}
