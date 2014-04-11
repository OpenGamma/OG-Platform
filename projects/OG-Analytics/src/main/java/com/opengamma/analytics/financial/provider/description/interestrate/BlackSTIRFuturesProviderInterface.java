/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;

/**
 * Provider of Black smile for options on STIR futures. The volatility is time to expiration/delay/strike price/underlying futures price dependent. 
 * The "delay" is the time between expiration of the option and last trading date of the underlying futures.
 * The strike price refers to the futures price, not its rate, i.e. the strike price is around 0.95 to 0.99, not around 0.05 to 0.01.
 */
public interface BlackSTIRFuturesProviderInterface extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider
   * @return The bundle
   */
  @Override
  BlackSTIRFuturesProviderInterface copy();

  /**
   * Gets the Black volatility at a given expiry-strike-delay point.
   * @param expiry The time to expiration.
   * @param delay The delay between expiration of the option and last trading date of the underlying futures.
   * @param strikePrice The strike price (not the strike rate).
   * @param futuresPrice The price of the underlying futures. Used for relative moneyness smile description.
   * @return The volatility.
   */
  double getVolatility(final double expiry, final double delay, final double strikePrice, double futuresPrice);

  /**
   * Returns the Ibor Index of the futures on for which the Black data is valid, i.e. the data is calibrated to futures on the given index.
   * @return The index.
   */
  IborIndex getFuturesIndex();

}
