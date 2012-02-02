/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import javax.time.Instant;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ConfigDBFXForwardCurveSpecificationSource implements FXForwardCurveSpecificationSource {
  private final ConfigSource _configSource;

  public ConfigDBFXForwardCurveSpecificationSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public FXForwardCurveSpecification getSpecification(final String name) {
    return _configSource.getLatestByName(FXForwardCurveSpecification.class, name + "_FX_FORWARD");
  }

  @Override
  public FXForwardCurveSpecification getSpecification(final String name, final Instant instant) {
    return _configSource.getByName(FXForwardCurveSpecification.class, name + "_FX_FORWARD", instant);
  }
}
