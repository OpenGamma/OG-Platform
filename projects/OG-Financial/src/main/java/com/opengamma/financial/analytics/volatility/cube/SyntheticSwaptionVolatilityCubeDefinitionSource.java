/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A source of swaption volatility cube definitions that come from a {@link ConfigSource}
 *
 */
public class SyntheticSwaptionVolatilityCubeDefinitionSource implements VolatilityCubeDefinitionSource {
  private final ConfigSource _configSource;

  public SyntheticSwaptionVolatilityCubeDefinitionSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public VolatilityCubeDefinition getDefinition(final Currency currency, final String name) {
    ArgumentChecker.notNull(name, "name");
    return _configSource.getLatestByName(VolatilityCubeDefinition.class, name);
  }

  @Override
  public VolatilityCubeDefinition getDefinition(final Currency currency, final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return _configSource.getSingle(VolatilityCubeDefinition.class, name, versionCorrection);
  }

}
