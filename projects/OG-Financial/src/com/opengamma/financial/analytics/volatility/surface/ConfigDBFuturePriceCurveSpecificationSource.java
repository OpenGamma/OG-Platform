/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import javax.time.Instant;

import org.apache.commons.lang.Validate;

import com.opengamma.core.config.ConfigSource;

/**
 * 
 */
public class ConfigDBFuturePriceCurveSpecificationSource implements FuturePriceCurveSpecificationSource {
  private final ConfigSource _configSource;

  public ConfigDBFuturePriceCurveSpecificationSource(final ConfigSource configSource) {
    Validate.notNull(configSource, "config source");
    _configSource = configSource;
  }

  protected ConfigSource getConfigSource() {
    return _configSource;
  }

  @Override
  public FuturePriceCurveSpecification getSpecification(final String name, final String instrumentType) {
    return _configSource.getLatestByName(FuturePriceCurveSpecification.class, name + "_" + instrumentType);
  }

  @Override
  public FuturePriceCurveSpecification getSpecification(final String name, final String instrumentType, final Instant version) {
    return _configSource.getByName(FuturePriceCurveSpecification.class, name, version);
  }
}
