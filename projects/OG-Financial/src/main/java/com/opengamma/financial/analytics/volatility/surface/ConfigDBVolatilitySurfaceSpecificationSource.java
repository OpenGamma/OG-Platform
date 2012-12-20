/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of yield curve definitions based on configuration.
 * <p>
 * This supplies curve definitions from a {@link ConfigSource}.
 */
public class ConfigDBVolatilitySurfaceSpecificationSource implements VolatilitySurfaceSpecificationSource {

  /**
   * The config source for the data.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance backed by a config source.
   * @param configSource  the source, not null
   */
  public ConfigDBVolatilitySurfaceSpecificationSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  /**
   * Gets the config source.
   * @return the config source, not null
   */
  protected ConfigSource getConfigSource() {
    return _configSource;
  }

  //-------------------------------------------------------------------------

  @Override
  public VolatilitySurfaceSpecification getSpecification(final String name, final String instrumentType) {
    final VolatilitySurfaceSpecification specification = _configSource.getLatestByName(VolatilitySurfaceSpecification.class, name + "_" + instrumentType);
    if (specification == null && InstrumentTypeProperties.FOREX.equals(instrumentType)) {
      final String[] substrings = name.split("_");
      if (substrings.length == 2 && substrings[1].length() == 6) {
        final String firstCcy = substrings[1].substring(0, 3);
        final String secondCcy = substrings[1].substring(3, 6);
        final String reversedCcys = secondCcy + firstCcy;
        return _configSource.getLatestByName(VolatilitySurfaceSpecification.class, substrings[0] + "_" + reversedCcys + "_" + instrumentType);
      }
    }
    return specification;
  }

  @Override
  public VolatilitySurfaceSpecification getSpecification(final String name, final String instrumentType, final VersionCorrection versionCorrection) {
    final VolatilitySurfaceSpecification specification = _configSource.getSingle(VolatilitySurfaceSpecification.class, name + "_" + instrumentType, versionCorrection);
    if (specification == null && InstrumentTypeProperties.FOREX.equals(instrumentType)) {
      final String[] substrings = name.split("_");
      if (substrings.length == 2 && substrings[1].length() == 6) {
        final String firstCcy = substrings[1].substring(0, 3);
        final String secondCcy = substrings[1].substring(3, 6);
        final String reversedCcys = secondCcy + firstCcy;
        return _configSource.getSingle(VolatilitySurfaceSpecification.class, substrings[0] + "_" + reversedCcys + "_" + instrumentType, versionCorrection);
      }
    }
    return specification;
  }
}
