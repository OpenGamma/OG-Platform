/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ConfigDBCurveExposureConfigurationSource implements CurveExposureConfigurationSource {
  private final ConfigSource _configSource;

  public ConfigDBCurveExposureConfigurationSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public CurveExposureConfiguration getCurveExposureConfiguration(final String name) {
    ArgumentChecker.notNull(name, "name");
    return _configSource.getLatestByName(CurveExposureConfiguration.class, name);
  }

  @Override
  public CurveExposureConfiguration getCurveExposureConfiguration(final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(versionCorrection, "version correction");
    return _configSource.getSingle(CurveExposureConfiguration.class, name, versionCorrection);
  }


}
