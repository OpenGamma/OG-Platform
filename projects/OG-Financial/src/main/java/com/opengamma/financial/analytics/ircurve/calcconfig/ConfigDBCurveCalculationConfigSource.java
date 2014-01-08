/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.calcconfig;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ConfigDBCurveCalculationConfigSource implements CurveCalculationConfigSource {

  private final ConfigSourceQuery<MultiCurveCalculationConfig> _query;

  /**
   * @param configSource the config source, not null
   * @deprecated Use {@link #ConfigDBCurveCalculationConfigSource(ConfigSource,VersionCorrection)}, {@link #ConfigDBCurveCalculationConfigSource(ConfigSourceQuery)} or {@link #init} instead
   */
  @Deprecated
  public ConfigDBCurveCalculationConfigSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  public ConfigDBCurveCalculationConfigSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<MultiCurveCalculationConfig>(configSource, MultiCurveCalculationConfig.class, versionCorrection));
  }

  public ConfigDBCurveCalculationConfigSource(final ConfigSourceQuery<MultiCurveCalculationConfig> query) {
    _query = ArgumentChecker.notNull(query, "query");
  }

  public static ConfigDBCurveCalculationConfigSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new ConfigDBCurveCalculationConfigSource(ConfigSourceQuery.init(context, function, MultiCurveCalculationConfig.class));
  }

  @Override
  public MultiCurveCalculationConfig getConfig(final String name) {
    return _query.get(name);
  }

  @Override
  public MultiCurveCalculationConfig getConfig(final String name, final VersionCorrection versionCorrection) {
    return _query.get(name, versionCorrection);
  }

}
