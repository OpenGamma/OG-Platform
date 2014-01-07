/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionDigitalBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionSingleBarrierBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Calculator of the delta (second order derivative with respect to the spot rate) for Forex derivatives in the Black (Garman-Kohlhagen) world.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public class DeltaValueBlackForexCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, CurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final DeltaValueBlackForexCalculator INSTANCE = new DeltaValueBlackForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static DeltaValueBlackForexCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  DeltaValueBlackForexCalculator() {
  }

  /** Vanilla option calculator */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTIONVANILLA = ForexOptionVanillaBlackSmileMethod.getInstance();
  /** Digital option calculator */
  private static final ForexOptionDigitalBlackMethod METHOD_FXDIGITAL = ForexOptionDigitalBlackMethod.getInstance();
  /** Single barrier option calculator */
  private static final ForexOptionSingleBarrierBlackMethod METHOD_FXBARRIER = ForexOptionSingleBarrierBlackMethod.getInstance();

  @Override
  public CurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONVANILLA.delta(derivative, data, true);
  }

  @Override
  public CurrencyAmount visitForexOptionDigital(final ForexOptionDigital derivative, final YieldCurveBundle data) {
    return METHOD_FXDIGITAL.delta(derivative, data, true);
  }

  @Override
  public CurrencyAmount visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final YieldCurveBundle data) {
    return METHOD_FXBARRIER.delta(derivative, data, true);
  }
}
