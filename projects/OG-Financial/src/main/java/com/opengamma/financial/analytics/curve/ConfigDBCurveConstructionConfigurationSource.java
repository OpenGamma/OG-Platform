/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Supplies {@link CurveConstructionConfiguration}s stored in a {@link ConfigSource}
 */
public class ConfigDBCurveConstructionConfigurationSource implements CurveConstructionConfigurationSource {

  /** The config source */
  private final ConfigSourceQuery<CurveConstructionConfiguration> _query;

  /**
   * @param configSource The config source, not null
   * @deprecated Use {@link #ConfigDBCurveConstructionConfigurationSource(ConfigSource,VersionCorrection)}, {@link #ConfigDBCurveConstructionConfigurationSource(ConfigSourceQuery)} or {@link #init}
   *             instead
   */
  @Deprecated
  public ConfigDBCurveConstructionConfigurationSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  /**
   * @param configSource The config source, not null
   * @param versionCorrection The version correction to query at, not null
   */
  public ConfigDBCurveConstructionConfigurationSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<CurveConstructionConfiguration>(configSource, CurveConstructionConfiguration.class, versionCorrection));
  }

  public ConfigDBCurveConstructionConfigurationSource(final ConfigSourceQuery<CurveConstructionConfiguration> query) {
    _query = ArgumentChecker.notNull(query, "query");
  }

  public static ConfigDBCurveConstructionConfigurationSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new ConfigDBCurveConstructionConfigurationSource(ConfigSourceQuery.init(context, function, CurveConstructionConfiguration.class));
  }

  @Override
  public CurveConstructionConfiguration getCurveConstructionConfiguration(final String name) {
    ArgumentChecker.notNull(name, "name");
    return _query.get(name);
  }

  @Override
  public CurveConstructionConfiguration getCurveConstructionConfiguration(final String name, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(versionCorrection, "version correction");
    return _query.get(name, versionCorrection);
  }
}
