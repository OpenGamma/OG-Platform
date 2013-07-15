/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.core.Source;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * A source of market convention currency pairs.
 */
public interface VersionedCurrencyPairsSource extends Source<CurrencyPairs>, ChangeProvider {

  /**
   * Gets the currency pairs by name.
   * 
   * @param name the name of the set of currency pairs, not null
   * @param versionCorrection the version/correction timestamp of the convention to fetch, not null
   * @return the currency pairs, null if not found
   */
  CurrencyPairs getCurrencyPairs(String name, VersionCorrection versionCorrection);

  /**
   * Gets a single currency pair
   * 
   * @param name the name of the set of market convention currency pairs, not null
   * @param versionCorrection the version/correction timestamp of the convention to fetch, not null
   * @param currency1 the first currency, not null
   * @param currency2 the second currency, not null
   * @return the market convention pair for the currencies, null if not found
   */
  CurrencyPair getCurrencyPair(String name, VersionCorrection versionCorrection, Currency currency1, Currency currency2);

}
