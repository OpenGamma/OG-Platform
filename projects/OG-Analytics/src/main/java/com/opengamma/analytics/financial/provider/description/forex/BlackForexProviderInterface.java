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
 * Interface for G2++ parameters provider for one currency.
 * @param <VOLATILITY_TYPE> The volatilty type.
 */
public interface BlackForexProviderInterface<VOLATILITY_TYPE> extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider.
   * @return The bundle.
   */
  @Override
  BlackForexProviderInterface<VOLATILITY_TYPE> copy();

  /**
   * Returns the XX
   * @return The parameters.
   */
  VOLATILITY_TYPE getVolatility();

  /**
   * Returns XXX
   * @return The currency pair.
   */
  Pair<Currency, Currency> getCurrencyPair();

  /**
   * XXX
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @return XXX
   */
  boolean checkCurrencies(final Currency ccy1, final Currency ccy2);

  /**
   * Returns the MulticurveProvider from which the InflationProvider is composed.
   * @return The multi-curves provider.
   */
  @Override
  MulticurveProviderInterface getMulticurveProvider();

}
