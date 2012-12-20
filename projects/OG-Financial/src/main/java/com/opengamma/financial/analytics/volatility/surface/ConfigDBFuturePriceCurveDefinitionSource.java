/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;

/**
 *
 */
public class ConfigDBFuturePriceCurveDefinitionSource implements FuturePriceCurveDefinitionSource {
  private final ConfigSource _configSource;

  public ConfigDBFuturePriceCurveDefinitionSource(final ConfigSource configSource) {
    Validate.notNull(configSource, "config source");
    _configSource = configSource;
  }

  protected ConfigSource getConfigSource() {
    return _configSource;
  }

  @Override
  public FuturePriceCurveDefinition<?> getDefinition(final String name, final String instrumentType) {
    return _configSource.getLatestByName(FuturePriceCurveDefinition.class, name + "_" + instrumentType);
  }

  @Override
  public FuturePriceCurveDefinition<?> getDefinition(final String name, final String instrumentType, final VersionCorrection versionCorrection) {
    return _configSource.getSingle(FuturePriceCurveDefinition.class, name + "_" + instrumentType, versionCorrection);
  }

}
