/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.List;

import javax.time.Instant;

import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.engine.config.ConfigSource;
import com.opengamma.financial.Currency;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of yield curve definitions based on configuration.
 * <p>
 * This supplies curve definitions from a {@link ConfigSource}.
 */
public class ConfigDBInterpolatedYieldCurveDefinitionSource implements InterpolatedYieldCurveDefinitionSource {

  /**
   * The config source for the data.
   */
  private final ConfigSource _configSource;

  /**
   * Creates an instance backed by a config source.
   * @param configSource  the source, not null
   */
  public ConfigDBInterpolatedYieldCurveDefinitionSource(final ConfigSource configSource) {
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
  public YieldCurveDefinition getDefinition(final Currency ccy, final String name) {
    ConfigSearchRequest configSearchRequest = new ConfigSearchRequest();
    configSearchRequest.setName(name + "_" + ccy.getISOCode());
    List<YieldCurveDefinition> definitions = _configSource.search(YieldCurveDefinition.class, configSearchRequest);
    if (definitions.size() == 0) {
      return null;
    }
    return definitions.get(0);
  }

  @Override
  public YieldCurveDefinition getDefinition(final Currency ccy, final String name, final Instant version) {
    ConfigSearchRequest configSearchRequest = new ConfigSearchRequest();
    configSearchRequest.setName(name + "_" + ccy.getISOCode());
    configSearchRequest.setVersionAsOfInstant(version);
    List<YieldCurveDefinition> definitions = _configSource.search(YieldCurveDefinition.class, configSearchRequest);
    if (definitions.size() == 0) {
      return null;
    }
    return definitions.get(0);
  }

}
