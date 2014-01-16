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

/**
 * A source of yield curve definitions based on configuration.
 * <p>
 * This supplies curve definitions from a {@link ConfigSource}.
 */
public class ConfigDBVolatilitySurfaceSpecificationSource implements VolatilitySurfaceSpecificationSource {

  /**
   * The config source for the data.
   */
  private final ConfigSourceQuery<VolatilitySurfaceSpecification> _query;

  /**
   * Creates an instance backed by a config source.
   * 
   * @param configSource the source, not null
   * @deprecated Use {@link #ConfigDBVolatilitySurfaceSpecificationSource(ConfigSource, VersionCorrection)} or {@link #init} instead
   */
  @Deprecated
  public ConfigDBVolatilitySurfaceSpecificationSource(final ConfigSource configSource) {
    this(configSource, VersionCorrection.LATEST);
  }

  public ConfigDBVolatilitySurfaceSpecificationSource(final ConfigSource configSource, final VersionCorrection versionCorrection) {
    this(new ConfigSourceQuery<>(configSource, VolatilitySurfaceSpecification.class, versionCorrection));
  }

  private ConfigDBVolatilitySurfaceSpecificationSource(final ConfigSourceQuery<VolatilitySurfaceSpecification> query) {
    _query = query;
  }

  public static ConfigDBVolatilitySurfaceSpecificationSource init(final FunctionCompilationContext context, final FunctionDefinition function) {
    return new ConfigDBVolatilitySurfaceSpecificationSource(ConfigSourceQuery.init(context, function, VolatilitySurfaceSpecification.class));
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
  public VolatilitySurfaceSpecification getSpecification(final String name, final String instrumentType) {
    return getSpecification(name, instrumentType, _query.getVersionCorrection());
  }

  @Override
  public VolatilitySurfaceSpecification getSpecification(final String name, final String instrumentType, final VersionCorrection versionCorrection) {
    final VolatilitySurfaceSpecification specification = _query.get(name + "_" + instrumentType, versionCorrection);
    if (specification == null && InstrumentTypeProperties.FOREX.equals(instrumentType)) {
      final String[] substrings = name.split("_");
      if (substrings.length == 2 && substrings[1].length() == 6) {
        final String firstCcy = substrings[1].substring(0, 3);
        final String secondCcy = substrings[1].substring(3, 6);
        final String reversedCcys = secondCcy + firstCcy;
        return _query.get(substrings[0] + "_" + reversedCcys + "_" + instrumentType, versionCorrection);
      }
    }
    return specification;
  }
}
