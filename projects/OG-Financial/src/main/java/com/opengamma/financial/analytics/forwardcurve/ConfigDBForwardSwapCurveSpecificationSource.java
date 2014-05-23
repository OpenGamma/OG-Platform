/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;

/**
 *
 */
public class ConfigDBForwardSwapCurveSpecificationSource implements ForwardCurveSpecificationSource {

  private static final String SUFFIX = "_FORWARD_SWAP";

  private final ConfigSourceQuery<ForwardSwapCurveSpecification> _query;

  /**
   * @param configSource the config source, not null
   * @deprecated Use {@link ConfigDBForwardSwapCurveSpecificationSource(ConfigSource,VersionCorrection)} or {@link #init} instead.
   */
  @Deprecated
  public ConfigDBForwardSwapCurveSpecificationSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  public ConfigDBForwardSwapCurveSpecificationSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<>(configSource, ForwardSwapCurveSpecification.class, versionCorrection));
  }

  private ConfigDBForwardSwapCurveSpecificationSource(final ConfigSourceQuery<ForwardSwapCurveSpecification> query) {
    _query = query;
  }

  public static ConfigDBForwardSwapCurveSpecificationSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new ConfigDBForwardSwapCurveSpecificationSource(ConfigSourceQuery.init(context, function, ForwardSwapCurveSpecification.class));
  }

  @Override
  public ForwardSwapCurveSpecification getSpecification(final String name, final String currency) {
    return _query.get(name + "_" + currency + SUFFIX);
  }

  @Override
  public ForwardSwapCurveSpecification getSpecification(final String name, final String currency, final VersionCorrection versionCorrection) {
    return _query.get(name + "_" + currency + SUFFIX, versionCorrection);
  }
}
