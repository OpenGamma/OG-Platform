package com.opengamma.financial.currency;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.util.ArgumentChecker;

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
    return _configSource.getLatestByName(CurrencyPairs.class, name);
  }
}
