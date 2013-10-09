/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;

/**
 * Calculates the spot delta (first order derivative with respect to the spot rate) for Forex derivatives in the Black (Garman-Kohlhagen) world.
 */
public class SpotDeltaForexBlackSmileCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final SpotDeltaForexBlackSmileCalculator INSTANCE = new SpotDeltaForexBlackSmileCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static SpotDeltaForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  SpotDeltaForexBlackSmileCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTIONVANILLA = ForexOptionVanillaBlackSmileMethod.getInstance();

  @Override
  public Double visitForexOptionVanilla(final ForexOptionVanilla optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    return METHOD_FXOPTIONVANILLA.spotDeltaTheoretical(optionForex, smileMulticurves);
  }
}
