/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionSingleBarrierBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Calculator of the vomma (second order derivative with respect to implied volatility) for Forex derivatives in the Black (Garman-Kohlhagen) world.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public class VommaValueBlackForexCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, CurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final VommaValueBlackForexCalculator INSTANCE = new VommaValueBlackForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static VommaValueBlackForexCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  VommaValueBlackForexCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTIONVANILLA = ForexOptionVanillaBlackSmileMethod.getInstance();
  private static final ForexOptionSingleBarrierBlackMethod METHOD_FXOPTIONBARRIER = ForexOptionSingleBarrierBlackMethod.getInstance();

  @Override
  public CurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONVANILLA.vomma(derivative, data);
  }

  @Override
  public CurrencyAmount visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONBARRIER.vommaFd(derivative, data);
  }
}
