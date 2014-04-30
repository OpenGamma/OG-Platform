/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceBlackBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceBlackSensitivityBlackBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceCurveSensitivityBlackBondFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackBondFuturesCubeSensitivity;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Interface to generic futures security pricing method for multi-curve, issuer and Black on bond futures parameter provider.
 */
public class FuturesSecurityBlackBondFuturesMethod extends FuturesSecurityMethod {

  /** The futures price calculator **/
  private static final FuturesPriceBlackBondFuturesCalculator FPC = FuturesPriceBlackBondFuturesCalculator.getInstance();
  /** The futures price curve sensitivity calculator **/
  private static final FuturesPriceCurveSensitivityBlackBondFuturesCalculator FPCSC = FuturesPriceCurveSensitivityBlackBondFuturesCalculator.getInstance();
  /** The futures price Black sensitivity sensitivity calculator **/
  private static final FuturesPriceBlackSensitivityBlackBondFuturesCalculator FPBSC = FuturesPriceBlackSensitivityBlackBondFuturesCalculator.getInstance();

  /**
   * Computes the quoted price of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price.
   */
  public double price(final FuturesSecurity futures, final BlackBondFuturesProviderInterface multicurve) {
    return futures.accept(FPC, multicurve);
  }

  /**
   * Computes the quoted price curve sensitivity of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final FuturesSecurity futures, final BlackBondFuturesProviderInterface multicurve) {
    return futures.accept(FPCSC, multicurve);
  }

  /**
   * Computes the price sensitivity to the Black implied volatility (point sensitivity) from the curve and volatility provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price Black sensitivity.
   */
  public PresentValueBlackBondFuturesCubeSensitivity priceBlackSensitivity(final FuturesSecurity futures, final BlackBondFuturesProviderInterface multicurve) {
    return futures.accept(FPBSC, multicurve);
  }

}
