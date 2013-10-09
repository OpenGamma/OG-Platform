/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

/**
 * Provider of Black smile for options on bond futures. The volatility is time to expiration/strike dependent.
 */
public interface BlackBondFuturesSmileProviderInterface extends ParameterIssuerProviderInterface {

  /**
   * Create a new copy of the provider
   * @return The bundle
   */
  @Override
  BlackBondFuturesSmileProviderInterface copy();

  /**
   * Gets the Black volatility at a given expiry-strike-delay point.
   * @param expiry The time to expiration.
   * @param strike The strike.
   * @return The volatility.
   */
  double getVolatility(final double expiry, final double strike);

  //TODO: Add a reference to the underlying.
  //  /**
  //   * Returns the Ibor Index of the futures on for which the Black data is valid, i.e. the data is calibrated to futures on the given index.
  //   * @return The generator.
  //   */
  //  IborIndex getFuturesIndex();

}
