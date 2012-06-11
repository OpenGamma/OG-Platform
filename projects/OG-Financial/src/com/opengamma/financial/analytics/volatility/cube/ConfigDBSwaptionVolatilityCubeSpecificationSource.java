/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import javax.time.Instant;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.volatility.SwaptionVolatilityCubeSpecificationSource;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of swaption volatility cube specifications that come from a {@link ConfigSource}
 */
public class ConfigDBSwaptionVolatilityCubeSpecificationSource implements SwaptionVolatilityCubeSpecificationSource {
  private final ConfigSource _configSource;

  public ConfigDBSwaptionVolatilityCubeSpecificationSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public SwaptionVolatilityCubeSpecification getSpecification(final String name) {
    ArgumentChecker.notNull(name, "name");
    return _configSource.getLatestByName(SwaptionVolatilityCubeSpecification.class, name);
  }

  @Override
  public SwaptionVolatilityCubeSpecification getSpecification(final String name, final Instant version) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(version, "version");
    return _configSource.getByName(SwaptionVolatilityCubeSpecification.class, name, version);
  }

}
