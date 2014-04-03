/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceBlackFlatBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceCurveSensitivityBlackFlatBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesFlatProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Interface to generic futures security pricing method for multi-curve, issuer and Black on bond futures parameter provider.
 */
public class FuturesSecurityBlackFlatBondFuturesMethod extends FuturesSecurityMethod {

  /** The futures price calculator **/
  private static final FuturesPriceBlackFlatBondFuturesCalculator FPC = FuturesPriceBlackFlatBondFuturesCalculator.getInstance();
  /** The futures price curve sensitivity calculator **/
  private static final FuturesPriceCurveSensitivityBlackFlatBondFuturesCalculator FPCSC = FuturesPriceCurveSensitivityBlackFlatBondFuturesCalculator.getInstance();

  /**
   * Computes the quoted price of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price.
   */
  public double price(final FuturesSecurity futures, final BlackBondFuturesFlatProviderInterface multicurve) {
    return futures.accept(FPC, multicurve);
  }

  /**
   * Computes the quoted price curve sensitivity of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final FuturesSecurity futures, final BlackBondFuturesFlatProviderInterface multicurve) {
    return futures.accept(FPCSC, multicurve);
  }
}
