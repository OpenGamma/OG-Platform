/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.forexpoints;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.provider.ForexForwardPointsMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveForwardPointsProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueCurveSensitivityForexForwardPointsCalculator
    extends InstrumentDerivativeVisitorAdapter<MulticurveForwardPointsProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityForexForwardPointsCalculator INSTANCE = new PresentValueCurveSensitivityForexForwardPointsCalculator();

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityForexForwardPointsCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityForexForwardPointsCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final ForexForwardPointsMethod METHOD_FX_FWD = ForexForwardPointsMethod.getInstance();

  // -----     Forex     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitForex(final Forex fx, final MulticurveForwardPointsProviderInterface multicurvePoints) {
    return METHOD_FX_FWD.presentValueCurveSensitivity(fx, multicurvePoints);
  }

}
