/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionDigitalBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionSingleBarrierBlackMethod;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackMethod;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;

/**
 * Calculator of the volatility sensitivity for Forex derivatives in the Black (Garman-Kohlhagen) world.
 */
public class PresentValueVolatilitySensitivityBlackForexCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, PresentValueForexBlackVolatilitySensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueVolatilitySensitivityBlackForexCalculator s_instance = new PresentValueVolatilitySensitivityBlackForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueVolatilitySensitivityBlackForexCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  PresentValueVolatilitySensitivityBlackForexCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackMethod METHOD_FXOPTION = ForexOptionVanillaBlackMethod.getInstance();
  private static final ForexOptionSingleBarrierBlackMethod METHOD_FXOPTIONBARRIER = ForexOptionSingleBarrierBlackMethod.getInstance();
  //  private static final ForexNonDeliverableOptionBlackMethod METHOD_NDO = ForexNonDeliverableOptionBlackMethod.getInstance();
  private static final ForexOptionDigitalBlackMethod METHOD_FXOPTIONDIGITAL = ForexOptionDigitalBlackMethod.getInstance();

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTION.presentValueVolatilitySensitivity(derivative, data);
  }

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONBARRIER.presentValueVolatilitySensitivity(derivative, data);
  }

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexOptionDigital(final ForexOptionDigital derivative, final YieldCurveBundle data) {
    return METHOD_FXOPTIONDIGITAL.presentValueVolatilitySensitivity(derivative, data);
  }

}
