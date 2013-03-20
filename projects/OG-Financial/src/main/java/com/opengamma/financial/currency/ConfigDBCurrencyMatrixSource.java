/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

/**
 * Provides a source of currency conversion matrices ({@link CurrencyMatrix}) backed by a config database.
 */
public class ConfigDBCurrencyMatrixSource extends AbstractSource<CurrencyMatrix> implements CurrencyMatrixSource {

  /**
   * The config source for the data.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance backed by a config source.
   * 
   * @param configSource the source, not null
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

  // CurrencyMatrixSource

  @Override
  public CurrencyMatrix getCurrencyMatrix(final String name, final VersionCorrection versionCorrection) {
    return getConfigSource().getSingle(CurrencyMatrix.class, name, versionCorrection);
  }

  @Override
  public CurrencyMatrix get(UniqueId identifier) {
    return getConfigSource().getConfig(CurrencyMatrix.class, identifier);
  }

  @Override
  public CurrencyMatrix get(ObjectId identifier, VersionCorrection versionCorrection) {
    return getConfigSource().getConfig(CurrencyMatrix.class, identifier, versionCorrection);
  }

  // ChangeProvider

  @Override
  public ChangeManager changeManager() {
    // TODO: Only need to propogate change messages for configuration items that are CurrencyMatrix type
    return getConfigSource().changeManager();
  }

}
