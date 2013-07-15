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
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueForexForwardPointsCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveForwardPointsProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueForexForwardPointsCalculator INSTANCE = new PresentValueForexForwardPointsCalculator();

  /**
   * Constructor.
   */
  private PresentValueForexForwardPointsCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueForexForwardPointsCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final ForexForwardPointsMethod METHOD_FX_FWD = ForexForwardPointsMethod.getInstance();

  // -----     Forex     ------

  @Override
  public MultipleCurrencyAmount visitForex(final Forex fx, final MulticurveForwardPointsProviderInterface multicurvePoints) {
    return METHOD_FX_FWD.presentValue(fx, multicurvePoints);
  }

}
