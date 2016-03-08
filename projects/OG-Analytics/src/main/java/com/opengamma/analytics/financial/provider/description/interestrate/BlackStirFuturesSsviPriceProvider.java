/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.math.differentiation.ValueDerivatives;

/**
 * Interface of Black implied volatility for STIR futures with volatility given by a SSVI formula.
 */
public interface BlackStirFuturesSsviPriceProvider extends BlackSTIRFuturesProviderInterface {
  
  /**
   * Computes the volatility and its derivative with respect to the inputs.
   * @param expiry The option time to expiration.
   * @param delay The delay between expiration of the option and last trading date of the underlying futures.
   * @param strikePrice The strike price (not the strike rate).
   * @param futuresPrice The price of the underlying futures.
   * @return  The volatility and its derivatives with respect to the inputs. In the {@link ValueDerivatives} object,
   * the order of the derivatives are: [0] price, [1] strike, [2] expiry, [3] ATM vol, [4] rho, [5] eta.
   */
  ValueDerivatives volatilityAdjoint(double expiry, double delay, double strikePrice, double futuresPrice);

}
