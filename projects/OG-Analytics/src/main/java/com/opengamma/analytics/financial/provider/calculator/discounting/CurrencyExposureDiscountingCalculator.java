/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class CurrencyExposureDiscountingCalculator extends InstrumentDerivativeVisitorDelegate<MulticurveProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final CurrencyExposureDiscountingCalculator INSTANCE = new CurrencyExposureDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static CurrencyExposureDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private CurrencyExposureDiscountingCalculator() {
    super(PresentValueDiscountingCalculator.getInstance());
  }

  // TODO: Add FX specific products when required

}
