/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.method.ForexDiscountingMethod;
import com.opengamma.financial.forex.method.ForexNonDeliverableForwardDiscountingMethod;
import com.opengamma.financial.forex.method.ForexNonDeliverableOptionBlackMethod;
import com.opengamma.financial.forex.method.ForexOptionVanillaBlackMethod;
import com.opengamma.financial.forex.method.YieldCurveWithFXBundle;
import com.opengamma.financial.interestrate.AbstractInstrumentDerivativeVisitor;

/**
 * Calculator of the forward Forex rate for Forex derivatives.
 */
public class ForwardRateForexCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveWithFXBundle, Double> {

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
  private static final ForexOptionVanillaBlackMethod METHOD_FXOPTION = ForexOptionVanillaBlackMethod.getInstance();
  private static final ForexNonDeliverableOptionBlackMethod METHOD_NDO = ForexNonDeliverableOptionBlackMethod.getInstance();

  @Override
  public Double visitForex(final Forex derivative, final YieldCurveWithFXBundle data) {
    return METHOD_FOREX.forwardForexRate(derivative, data);
  }

  @Override
  public Double visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final YieldCurveWithFXBundle data) {
    return METHOD_NDF.forwardForexRate(derivative, data);
  }

  @Override
  public Double visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveWithFXBundle data) {
    return METHOD_FXOPTION.forwardForexRate(derivative, data);
  }

  @Override
  public Double visitForexNonDeliverableOption(final ForexNonDeliverableOption derivative, final YieldCurveWithFXBundle data) {
    return METHOD_NDO.forwardForexRate(derivative, data);
  }

}
