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

/**
 * A source of yield curve definitions based on configuration.
 * <p>
 * This supplies curve definitions from a {@link ConfigSource}.
 */
public class ConfigDBSurfaceSpecificationSource implements SurfaceSpecificationSource {

  /**
   * The config source for the data.
   */
  private final ConfigSourceQuery<SurfaceSpecification> _query;

  /**
   * @param configSource The config source, not null
   * @param versionCorrection The version correction, not null
   */
  public ConfigDBSurfaceSpecificationSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<>(configSource, SurfaceSpecification.class, versionCorrection));
  }

  /**
   * @param query The config source query
   */
  private ConfigDBSurfaceSpecificationSource(final ConfigSourceQuery<SurfaceSpecification> query) {
    _query = query;
  }

  /**
   * @param context The function compilation context, not null
   * @param function The function, not null
   * @return A surface specification source backed by a config source
   */
  public static ConfigDBSurfaceSpecificationSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new ConfigDBSurfaceSpecificationSource(ConfigSourceQuery.init(context, function, SurfaceSpecification.class));
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
  public SurfaceSpecification getSpecification(final String name) {
    return getSpecification(name, _query.getVersionCorrection());
  }

  @Override
  public SurfaceSpecification getSpecification(final String name, final VersionCorrection versionCorrection) {
    final SurfaceSpecification specification = _query.get(name, versionCorrection);
    return specification;
  }
}
