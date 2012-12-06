/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.forexblack;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackFlatMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.ForexBlackFlatProviderInterface;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueForexVolatilitySensitivityForexBlackFlatCalculator extends
    InstrumentDerivativeVisitorSameMethodAdapter<ForexBlackFlatProviderInterface, PresentValueForexBlackVolatilitySensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueForexVolatilitySensitivityForexBlackFlatCalculator INSTANCE = new PresentValueForexVolatilitySensitivityForexBlackFlatCalculator();

  /**
   * Constructor.
   */
  private PresentValueForexVolatilitySensitivityForexBlackFlatCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueForexVolatilitySensitivityForexBlackFlatCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final ForexOptionVanillaBlackFlatMethod METHOD_FX_VAN = ForexOptionVanillaBlackFlatMethod.getInstance();

  @Override
  public PresentValueForexBlackVolatilitySensitivity visit(final InstrumentDerivative derivative, final ForexBlackFlatProviderInterface blackSmile) {
    return derivative.accept(this, blackSmile);
  }

  // -----     Forex     ------

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexOptionVanilla(final ForexOptionVanilla option, final ForexBlackFlatProviderInterface blackSmile) {
    return METHOD_FX_VAN.presentValueBlackVolatilitySensitivity(option, blackSmile);
  }

  @Override
  public PresentValueForexBlackVolatilitySensitivity visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
