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
public class ConfigDBFuturePriceCurveSpecificationSource implements FuturePriceCurveSpecificationSource {

  private final ConfigSourceQuery<FuturePriceCurveSpecification> _query;

  /**
   * @param configSource the config source, not null
   * @deprecated Use {@link #ConfigDBFuturePriceCurveSpecificationSource(ConfigSource,VersionCorrection)} or {@link #init} instead
   */
  @Deprecated
  public ConfigDBFuturePriceCurveSpecificationSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  public ConfigDBFuturePriceCurveSpecificationSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<>(configSource, FuturePriceCurveSpecification.class, versionCorrection));
  }

  private ConfigDBFuturePriceCurveSpecificationSource(final ConfigSourceQuery<FuturePriceCurveSpecification> query) {
    _query = query;
  }

  public static ConfigDBFuturePriceCurveSpecificationSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new ConfigDBFuturePriceCurveSpecificationSource(ConfigSourceQuery.init(context, function, FuturePriceCurveSpecification.class));
  }

  protected ConfigSource getConfigSource() {
    return _query.getConfigSource();
  }

  @Override
  public FuturePriceCurveSpecification getSpecification(final String name, final String instrumentType) {
    return _query.get(name + "_" + instrumentType);
  }

  @Override
  public FuturePriceCurveSpecification getSpecification(final String name, final String instrumentType, final VersionCorrection versionCorrection) {
    return _query.get(name + "_" + instrumentType, versionCorrection);
  }
}
