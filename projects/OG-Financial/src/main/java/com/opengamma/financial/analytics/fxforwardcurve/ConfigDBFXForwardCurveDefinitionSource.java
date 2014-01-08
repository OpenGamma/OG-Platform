/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ConfigDBFXForwardCurveDefinitionSource implements FXForwardCurveDefinitionSource {

  private final ConfigSourceQuery<FXForwardCurveDefinition> _query;

  /**
   * @param configSource the config source, not null
   * @deprecated Use {@link #ConfigDBFXForwardCurveDefinitionSource(ConfigSource,VersionCorrection)}, {@link #ConfigDBFXForwardCurveDefinitionSource(ConfigSourceQuery)} or {@link #init} instead
   */
  @Deprecated
  public ConfigDBFXForwardCurveDefinitionSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  public ConfigDBFXForwardCurveDefinitionSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<FXForwardCurveDefinition>(configSource, FXForwardCurveDefinition.class, versionCorrection));
  }

  public ConfigDBFXForwardCurveDefinitionSource(final ConfigSourceQuery<FXForwardCurveDefinition> query) {
    _query = ArgumentChecker.notNull(query, "query");
  }

  public static ConfigDBFXForwardCurveDefinitionSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new ConfigDBFXForwardCurveDefinitionSource(ConfigSourceQuery.init(context, function, FXForwardCurveDefinition.class));
  }

  @Override
  public FXForwardCurveDefinition getDefinition(final String name, final String currencyPair) {
    return getDefinition(name, currencyPair, _query.getVersionCorrection());
  }

  @Override
  public FXForwardCurveDefinition getDefinition(final String name, final String currencyPair, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(currencyPair, "currency pair");
    final FXForwardCurveDefinition definition = _query.get(name + "_" + currencyPair + "_FX_FORWARD", versionCorrection);
    if (definition == null) {
      if (currencyPair.length() == 6) {
        final String firstCcy = currencyPair.substring(0, 3);
        final String secondCcy = currencyPair.substring(3, 6);
        final String reversedCcys = secondCcy + firstCcy;
        return _query.get(name + "_" + reversedCcys + "_FX_FORWARD", versionCorrection);
      }
    }
    return definition;
  }
}
