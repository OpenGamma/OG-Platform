/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Source of {@link CurrencyPairs} that obtains them from a {@link ConfigSource}.
 */
public class ConfigDBCurrencyPairsSource implements CurrencyPairsSource {

  /**
   * The config source for the data.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance that obtains {@link CurrencyPairs} from {@code configSource}.
   * @param configSource Source of configuration, not null
   */
  public ConfigDBCurrencyPairsSource(ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  @Override
  public CurrencyPairs getCurrencyPairs(String name) {
    // TODO can name be null? where should the default name be?
    return _configSource.getLatestByName(CurrencyPairs.class, name);
  }

  /*@Override
  public CurrencyPair getCurrencyPair(Currency currency1, Currency currency2, String name) {
    CurrencyPairs currencyPairs = getCurrencyPairs(name);
    if (currencyPairs == null) {
      return null;
    }
    return currencyPairs.getCurrencyPair(currency1, currency2);
  }

  @Override
  public Double getRate(Currency currency1, double amount1, Currency currency2, double amount2, String name) {
    CurrencyPairs currencyPairs = getCurrencyPairs(name);
    if (currencyPairs == null) {
      return null;
    }
    return currencyPairs.getRate(currency1, amount1, currency2, amount2);
  }*/
}
