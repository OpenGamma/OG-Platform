/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.forex;

import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Interface for Black volatility parameters for FX volatility surfaces.
 * @param <VOLATILITY_TYPE> The volatility type.
 */
public interface BlackForexProviderInterface<VOLATILITY_TYPE> extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  @Override
  BlackForexProviderInterface<VOLATILITY_TYPE> copy();

  /**
   * Returns the the volatilities.
   * @return The parameters.
   */
  VOLATILITY_TYPE getVolatility();

  /**
   * Returns the currency pair
   * @return The currency pair.
   */
  Pair<Currency, Currency> getCurrencyPair();

  /**
   * Checks that the two currencies are consistent with those for which this surface applies.
   * The order of the two currencies is not important.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @return True if the currencies are consistent
   */
  boolean checkCurrencies(final Currency ccy1, final Currency ccy2);

  /**
   * Returns the underlying multi-curves provider.
   * @return The multi-curves provider.
   */
  @Override
  MulticurveProviderInterface getMulticurveProvider();

}
