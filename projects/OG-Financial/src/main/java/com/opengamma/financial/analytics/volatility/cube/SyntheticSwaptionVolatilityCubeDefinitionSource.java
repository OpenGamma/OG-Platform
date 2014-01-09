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
import com.opengamma.util.money.Currency;

/**
 * A source of swaption volatility cube definitions that come from a {@link ConfigSource}
 */
public class SyntheticSwaptionVolatilityCubeDefinitionSource implements VolatilityCubeDefinitionSource {

  private final ConfigSourceQuery<VolatilityCubeDefinition> _query;

  /**
   * @param configSource the config source, not null
   * @deprecated Use {@link #SyntheticSwaptionVolatilityCubeDefinitionSource(ConfigSource,VersionCorrection)} or {@link #init}) instead
   */
  @Deprecated
  public SyntheticSwaptionVolatilityCubeDefinitionSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  public SyntheticSwaptionVolatilityCubeDefinitionSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<>(configSource, VolatilityCubeDefinition.class, versionCorrection));
  }

  private SyntheticSwaptionVolatilityCubeDefinitionSource(final ConfigSourceQuery<VolatilityCubeDefinition> query) {
    _query = query;
  }

  public static SyntheticSwaptionVolatilityCubeDefinitionSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new SyntheticSwaptionVolatilityCubeDefinitionSource(ConfigSourceQuery.init(context, function, VolatilityCubeDefinition.class));
  }

  @Override
  public VolatilityCubeDefinition getDefinition(final Currency currency, final String name) {
    return _query.get(name);
  }

  @Override
  public VolatilityCubeDefinition getDefinition(final Currency currency, final String name, final VersionCorrection versionCorrection) {
    return _query.get(name, versionCorrection);
  }

}
