/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceIssuerCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Interface to generic futures security pricing method for multi-curve and issuer provider.
 */
public class FuturesSecurityIssuerMethod extends FuturesSecurityMethod {

  /** The futures price calculator **/
  private static final FuturesPriceIssuerCalculator FPIC = FuturesPriceIssuerCalculator.getInstance();
  /** The futures price calculator **/
  private static final FuturesPriceCurveSensitivityIssuerCalculator FPCSIC = FuturesPriceCurveSensitivityIssuerCalculator.getInstance();

  /**
   * Computes the quoted price of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price.
   */
  public double price(final FuturesSecurity futures, final ParameterIssuerProviderInterface multicurve) {
    return futures.accept(FPIC, multicurve);
  }

  /**
   * Computes the quoted price curve sensitivity of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final FuturesSecurity futures, final ParameterIssuerProviderInterface multicurve) {
    return futures.accept(FPCSIC, multicurve);
  }

}
