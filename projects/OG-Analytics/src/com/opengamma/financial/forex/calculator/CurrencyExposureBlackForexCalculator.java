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
 * Calculator of the currency exposure for Forex derivatives in the Black (Garman-Kohlhagen) world. The volatilities are given by delta-smile descriptions.
 * To compute the currency exposure, the Black volatility is kept constant; the volatility is not recomputed for spot and forward changes.
 */
public class CurrencyExposureBlackForexCalculator extends CurrencyExposureForexCalculator {

  /**
   * The unique instance of the calculator.
   */
  private static final CurrencyExposureBlackForexCalculator s_instance = new CurrencyExposureBlackForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static CurrencyExposureBlackForexCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  CurrencyExposureBlackForexCalculator() {
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionVanilla(ForexOptionVanilla derivative, YieldCurveBundle data) {
    ForexOptionVanillaBlackMethod method = new ForexOptionVanillaBlackMethod();
    return method.currencyExposure(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionSingleBarrier(ForexOptionSingleBarrier derivative, YieldCurveBundle data) {
    ForexOptionSingleBarrierBlackMethod method = new ForexOptionSingleBarrierBlackMethod();
    return method.currencyExposure(derivative, data);
  }

}
