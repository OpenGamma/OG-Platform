/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.core.AbstractSource;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Source of {@link CurrencyPairs} that obtains them from a {@link ConfigSource}.
 */
public class ConfigDBCurrencyPairsSource extends AbstractSource<CurrencyPairs> implements CurrencyPairsSource, VersionedCurrencyPairsSource {

  /**
   * The config source for the data.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance that obtains {@link CurrencyPairs} from {@code configSource}.
   * 
   * @param configSource Source of configuration, not null
   */
  public ConfigDBCurrencyPairsSource(ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  // Source

  @Override
  public CurrencyPairs get(UniqueId uniqueId) {
    return _configSource.getConfig(CurrencyPairs.class, uniqueId);
  }

  @Override
  public CurrencyPairs get(ObjectId objectId, VersionCorrection versionCorrection) {
    return _configSource.getConfig(CurrencyPairs.class, objectId, versionCorrection);
  }

  // CurrencyPairsSource

  /**
   * Returns a set of currency pairs with the specified name or null if there are none with a matching name. If {@code name} is null then the default set are looked up using
   * {@link CurrencyPairs#DEFAULT_CURRENCY_PAIRS} as the name.
   * 
   * @param name The name of the set of currency pairs, null for the default set.
   * @return The market convention currency pairs with the specified name or null if there are none that match
   */
  @Override
  public CurrencyPairs getCurrencyPairs(String name) {
    if (name == null) {
      name = CurrencyPairs.DEFAULT_CURRENCY_PAIRS;
    }
    return _configSource.getSingle(CurrencyPairs.class, name, VersionCorrection.LATEST);
  }

  @Override
  public CurrencyPair getCurrencyPair(String name, Currency currency1, Currency currency2) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    CurrencyPairs currencyPairs = getCurrencyPairs(name);
    if (currencyPairs == null) {
      return null;
    }
    return currencyPairs.getCurrencyPair(currency1, currency2);
  }

  // VersionedCurrencyPairsSource

  @Override
  public CurrencyPairs getCurrencyPairs(String name, final VersionCorrection versionCorrection) {
    if (name == null) {
      name = CurrencyPairs.DEFAULT_CURRENCY_PAIRS;
    }
    return _configSource.getSingle(CurrencyPairs.class, name, versionCorrection);
  }

  @Override
  public CurrencyPair getCurrencyPair(final String name, final VersionCorrection versionCorrection, final Currency currency1, final Currency currency2) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    CurrencyPairs currencyPairs = getCurrencyPairs(name, versionCorrection);
    if (currencyPairs == null) {
      return null;
    }
    return currencyPairs.getCurrencyPair(currency1, currency2);
  }

  // ChangeProvider

  @Override
  public ChangeManager changeManager() {
    return _configSource.changeManager();
  }

}
