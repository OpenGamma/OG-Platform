/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A source of vol cube definitions based on configuration.
 * <p>
 * This supplies cube definitions from a {@link ConfigSource}.
 */
public class ConfigDBVolatilityCubeDefinitionSource implements VolatilityCubeDefinitionSource {

  /**
   * The config source for the data.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance backed by a config source.
   * @param configSource  the source, not null
   */
  public ConfigDBVolatilityCubeDefinitionSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  @Override
  public VolatilityCubeDefinition getDefinition(final Currency ccy, final String name) {
    return _configSource.getLatestByName(VolatilityCubeDefinition.class, name + "_" + ccy.getCode());
  }

  @Override
  public VolatilityCubeDefinition getDefinition(final Currency ccy, final String name, final VersionCorrection versionCorrection) {
    return _configSource.getSingle(VolatilityCubeDefinition.class, name + "_" + ccy.getCode(), versionCorrection);
  }
}
