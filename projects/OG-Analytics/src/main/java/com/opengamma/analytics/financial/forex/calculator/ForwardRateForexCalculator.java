/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexNonDeliverableForwardDiscountingMethod;
import com.opengamma.analytics.financial.forex.method.ForexNonDeliverableOptionBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;

/**
 * Calculator of the forward Forex rate for Forex derivatives.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public class ForwardRateForexCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ForwardRateForexCalculator INSTANCE = new ForwardRateForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ForwardRateForexCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  ForwardRateForexCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();
  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_NDF = ForexNonDeliverableForwardDiscountingMethod.getInstance();
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTION = ForexOptionVanillaBlackSmileMethod.getInstance();
  private static final ForexNonDeliverableOptionBlackMethod METHOD_NDO = ForexNonDeliverableOptionBlackMethod.getInstance();

  @Override
  public Double visitForex(final Forex derivative, final YieldCurveBundle data) {
    return METHOD_FOREX.forwardForexRate(derivative, data);
  }

  @Override
  public Double visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final YieldCurveBundle data) {
    return METHOD_NDF.forwardForexRate(derivative, data);
  }

  @Override
  public Double visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTION.forwardForexRate(derivative, data);
  }

  @Override
  public Double visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final YieldCurveBundle data) {
    return METHOD_NDO.forwardForexRate(derivative, data);
  }

}
