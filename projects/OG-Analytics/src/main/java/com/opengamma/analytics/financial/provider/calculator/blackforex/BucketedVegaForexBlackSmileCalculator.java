/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityNodeSensitivityDataBundle;
import com.opengamma.analytics.financial.forex.provider.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;

/**
 * Calculates the bucketed vega matrix (first order derivative with respect to the implied volatility) for Forex derivatives in the
 * Black (Garman-Kohlhagen) world. The matrix axes are delta and time to expiry.
 */
public class BucketedVegaForexBlackSmileCalculator extends InstrumentDerivativeVisitorAdapter<BlackForexSmileProviderInterface, PresentValueForexBlackVolatilityNodeSensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final BucketedVegaForexBlackSmileCalculator INSTANCE = new BucketedVegaForexBlackSmileCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static BucketedVegaForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  BucketedVegaForexBlackSmileCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final ForexOptionVanillaBlackSmileMethod METHOD_FXOPTIONVANILLA = ForexOptionVanillaBlackSmileMethod.getInstance();

  @Override
  public PresentValueForexBlackVolatilityNodeSensitivityDataBundle visitForexOptionVanilla(final ForexOptionVanilla optionForex, final BlackForexSmileProviderInterface smileMulticurves) {
    return METHOD_FXOPTIONVANILLA.presentValueBlackVolatilityNodeSensitivity(optionForex, smileMulticurves);
  }
}
