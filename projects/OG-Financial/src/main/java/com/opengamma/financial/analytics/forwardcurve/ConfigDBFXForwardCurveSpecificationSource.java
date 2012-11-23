/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ConfigDBFXForwardCurveSpecificationSource implements ForwardCurveSpecificationSource {
  private final ConfigSource _configSource;

  public ConfigDBFXForwardCurveSpecificationSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public ForwardCurveSpecification getSpecification(final String name, final String currencyPair) {
    return _configSource.getLatestByName(ForwardCurveSpecification.class, name + "_" + currencyPair + "_FX_FORWARD");
  }

  @Override
  public ForwardCurveSpecification getSpecification(final String name, final String currencyPair, final VersionCorrection versionCorrection) {
    return _configSource.getSingle(ForwardCurveSpecification.class, name + "_" + currencyPair + "_FX_FORWARD", versionCorrection);
  }
}
