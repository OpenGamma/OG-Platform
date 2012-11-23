/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
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
  public FXForwardCurveSpecification getSpecification(final String name, final String currencyPair) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(currencyPair, "currency pair");
    final FXForwardCurveSpecification specification = _configSource.getLatestByName(FXForwardCurveSpecification.class, name + "_" + currencyPair + "_FX_FORWARD");
    if (specification == null) {
      if (currencyPair.length() == 6) {
        final String firstCcy = currencyPair.substring(0, 3);
        final String secondCcy = currencyPair.substring(3, 6);
        final String reversedCcys = secondCcy + firstCcy;
        return _configSource.getLatestByName(FXForwardCurveSpecification.class, name + "_" + reversedCcys + "_FX_FORWARD");
      }
    }
    return specification;
  }

  @Override
  public FXForwardCurveSpecification getSpecification(final String name, final String currencyPair, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(currencyPair, "currency pair");
    final FXForwardCurveSpecification specification = _configSource.getSingle(FXForwardCurveSpecification.class, name + "_" + currencyPair + "_FX_FORWARD", versionCorrection);
    if (specification == null) {
      if (currencyPair.length() == 6) {
        final String firstCcy = currencyPair.substring(0, 3);
        final String secondCcy = currencyPair.substring(3, 6);
        final String reversedCcys = secondCcy + firstCcy;
        return _configSource.getSingle(FXForwardCurveSpecification.class, name + "_" + reversedCcys + "_FX_FORWARD", versionCorrection);
      }
    }
    return specification;
  }
}
