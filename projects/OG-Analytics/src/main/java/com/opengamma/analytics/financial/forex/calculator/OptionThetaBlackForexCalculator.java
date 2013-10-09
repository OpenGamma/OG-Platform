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
 * Calculator of the theta (first order derivative with respect to time) using the forward theta
 * for Forex derivatives in the Black (Garman-Kohlhagen) world. The theta is not scaled and so is an annual
 * value.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public class OptionThetaBlackForexCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, CurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final OptionThetaBlackForexCalculator INSTANCE = new OptionThetaBlackForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static OptionThetaBlackForexCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  OptionThetaBlackForexCalculator() {
  }

  /** Vanilla option calculator */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTIONVANILLA = ForexOptionVanillaBlackSmileMethod.getInstance();
  /** Digital option calculator */
  private static final ForexOptionDigitalBlackMethod METHOD_FXDIGITAL = ForexOptionDigitalBlackMethod.getInstance();
  /** Single barrier option calculator */
  private static final ForexOptionSingleBarrierBlackMethod METHOD_FXBARRIER = ForexOptionSingleBarrierBlackMethod.getInstance();

  @Override
  public CurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONVANILLA.forwardTheta(derivative, data);
  }

  @Override
  public CurrencyAmount visitForexOptionDigital(final ForexOptionDigital derivative, final YieldCurveBundle data) {
    return METHOD_FXDIGITAL.forwardTheta(derivative, data);
  }

  @Override
  public CurrencyAmount visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final YieldCurveBundle data) {
    return METHOD_FXBARRIER.forwardTheta(derivative, data);
  }
}
