/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.analytics.financial.provider.calculator.issuer.FuturesPriceIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;

/**
 * Interface to generic futures security pricing method for multi-curve provider.
 */
public final class FuturesSecurityIssuerMethod extends FuturesSecurityMethod {

  /**
   * Creates the method unique instance.
   */
  private static final FuturesSecurityIssuerMethod INSTANCE = new FuturesSecurityIssuerMethod();

  /**
   * Constructor.
   */
  private FuturesSecurityIssuerMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static FuturesSecurityIssuerMethod getInstance() {
    return INSTANCE;
  }

  /** The futures price calculator **/
  private static final FuturesPriceIssuerCalculator FPIC = FuturesPriceIssuerCalculator.getInstance();

  /**
   * Computes the quoted price of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price.
   */
  double price(final FuturesSecurity futures, final ParameterIssuerProviderInterface multicurve) {
    return futures.accept(FPIC, multicurve.getIssuerProvider());
  }

}
