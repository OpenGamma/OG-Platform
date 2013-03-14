/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.credit;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ConfigDBCurveDefinitionSource implements CurveDefinitionSource {
  private final ConfigSource _configSource;

  public ConfigDBCurveDefinitionSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public CurveDefinition getCurveDefinition(final String name) {
    ArgumentChecker.notNull(name, "name");
    return _configSource.getLatestByName(CurveDefinition.class, name);
  }

  @Override
  public CurveDefinition getCurveDefinition(final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(versionCorrection, "version correction");
    return _configSource.getSingle(CurveDefinition.class, name, versionCorrection);
  }

}
