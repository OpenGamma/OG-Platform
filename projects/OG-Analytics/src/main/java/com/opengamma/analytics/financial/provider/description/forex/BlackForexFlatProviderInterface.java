/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.forex;

import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.util.money.Currency;

/**
 * Interface to Forex volatility (flat) and multi-curves provider.
 */
public interface BlackForexFlatProviderInterface extends BlackForexProviderInterface<BlackForexTermStructureParameters> {

  /**
    * Returns the (Black implied) volatility
    * @param ccy1 The first currency.
    * @param ccy2 The second currency.
    * @param time The time to expiration.
    * @return The volatility.
    */
  double getVolatility(Currency ccy1, Currency ccy2, double time);

  /**
   * Returns the volatility sensitivities at a particular time.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The time to expiration.
   * @return The sensitivities
   */
  Double[] getVolatilityTimeSensitivity(Currency ccy1, Currency ccy2, double time);

}
