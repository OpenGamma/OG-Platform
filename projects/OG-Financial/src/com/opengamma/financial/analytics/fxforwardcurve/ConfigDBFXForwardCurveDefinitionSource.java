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
public class ConfigDBFXForwardCurveDefinitionSource implements FXForwardCurveDefinitionSource {
  private final ConfigSource _configSource;

  public ConfigDBFXForwardCurveDefinitionSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public FXForwardCurveDefinition getDefinition(final String name) {
    return _configSource.getLatestByName(FXForwardCurveDefinition.class, name + "_FX_FORWARD");
  }

  @Override
  public FXForwardCurveDefinition getDefinition(final String name, final Instant version) {
    return _configSource.getByName(FXForwardCurveDefinition.class, name + "_FX_FORWARD", version);
  }
}
