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
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value for Forex derivatives in the Black (Garman-Kohlhagen) world. The volatilities are given by delta-smile descriptions.
 */
public final class PresentValueBlackForexCalculator extends PresentValueForexCalculator {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackForexCalculator s_instance = new PresentValueBlackForexCalculator();

  /**
   * Get the unique calculator instance.
   * @return The instance.
   */
  public static PresentValueBlackForexCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private PresentValueBlackForexCalculator() {
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionVanilla(ForexOptionVanilla derivative, YieldCurveBundle data) {
    ForexOptionVanillaBlackMethod method = new ForexOptionVanillaBlackMethod();
    return method.presentValue(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative, YieldCurveBundle data) {
    ForexOptionSingleBarrierBlackMethod method = new ForexOptionSingleBarrierBlackMethod();
    return method.presentValue(derivative, data);
  }

}
