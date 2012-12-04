/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.forexblack;

import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.forex.provider.ForexNonDeliverableOptionBlackSmileMethod;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.ForexBlackSmileProviderInterface;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueForexVolatilitySensitivityForexBlackSmileCalculator extends
    InstrumentDerivativeVisitorSameMethodAdapter<ForexBlackSmileProviderInterface, PresentValueForexBlackVolatilitySensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueForexVolatilitySensitivityForexBlackSmileCalculator INSTANCE = new PresentValueForexVolatilitySensitivityForexBlackSmileCalculator();

  /**
   * Constructor.
   */
  private PresentValueForexVolatilitySensitivityForexBlackSmileCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueForexVolatilitySensitivityForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FX_VAN = ForexOptionVanillaBlackSmileMethod.getInstance();
  private static final ForexNonDeliverableOptionBlackSmileMethod METHOD_NDO = ForexNonDeliverableOptionBlackSmileMethod.getInstance();

  @Override
  public PresentValueForexBlackVolatilitySensitivity visit(final InstrumentDerivative derivative, final ForexBlackSmileProviderInterface blackSmile) {
    return derivative.accept(this, blackSmile);
  }

  // -----     Forex     ------

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexOptionVanilla(final ForexOptionVanilla option, final ForexBlackSmileProviderInterface blackSmile) {
    return METHOD_FX_VAN.presentValueBlackVolatilitySensitivity(option, blackSmile);
  }

  @Override
  public PresentValueForexBlackVolatilitySensitivity visitForexNonDeliverableOption(final ForexNonDeliverableOption option, final ForexBlackSmileProviderInterface blackSmile) {
    return METHOD_NDO.presentValueBlackVolatilitySensitivity(option, blackSmile);
  }

  @Override
  public PresentValueForexBlackVolatilitySensitivity visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
