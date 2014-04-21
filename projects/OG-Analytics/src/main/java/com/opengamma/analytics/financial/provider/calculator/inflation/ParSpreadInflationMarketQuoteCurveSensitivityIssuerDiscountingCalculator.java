/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;

/**
 * Compute the sensitivity of the spread to the curve; the spread is the number to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "spread" will depend of each instrument.
 * This method is dedicated to inflation instrument using issuer.
 */
public final class ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator extends InstrumentDerivativeVisitorDelegate<InflationIssuerProviderInterface, InflationSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator INSTANCE = new ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadInflationMarketQuoteCurveSensitivityIssuerDiscountingCalculator() {
    super(new InflationIssuerProviderAdapter<>(ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator.getInstance()));
  }

}
