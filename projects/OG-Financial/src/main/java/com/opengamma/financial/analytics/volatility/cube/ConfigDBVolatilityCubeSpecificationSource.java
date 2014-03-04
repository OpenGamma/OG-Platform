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
 * A source of swaption volatility cube specifications that come from a {@link ConfigSource}
 */
public class ConfigDBVolatilityCubeSpecificationSource implements VolatilityCubeSpecificationSource {

  private final ConfigSourceQuery<VolatilityCubeSpecification> _query;

  /**
   * @param configSource the config source, not null
   * @deprecated Use {@link #ConfigDBVolatilityCubeSpecificationSource(ConfigSource,VersionCorrection)} or {@link #init} instead
   */
  @Deprecated
  public ConfigDBVolatilityCubeSpecificationSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  public ConfigDBVolatilityCubeSpecificationSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<>(configSource, VolatilityCubeSpecification.class, versionCorrection));
  }

  private ConfigDBVolatilityCubeSpecificationSource(final ConfigSourceQuery<VolatilityCubeSpecification> query) {
    _query = query;
  }

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
