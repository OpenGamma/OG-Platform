/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.legalentity.LegalEntity;

/**
 * Provider of Black smile for options on bond futures. The volatility is time to expiration/strike dependent.
 */
public interface BlackBondFuturesProviderInterface extends ParameterIssuerProviderInterface {

  /**
   * Create a new copy of the provider
   * @return The bundle
   */
  @Override
  BlackBondFuturesProviderInterface copy();

  /**
   * Gets the Black volatility at a given expiry-delay-strike point.
   * @param expiry The time to expiration.
   * @param delay The delay between the option expiry and the futures expiry.
   * @param strike The option strike.
   * @param futuresPrice The price of the underlying futures. Used for relative moneyness smile description.
   * @return The volatility.
   */
  double getVolatility(final double expiry, final double delay, final double strike, final double futuresPrice);

  /**
   * Returns the legal entity of the bonds underlying the futures for which the volatility data is valid.
   * @return The legal entity.
   */
  LegalEntity getLegalEntity();

}
