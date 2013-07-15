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
  double getVolatility(final Currency ccy1, final Currency ccy2, final double time);

  /**
   * Returns XXX
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The time to expiration.
   * @return XXX.
   */
  Double[] getVolatilityTimeSensitivity(final Currency ccy1, final Currency ccy2, final double time);

}
