/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.Instant;

import com.opengamma.core.common.Currency;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of yield curve definitions based on configuration.
 * <p>
 * This supplies curve definitions from a {@link ConfigSource}.
 */
public class ConfigDBVolatilitySurfaceDefinitionSource implements VolatilitySurfaceDefinitionSource {

  /**
   * The config source for the data.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance backed by a config source.
   * @param configSource  the source, not null
   */
  public ConfigDBVolatilitySurfaceDefinitionSource(final ConfigSource configSource) {
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
  public VolatilitySurfaceDefinition<?, ?> getDefinition(final Currency ccy, final String name) {
    return _configSource.getLatestByName(VolatilitySurfaceDefinition.class, name + "_" + ccy.getISOCode());
  }

  @Override
  public VolatilitySurfaceDefinition<?, ?> getDefinition(final Currency ccy, final String name, final Instant version) {
    return _configSource.getByName(VolatilitySurfaceDefinition.class, name + "_" + ccy.getISOCode(), version);
  }

}
