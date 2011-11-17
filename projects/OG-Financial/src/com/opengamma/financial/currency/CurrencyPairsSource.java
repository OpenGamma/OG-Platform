/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.util.money.Currency;

/**
 * A source of market convention currency pairs ({@link CurrencyPairs} instances).
 * TODO methods to look up a single pair - at the moment this requires passing all 5000 pairs over the network every time
 */
public interface CurrencyPairsSource {

  /**
   * @param name The name of the set of currency pairs
   * @return The currency pairs
   */
  CurrencyPairs getCurrencyPairs(String name);

  /*CurrencyPair getCurrencyPair(Currency currency1, Currency currency2, String name);

  Double getRate(Currency currency1, double amount1, Currency currency2, double amount2, String name);*/
}
