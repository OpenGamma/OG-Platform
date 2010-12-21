/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
   * Name of the document. This needs to change if multiple matrices make sense (see ConfigSource).
   */
  protected static final String DEFAULT_MATRIX_NAME = "Default";

  /**
   * The config source for the data.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance backed by a config source.
   * @param configSource  the source, not null
   */
  public ConfigDBCurrencyMatrixSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  /**
   * Gets the config source.
   * @return the config source, not null
   */
  protected ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Returns the currency conversion matrix.
   * 
   * @return the matrix
   */
  @Override
  public CurrencyMatrix getCurrencyMatrix() {
    return getConfigSource().getLatestByName(CurrencyMatrix.class, DEFAULT_MATRIX_NAME);
  }

}
