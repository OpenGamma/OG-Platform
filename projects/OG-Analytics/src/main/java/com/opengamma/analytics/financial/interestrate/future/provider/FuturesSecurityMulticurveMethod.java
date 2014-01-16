/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceCurveSensitivityMulticurveCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceMulticurveCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Interface to generic futures security pricing method for multi-curve provider.
 */
public class FuturesSecurityMulticurveMethod extends FuturesSecurityMethod {

  /** The futures price calculator **/
  private static final FuturesPriceMulticurveCalculator FPMC = FuturesPriceMulticurveCalculator.getInstance();
  /** The futures price calculator **/
  private static final FuturesPriceCurveSensitivityMulticurveCalculator FPCSMC = FuturesPriceCurveSensitivityMulticurveCalculator.getInstance();

  /**
   * Computes the quoted price of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price.
   */
  public double price(final FuturesSecurity futures, final ParameterProviderInterface multicurve) {
    return futures.accept(FPMC, multicurve);
  }

  /**
   * Computes the quoted price curve sensitivity of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final FuturesSecurity futures, final ParameterProviderInterface multicurve) {
    return futures.accept(FPCSMC, multicurve);
  }

}
