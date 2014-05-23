/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceADHullWhiteIssuerCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceCurveSensitivityHullWhiteIssuerCalculator;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceHullWhiteIssuerCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.tuple.Pair;

/**
 * Interface to generic futures security pricing method for multi-curve, issuer and Hull-White one factor parameter provider.
 */
public class FuturesSecurityHullWhiteIssuerMethod extends FuturesSecurityMethod {

  /** The futures price calculator **/
  private static final FuturesPriceHullWhiteIssuerCalculator FPHWIC = FuturesPriceHullWhiteIssuerCalculator.getInstance();
  /** The futures price curve sensitivity calculator **/
  private static final FuturesPriceCurveSensitivityHullWhiteIssuerCalculator FPCSHWIC = FuturesPriceCurveSensitivityHullWhiteIssuerCalculator.getInstance();
  /** The futures price and price curve sensitivity (simultaneous) calculator **/
  private static final FuturesPriceADHullWhiteIssuerCalculator FPADHWIC = FuturesPriceADHullWhiteIssuerCalculator.getInstance();

  /**
   * Computes the quoted price of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price.
   */
  public double price(final FuturesSecurity futures, final HullWhiteIssuerProviderInterface multicurve) {
    return futures.accept(FPHWIC, multicurve);
  }

  /**
   * Computes the quoted price curve sensitivity of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final FuturesSecurity futures, final HullWhiteIssuerProviderInterface multicurve) {
    return futures.accept(FPCSHWIC, multicurve);
  }

  /**
   * Computes the future price and the price curve sensitivity simultaneously (Algorithmic differentiation).
   * @param futures The future security.
   * @param multicurve The multicurve provider.
   * @return The price and price curve sensitivity as a pair.
   */
  public Pair<Double, MulticurveSensitivity> priceAD(final BondFuturesSecurity futures, final HullWhiteIssuerProviderInterface multicurve) {
    return futures.accept(FPADHWIC, multicurve);
  }
}
