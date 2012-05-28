/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import javax.time.Instant;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ConfigDBFXForwardCurveDefinitionSource implements ForwardCurveDefinitionSource {
  private final ConfigSource _configSource;

  public ConfigDBFXForwardCurveDefinitionSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public ForwardCurveDefinition getDefinition(final String name, final String currencyPair) {
    return _configSource.getLatestByName(ForwardCurveDefinition.class, name + "_" + currencyPair + "_FX_FORWARD");
  }

  @Override
  public ForwardCurveDefinition getDefinition(final String name, final String currencyPair, final Instant version) {
    return _configSource.getByName(ForwardCurveDefinition.class, name + "_" + currencyPair + "_FX_FORWARD", version);
  }
}
