/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;

/**
 * Provider of normal volatility (Bachelier model) smile for options on STIR futures. The volatility is time to expiration/strike/delay dependent. 
 * The "delay" is the time between expiration of the option and last trading date of the underlying futures.
 */
public interface NormalSTIRFuturesSmileProviderInterface extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider
   * @return The bundle
   */
  @Override
  NormalSTIRFuturesSmileProviderInterface copy();

  /**
   * Gets the normal volatility at a given expiry-strike-delay point.
   * @param expiry The time to expiration.
   * @param strike The strike.
   * @param delay The delay between expiration of the option and last trading date of the underlying futures.
   * @return The volatility.
   */
  double getVolatility(final double expiry, final double strike, final double delay);

  /**
   * Returns the Ibor Index of the futures on for which the Black data is valid, i.e. the data is calibrated to futures on the given index.
   * @return The generator.
   */
  IborIndex getFuturesIndex();

}
