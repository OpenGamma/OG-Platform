/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.credit;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A source for {@link CurveDefinition}s.
 */
public class ConfigDBCurveDefinitionSource implements CurveDefinitionSource {
  /** The config source */
  private final ConfigSource _configSource;

  /**
   * @param configSource The config source, not null
   */
  public ConfigDBCurveDefinitionSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public CurveDefinition getCurveDefinition(final String name) {
    ArgumentChecker.notNull(name, "name");
    final CurveDefinition result = _configSource.getLatestByName(InterpolatedCurveDefinition.class, name);
    if (result == null) {
      return _configSource.getLatestByName(CurveDefinition.class, name);
    }
    return result;
  }

  @Override
  public CurveDefinition getCurveDefinition(final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(versionCorrection, "version correction");
    final CurveDefinition result = _configSource.getSingle(InterpolatedCurveDefinition.class, name, versionCorrection);
    if (result == null) {
      return _configSource.getSingle(CurveDefinition.class, name, versionCorrection);
    }
    return result;
  }

}
