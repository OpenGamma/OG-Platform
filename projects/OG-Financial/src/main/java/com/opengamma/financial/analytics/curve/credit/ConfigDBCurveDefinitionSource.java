/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.credit;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.id.UniqueIdentifiable;
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
  public CurveDefinition getCurveDefinition(final String name, final UniqueIdentifiable identifier) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(identifier, "identifier");
    return _configSource.getLatestByName(CurveDefinition.class, name + "_" + identifier.getUniqueId().getValue());
  }

  @Override
  public CurveDefinition getCurveDefinition(final String name, final UniqueIdentifiable identifier, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(identifier, "identifier");
    ArgumentChecker.notNull(versionCorrection, "version correction");
    return _configSource.getSingle(CurveDefinition.class, name + "_" + identifier.getUniqueId().getValue(), versionCorrection);
  }

}
