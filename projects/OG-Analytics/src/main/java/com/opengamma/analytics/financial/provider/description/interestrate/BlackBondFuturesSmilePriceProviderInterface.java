/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

/**
 * Provider of Black smile for options on bond futures. The volatility is time to expiration/strike dependent.
 */
public interface BlackBondFuturesSmilePriceProviderInterface extends ParameterIssuerProviderInterface {

  /**
   * Create a new copy of the provider
   * @return The bundle
   */
  @Override
  BlackBondFuturesSmilePriceProviderInterface copy();

  /**
   * Returns the underlying bond futures price.
   * @return The price.
   */
  double getFuturesPrice();

  /**
   * Returns the Black bond futures provider.
   * @return The provider.
   */
  BlackBondFuturesSmileProviderInterface getBlackProvider();

}
