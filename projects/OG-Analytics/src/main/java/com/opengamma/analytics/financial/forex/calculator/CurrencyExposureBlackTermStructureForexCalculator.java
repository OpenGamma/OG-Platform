/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackTermStructureMethod;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the currency exposure for Forex derivatives in the Black (Garman-Kohlhagen) world. A term structure of implied volatility is provided.
 * To compute the currency exposure, the Black volatility is kept constant; the volatility is not recomputed for spot and forward changes.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public final class CurrencyExposureBlackTermStructureForexCalculator extends CurrencyExposureForexCalculator {

  /**
   * The unique instance of the calculator.
   */
  private static final CurrencyExposureBlackTermStructureForexCalculator s_instance = new CurrencyExposureBlackTermStructureForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static CurrencyExposureBlackTermStructureForexCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  private CurrencyExposureBlackTermStructureForexCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackTermStructureMethod METHOD_FXOPTION = ForexOptionVanillaBlackTermStructureMethod.getInstance();

  @Override
  public MultipleCurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTION.currencyExposure(derivative, data);
  }

}
