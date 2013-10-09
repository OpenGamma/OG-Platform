/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Supplies {@link ExposureFunction}s stored in a {@link ConfigSource}
 */
public class ConfigDBExposureFunctionsSource implements ExposureFunctionsSource {
  /** The config source */
  private final ConfigSource _configSource;

  /**
   * @param configSource The config source, not null
   */
  public ConfigDBExposureFunctionsSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public ExposureFunctions getExposureFunctions(final String name) {
    ArgumentChecker.notNull(name, "name");
    return _configSource.getLatestByName(ExposureFunctions.class, name);
  }

  @Override
  public ExposureFunctions getExposureFunctions(final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(versionCorrection, "version correction");
    final ExposureFunctions result = _configSource.getSingle(ExposureFunctions.class, name, versionCorrection);
    if (result == null) {
      return _configSource.getSingle(ExposureFunctions.class, name, versionCorrection);
    }
    return result;
  }

}
