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
public class ConfigDBFXForwardCurveDefinitionSource implements FXForwardCurveDefinitionSource {
  private final ConfigSource _configSource;

  public ConfigDBFXForwardCurveDefinitionSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public FXForwardCurveDefinition getDefinition(final String name, final String currencyPair) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(currencyPair, "currency pair");
    final FXForwardCurveDefinition definition = _configSource.getLatestByName(FXForwardCurveDefinition.class, name + "_" + currencyPair + "_FX_FORWARD");
    if (definition == null) {
      if (currencyPair.length() == 6) {
        final String firstCcy = currencyPair.substring(0, 3);
        final String secondCcy = currencyPair.substring(3, 6);
        final String reversedCcys = secondCcy + firstCcy;
        return _configSource.getLatestByName(FXForwardCurveDefinition.class, name + "_" + reversedCcys + "_FX_FORWARD");
      }
    }
    return definition;
  }

  @Override
  public FXForwardCurveDefinition getDefinition(final String name, final String currencyPair, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(currencyPair, "currency pair");
    final FXForwardCurveDefinition definition = _configSource.getSingle(FXForwardCurveDefinition.class, name + "_" + currencyPair + "_FX_FORWARD", versionCorrection);
    if (definition == null) {
      if (currencyPair.length() == 6) {
        final String firstCcy = currencyPair.substring(0, 3);
        final String secondCcy = currencyPair.substring(3, 6);
        final String reversedCcys = secondCcy + firstCcy;
        return _configSource.getSingle(FXForwardCurveDefinition.class, name + "_" + reversedCcys + "_FX_FORWARD", versionCorrection);
      }
    }
    return definition;
  }
}
