/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import javax.time.Instant;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of swaption volatility cube definitions that come from a {@link ConfigSource}
 * 
 */
public class ConfigDBSwaptionVolatilityCubeDefinitionSource implements SwaptionVolatilityCubeDefinitionSource {
  private final ConfigSource _configSource;

  public ConfigDBSwaptionVolatilityCubeDefinitionSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public SwaptionVolatilityCubeDefinition<?, ?, ?> getDefinition(final String name) {
    ArgumentChecker.notNull(name, "name");
    return _configSource.getLatestByName(SwaptionVolatilityCubeDefinition.class, name);
  }

  @Override
  public SwaptionVolatilityCubeDefinition<?, ?, ?> getDefinition(final String name, final Instant version) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(version, "version");
    return _configSource.getByName(SwaptionVolatilityCubeDefinition.class, name, version);
  }

}
