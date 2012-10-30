/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides a source of currency conversion matrices ({@link CurrencyMatrix}) backed by a config database.
 */
public class ConfigDBCurrencyMatrixSource implements CurrencyMatrixSource {

  /**
   * The config source for the data.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance backed by a config source.
   * 
   * @param configSource  the source, not null
   */
  public ConfigDBCurrencyMatrixSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config source.
   * 
   * @return the config source, not null
   */
  protected ConfigSource getConfigSource() {
    return _configSource;
  }

  //-------------------------------------------------------------------------
  @Override
  public CurrencyMatrix getCurrencyMatrix(final String name) {
    return getConfigSource().getLatestByName(CurrencyMatrix.class, name);
  }

}
