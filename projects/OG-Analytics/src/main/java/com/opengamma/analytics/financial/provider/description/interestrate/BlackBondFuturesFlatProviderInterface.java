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
public interface BlackBondFuturesFlatProviderInterface extends ParameterIssuerProviderInterface {

  /**
   * Create a new copy of the provider
   * @return The bundle
   */
  @Override
  BlackBondFuturesFlatProviderInterface copy();

  /**
   * Gets the Black volatility at a given expiry-delay point.
   * @param expiry The time to expiration.
   * @param delay The delay between the option expiry and the futures expiry.
   * @return The volatility.
   */
  double getVolatility(final double expiry, final double delay);

  /**
   * Returns the legal entity of the bonds underlying the futures for which the volatility data is valid.
   * @return The legal entity.
   */
  LegalEntity getLegalEntity();

}
