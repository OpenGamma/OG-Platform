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
public class ConfigDBForwardSwapCurveDefinitionSource implements ForwardCurveDefinitionSource {

  private static final String SUFFIX = "_FORWARD_SWAP";

  private final ConfigSourceQuery<ForwardSwapCurveDefinition> _query;

  /**
   * @param configSource the config source, not null
   * @deprecated Use {@link ConfigDBForwardSwapCurveDefinitionSource(ConfigSource,VersionCorrection)} or {@link #init} instead.
   */
  @Deprecated
  public ConfigDBForwardSwapCurveDefinitionSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  public ConfigDBForwardSwapCurveDefinitionSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<>(configSource, ForwardSwapCurveDefinition.class, versionCorrection));
  }

  private ConfigDBForwardSwapCurveDefinitionSource(final ConfigSourceQuery<ForwardSwapCurveDefinition> query) {
    _query = query;
  }

  public static ConfigDBForwardSwapCurveDefinitionSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new ConfigDBForwardSwapCurveDefinitionSource(ConfigSourceQuery.init(context, function, ForwardSwapCurveDefinition.class));
  }

  @Override
  public ForwardSwapCurveDefinition getDefinition(final String name, final String currency) {
    return _query.get(name + "_" + currency + SUFFIX);
  }

  @Override
  public ForwardSwapCurveDefinition getDefinition(final String name, final String currency, final VersionCorrection versionCorrection) {
    return _query.get(name + "_" + currency + SUFFIX, versionCorrection);
  }
}
