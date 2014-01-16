/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceHullWhiteCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Interface to generic futures security pricing method for multi-curve and Hull-White one factor paramter provider.
 */
public class FuturesSecurityHullWhiteMethod extends FuturesSecurityMethod {

  /** The futures price calculator **/
  private static final FuturesPriceHullWhiteCalculator FPHWC = FuturesPriceHullWhiteCalculator.getInstance();
  /** The futures price curve sensitivity calculator **/
  private static final FuturesPriceCurveSensitivityHullWhiteCalculator FPCSHWC = FuturesPriceCurveSensitivityHullWhiteCalculator.getInstance();

  //  /** The futures price and price curve sensitivity (simultaneous) calculator **/
  //  private static final FuturesPriceADHullWhiteCalculator FPADHWIC = FuturesPriceADHullWhiteCalculator.getInstance();

  /**
   * Computes the quoted price of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price.
   */
  public double price(final FuturesSecurity futures, final HullWhiteOneFactorProviderInterface multicurve) {
    return futures.accept(FPHWC, multicurve);
  }

  /**
   * Computes the quoted price curve sensitivity of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final FuturesSecurity futures, final HullWhiteOneFactorProviderInterface multicurve) {
    return futures.accept(FPCSHWC, multicurve);
  }

  //  /**
  //   * Computes the future price and the price curve sensitivity simultaneously (Algorithmic differentiation).
  //   * @param futures The future security.
  //   * @param multicurve The multicurve provider.
  //   * @return The price and price curve sensitivity as a pair.
  //   */
  //  public Pair<Double, MulticurveSensitivity> priceAD(final BondFuturesSecurity futures, final HullWhiteOneFactorProviderInterface multicurve) {
  //    return futures.accept(FPADHWC, multicurve);
  //  }

}
