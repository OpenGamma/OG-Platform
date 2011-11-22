/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.util.money.Currency;

/**
 * A source of market convention currency pairs ({@link CurrencyPairs} instances).
 */
public interface CurrencyPairsSource {

  /**
   * @param name The name of the set of currency pairs
   * @return The currency pairs or null if there are none in the system with a matching name
   */
  CurrencyPairs getCurrencyPairs(String name);

  /**
   * @param currency1 A currency
   * @param currency2 Another currency
   * @param name Name of the set of market convention currency pairs
   * @return Market convention {@code CurrencyPair} for the currencies or null if there is no pair or not
   * set of pairs matching {@code name}.
   */
  CurrencyPair getCurrencyPair(Currency currency1, Currency currency2, String name);
}
