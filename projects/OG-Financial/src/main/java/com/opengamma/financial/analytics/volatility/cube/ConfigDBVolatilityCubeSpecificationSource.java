/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;

/**
 * A source of volatility cube specifications that come from a {@link ConfigSource}
 */
public class ConfigDBVolatilityCubeSpecificationSource implements VolatilityCubeSpecificationSource {
  /** The query */
  private final ConfigSourceQuery<VolatilityCubeSpecification> _query;

  /**
   * @param configSource The config source, not null
   * @param versionCorrection The version correction, not null
   */
  public ConfigDBVolatilityCubeSpecificationSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<>(configSource, VolatilityCubeSpecification.class, versionCorrection));
  }

  /**
   * @param query The query, not null
   */
  private ConfigDBVolatilityCubeSpecificationSource(final ConfigSourceQuery<VolatilityCubeSpecification> query) {
    _query = query;
  }

  /**
   * @param context The function compilation context, not null
   * @param function The function definition, not null
   * @return The cube specification source
   */
  public static ConfigDBVolatilityCubeSpecificationSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new ConfigDBVolatilityCubeSpecificationSource(ConfigSourceQuery.init(context, function, VolatilityCubeSpecification.class));
  }

  @Override
  public VolatilityCubeSpecification getSpecification(final String name) {
    return _query.get(name);
  }

  @Override
  public VolatilityCubeSpecification getSpecification(final String name, final VersionCorrection versionCorrection) {
    return _query.get(name, versionCorrection);
  }

}
