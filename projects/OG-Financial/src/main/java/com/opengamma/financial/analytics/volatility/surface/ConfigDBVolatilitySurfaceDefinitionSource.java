/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.config.ConfigSourceQuery;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of volatility surface definitions based on configuration.
 * <p>
 * This supplies surface definitions from a {@link ConfigSource}.
 */
public class ConfigDBVolatilitySurfaceDefinitionSource implements VolatilitySurfaceDefinitionSource {

  /**
   * The config source for the data.
   */
  @SuppressWarnings("rawtypes")
  private final ConfigSourceQuery<VolatilitySurfaceDefinition> _query;

  /**
   * Creates an instance backed by a config source. Will use the latest version correction.
   *
   * @param configSource the source, not null
   * @deprecated Use {@link #ConfigDBVolatilitySurfaceDefinitionSource(ConfigSource,VersionCorrection)} or {@link #init} instead
   */
  @Deprecated
  public ConfigDBVolatilitySurfaceDefinitionSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  /**
   * @param configSource The config source, not null
   * @param versionCorrection The version correction, not null
   */
  public ConfigDBVolatilitySurfaceDefinitionSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<>(configSource, VolatilitySurfaceDefinition.class, versionCorrection));
  }

  /**
   * @param query The config source query
   */
  @SuppressWarnings("rawtypes")
  private ConfigDBVolatilitySurfaceDefinitionSource(final ConfigSourceQuery<VolatilitySurfaceDefinition> query) {
    _query = query;
  }

  /**
   * @param context The function compilation context, not null
   * @param function The function, not null
   * @return A volatility surface definition source backed by a config source
   */
  public static ConfigDBVolatilitySurfaceDefinitionSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(function, "function");
    return new ConfigDBVolatilitySurfaceDefinitionSource(ConfigSourceQuery.init(context, function, VolatilitySurfaceDefinition.class));
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
  public VolatilitySurfaceDefinition<?, ?> getDefinition(final String name, final String instrumentType) {
    return getDefinition(name, instrumentType, _query.getVersionCorrection());
  }

  @Override
  public VolatilitySurfaceDefinition<?, ?> getDefinition(final String name, final String instrumentType, final VersionCorrection versionCorrection) {
    final VolatilitySurfaceDefinition<?, ?> definition = _query.get(name + "_" + instrumentType, versionCorrection);
    if (definition == null && InstrumentTypeProperties.FOREX.equals(instrumentType)) {
      final String[] substrings = name.split("_");
      if (substrings.length == 2 && substrings[1].length() == 6) {
        final String firstCcy = substrings[1].substring(0, 3);
        final String secondCcy = substrings[1].substring(3, 6);
        final String reversedCcys = secondCcy + firstCcy;
        return _query.get(substrings[0] + "_" + reversedCcys + "_" + instrumentType, versionCorrection);
      }
    }
    return definition;
  }

}
