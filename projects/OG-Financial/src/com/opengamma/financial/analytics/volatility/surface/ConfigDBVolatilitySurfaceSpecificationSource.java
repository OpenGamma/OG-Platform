/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.Instant;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of yield curve definitions based on configuration.
 * <p>
 * This supplies curve definitions from a {@link ConfigSource}.
 */
public class ConfigDBVolatilitySurfaceSpecificationSource implements VolatilitySurfaceSpecificationSource {

  /**
   * The config source for the data.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance backed by a config source.
   * @param configSource  the source, not null
   */
  public ConfigDBVolatilitySurfaceSpecificationSource(final ConfigSource configSource) {
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

  //-------------------------------------------------------------------------

  @Override
  public VolatilitySurfaceSpecification getSpecification(final String currencyLabel, final String name, final String instrumentType) {
    return _configSource.getLatestByName(VolatilitySurfaceSpecification.class, name + "_" + instrumentType);
  }

  @Override
  public VolatilitySurfaceSpecification getSpecification(final String ccyLabel, final String name, final String instrumentType, final Instant version) {
    return _configSource.getByName(VolatilitySurfaceSpecification.class, name + "_" + instrumentType, version);
  }
}
