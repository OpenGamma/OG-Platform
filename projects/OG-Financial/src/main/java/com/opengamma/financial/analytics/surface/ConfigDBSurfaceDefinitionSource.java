/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.surface;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of surface definitions based on a config database.
 * <p>
 * This supplies surface definitions from a {@link ConfigSource}.
 */
public class ConfigDBSurfaceDefinitionSource implements SurfaceDefinitionSource {

  /**
   * The config source for the data.
   */
  @SuppressWarnings("rawtypes")
  private final ConfigSourceQuery<SurfaceDefinition> _query;

  /**
   * @param configSource The config source, not null
   * @param versionCorrection The version correction, not null
   */
  public ConfigDBSurfaceDefinitionSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<>(configSource, SurfaceDefinition.class, versionCorrection));
  }

  /**
   * @param query The config source query
   */
  @SuppressWarnings("rawtypes")
  private ConfigDBSurfaceDefinitionSource(final ConfigSourceQuery<SurfaceDefinition> query) {
    _query = query;
  }

  /**
   * @param context The function compilation context, not null
   * @param function The function, not null
   * @return A surface definition source backed by a config source
   */
  public static ConfigDBSurfaceDefinitionSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(function, "function");
    return new ConfigDBSurfaceDefinitionSource(ConfigSourceQuery.init(context, function, SurfaceDefinition.class));
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
  public SurfaceDefinition<?, ?> getDefinition(final String name) {
    return getDefinition(name, _query.getVersionCorrection());
  }

  @Override
  public SurfaceDefinition<?, ?> getDefinition(final String name, final VersionCorrection versionCorrection) {
    final SurfaceDefinition<?, ?> definition = _query.get(name, versionCorrection);
    return definition;
  }

}
