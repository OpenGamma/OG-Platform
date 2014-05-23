/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceBlackSTIRFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceBlackSensitivityBlackSTIRFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceCurveSensitivityBlackSTIRFuturesCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackSTIRFuturesCubeSensitivity;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Interface to generic futures security pricing method for multi-curve, issuer and Black on bond futures parameter provider.
 */
public class FuturesSecurityBlackSTIRFuturesMethod extends FuturesSecurityMethod {

  /** The futures price calculator **/
  private static final FuturesPriceBlackSTIRFuturesCalculator FPC = FuturesPriceBlackSTIRFuturesCalculator.getInstance();
  /** The futures price curve sensitivity calculator **/
  private static final FuturesPriceCurveSensitivityBlackSTIRFuturesCalculator FPCSC = FuturesPriceCurveSensitivityBlackSTIRFuturesCalculator.getInstance();
  /** The futures price Black sensitivity sensitivity calculator **/
  private static final FuturesPriceBlackSensitivityBlackSTIRFuturesCalculator FPBSC = FuturesPriceBlackSensitivityBlackSTIRFuturesCalculator.getInstance();

  /**
   * Computes the quoted price of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price.
   */
  public double price(final FuturesSecurity futures, final BlackSTIRFuturesProviderInterface multicurve) {
    return futures.accept(FPC, multicurve);
  }

  /**
   * Computes the quoted price curve sensitivity of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final FuturesSecurity futures, final BlackSTIRFuturesProviderInterface multicurve) {
    return futures.accept(FPCSC, multicurve);
  }

  /**
   * Computes the price sensitivity to the Black implied volatility (point sensitivity) from the curve and volatility provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price Black sensitivity.
   */
  public PresentValueBlackSTIRFuturesCubeSensitivity priceBlackSensitivity(final FuturesSecurity futures, final BlackSTIRFuturesProviderInterface multicurve) {
    return futures.accept(FPBSC, multicurve);
  }

}
