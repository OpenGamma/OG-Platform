/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.util.money.Currency;

/**
 * A source of market convention currency pairs.
 */
public interface CurrencyPairsSource {

  /**
   * Gets the currency pairs by name.
   * 
   * @param name  the name of the set of currency pairs, not null
   * @return the currency pairs, null if not found
   */
  CurrencyPairs getCurrencyPairs(String name);

  /**
   * Gets a single currency pair
   * 
   * @param name  the name of the set of market convention currency pairs, not null
   * @param currency1  the first currency, not null
   * @param currency2  the second currency, not null
   * @return the market convention pair for the currencies, null if not found
   */
  CurrencyPair getCurrencyPair(String name, Currency currency1, Currency currency2);

}
