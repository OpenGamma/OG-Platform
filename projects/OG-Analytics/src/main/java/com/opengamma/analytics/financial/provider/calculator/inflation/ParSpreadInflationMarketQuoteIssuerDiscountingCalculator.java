/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;

/**
 * 
 */
public final class ParSpreadInflationMarketQuoteIssuerDiscountingCalculator extends InstrumentDerivativeVisitorDelegate<InflationIssuerProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadInflationMarketQuoteIssuerDiscountingCalculator INSTANCE = new ParSpreadInflationMarketQuoteIssuerDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadInflationMarketQuoteIssuerDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadInflationMarketQuoteIssuerDiscountingCalculator() {
    super(new InflationIssuerProviderAdapter<>(ParSpreadInflationMarketQuoteDiscountingCalculator.getInstance()));
  }

}
