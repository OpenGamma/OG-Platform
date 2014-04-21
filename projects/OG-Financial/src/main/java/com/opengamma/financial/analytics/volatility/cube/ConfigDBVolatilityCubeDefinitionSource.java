/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of volatility surface definitions based on configuration.
 * <p>
 * This supplies surface definitions from a {@link ConfigSource}.
 */
public class ConfigDBVolatilityCubeDefinitionSource implements VolatilityCubeDefinitionSource {

  /**
   * The config source for the data.
   */
  @SuppressWarnings("rawtypes")
  private final ConfigSourceQuery<VolatilityCubeDefinition> _query;

  /**
   * Creates an instance backed by a config source. Uses the latest version correction.
   *
   * @param configSource the source, not null
   * @deprecated Use {@link #ConfigDBVolatilityCubeDefinitionSource(ConfigSource,VersionCorrection)} or {@link #init} instead
   */
  @Deprecated
  public ConfigDBVolatilityCubeDefinitionSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  /**
   * @param configSource The config source, not null
   * @param versionCorrection The version correction, not null
   */
  public ConfigDBVolatilityCubeDefinitionSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<>(configSource, VolatilityCubeDefinition.class, versionCorrection));
  }

  /**
   * @param query The config source query
   */
  @SuppressWarnings("rawtypes")
  private ConfigDBVolatilityCubeDefinitionSource(final ConfigSourceQuery<VolatilityCubeDefinition> query) {
    _query = query;
  }

  /**
   * @param context The function compilation context, not null
   * @param function The function, not null
   * @return A volatility cube definition source bakced by a config source
   */
  public static ConfigDBVolatilityCubeDefinitionSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(function, "function");
    return new ConfigDBVolatilityCubeDefinitionSource(ConfigSourceQuery.init(context, function, VolatilityCubeDefinition.class));
  }

  /**
   * Gets the config source.
   *
   * @return the config source, not null
   */
  protected ConfigSource getConfigSource() {
    return _query.getConfigSource();
  }

  //-------------------------------------------------------------------------
  @Override
  public VolatilityCubeDefinition<?, ?, ?> getDefinition(final String name) {
    return getDefinition(name, _query.getVersionCorrection());
  }

  @Override
  public VolatilityCubeDefinition<?, ?, ?> getDefinition(final String name, final VersionCorrection versionCorrection) {
    final VolatilityCubeDefinition<?, ?, ?> definition = _query.get(name, versionCorrection);
    return definition;
  }

}
