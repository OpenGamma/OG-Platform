/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackFlatMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexFlatProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueCurveSensitivityForexBlackFlatCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<BlackForexFlatProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityForexBlackFlatCalculator INSTANCE = new PresentValueCurveSensitivityForexBlackFlatCalculator();

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityForexBlackFlatCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityForexBlackFlatCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final ForexOptionVanillaBlackFlatMethod METHOD_FX_VAN = ForexOptionVanillaBlackFlatMethod.getInstance();

  @Override
  public MultipleCurrencyMulticurveSensitivity visit(final InstrumentDerivative derivative, final BlackForexFlatProviderInterface blackSmile) {
    return derivative.accept(this, blackSmile);
  }

  // -----     Forex     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForexOptionVanilla(final ForexOptionVanilla option, final BlackForexFlatProviderInterface blackSmile) {
    return METHOD_FX_VAN.presentValueCurveSensitivity(option, blackSmile);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
