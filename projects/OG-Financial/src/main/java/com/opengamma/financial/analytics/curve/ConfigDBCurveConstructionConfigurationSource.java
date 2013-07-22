/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Supplies {@link CurveConstructionConfiguration}s stored in a {@link ConfigSource}
 */
public class ConfigDBCurveConstructionConfigurationSource implements CurveConstructionConfigurationSource {
  /** The config source */
  private final ConfigSource _configSource;

  /**
   * @param configSource The config source, not null
   */
  public ConfigDBCurveConstructionConfigurationSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public CurveConstructionConfiguration getCurveConstructionConfiguration(final String name) {
    ArgumentChecker.notNull(name, "name");
    return _configSource.getLatestByName(CurveConstructionConfiguration.class, name);
  }

  @Override
  public CurveConstructionConfiguration getCurveConstructionConfiguration(final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(versionCorrection, "version correction");
    final CurveConstructionConfiguration result = _configSource.getSingle(CurveConstructionConfiguration.class, name, versionCorrection);
    if (result == null) {
      return _configSource.getSingle(CurveConstructionConfiguration.class, name, versionCorrection);
    }
    return result;
  }
}
