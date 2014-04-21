/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;

/**
 *
 */
public class ConfigDBFuturePriceCurveDefinitionSource implements FuturePriceCurveDefinitionSource {

  @SuppressWarnings("rawtypes")
  private final ConfigSourceQuery<FuturePriceCurveDefinition> _query;

  /**
   * @param configSource the config source, not null
   * @deprecated Use {@link #ConfigDBFuturePriceCurveDefinitionSource(ConfigSource,VersionCorrection)} or {@link #init} instead
   */
  @Deprecated
  public ConfigDBFuturePriceCurveDefinitionSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  @SuppressWarnings("rawtypes")
  public ConfigDBFuturePriceCurveDefinitionSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<FuturePriceCurveDefinition>(configSource, FuturePriceCurveDefinition.class, versionCorrection));
  }

  @SuppressWarnings("rawtypes")
  private ConfigDBFuturePriceCurveDefinitionSource(final ConfigSourceQuery<FuturePriceCurveDefinition> query) {
    _query = query;
  }

  public static ConfigDBFuturePriceCurveDefinitionSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new ConfigDBFuturePriceCurveDefinitionSource(ConfigSourceQuery.init(context, function, FuturePriceCurveDefinition.class));
  }

  protected ConfigSource getConfigSource() {
    return _query.getConfigSource();
  }

  @Override
  public FuturePriceCurveDefinition<?> getDefinition(final String name, final String instrumentType) {
    return _query.get(name + "_" + instrumentType);
  }

  @Override
  public FuturePriceCurveDefinition<?> getDefinition(final String name, final String instrumentType, final VersionCorrection versionCorrection) {
    return _query.get(name + "_" + instrumentType, versionCorrection);
  }

}
