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
 * Calculates the value delta (first order derivative with respect to the spot rate) for Forex derivatives in the Black (Garman-Kohlhagen) world.
 */
public class ValueDeltaForexBlackSmileCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ValueDeltaForexBlackSmileCalculator INSTANCE = new ValueDeltaForexBlackSmileCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ValueDeltaForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  ValueDeltaForexBlackSmileCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTIONVANILLA = ForexOptionVanillaBlackSmileMethod.getInstance();

  /**
   * The value delta is provided with "direct quote", i.e. (1 foreign = x domestic) and not the reverse quote (1 domestic = x foreign).
   * @param optionForex The Forex option.
   * @param smileMulticurves The curve and smile data.
   * @return The value gamma.
   */
  @Override
  public Double visitForexOptionVanilla(final ForexOptionVanilla optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    return METHOD_FXOPTIONVANILLA.deltaRelative(optionForex, smileMulticurves, true);
  }
}
