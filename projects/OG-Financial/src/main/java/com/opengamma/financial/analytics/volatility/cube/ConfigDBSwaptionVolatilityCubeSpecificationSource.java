/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.analytics.volatility.SwaptionVolatilityCubeSpecificationSource;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;

/**
 * A source of swaption volatility cube specifications that come from a {@link ConfigSource}
 */
public class ConfigDBSwaptionVolatilityCubeSpecificationSource implements SwaptionVolatilityCubeSpecificationSource {

  private final ConfigSourceQuery<SwaptionVolatilityCubeSpecification> _query;

  /**
   * @param configSource the config source, not null
   * @deprecated Use {@link #ConfigDBSwaptionVolatilityCubeSpecificationSource(ConfigSource,VersionCorrection)} or {@link #init} instead
   */
  @Deprecated
  public ConfigDBSwaptionVolatilityCubeSpecificationSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  public ConfigDBSwaptionVolatilityCubeSpecificationSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<>(configSource, SwaptionVolatilityCubeSpecification.class, versionCorrection));
  }

  private ConfigDBSwaptionVolatilityCubeSpecificationSource(final ConfigSourceQuery<SwaptionVolatilityCubeSpecification> query) {
    _query = query;
  }

  public static ConfigDBSwaptionVolatilityCubeSpecificationSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new ConfigDBSwaptionVolatilityCubeSpecificationSource(ConfigSourceQuery.init(context, function, SwaptionVolatilityCubeSpecification.class));
  }

  @Override
  public SwaptionVolatilityCubeSpecification getSpecification(final String name) {
    return _query.get(name);
  }

  @Override
  public SwaptionVolatilityCubeSpecification getSpecification(final String name, final VersionCorrection versionCorrection) {
    return _query.get(name, versionCorrection);
  }

}
