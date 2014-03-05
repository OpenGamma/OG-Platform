/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.ArrayList;
import java.util.Collection;

import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Aggregates an ordered set of volatility cube definition sources into a single source.
 */
public class AggregatingVolatilityCubeDefinitionSource implements VolatilityCubeDefinitionSource {
  /** The definition sources */
  private final Collection<VolatilityCubeDefinitionSource> _sources;

  /**
   * @param sources A collection of sources, not null
   */
  public AggregatingVolatilityCubeDefinitionSource(final Collection<VolatilityCubeDefinitionSource> sources) {
    ArgumentChecker.notNull(sources, "sources");
    _sources = new ArrayList<>(sources);
  }

  @Override
  public VolatilityCubeDefinition<?, ?, ?> getDefinition(final String name) {
    for (final VolatilityCubeDefinitionSource source : _sources) {
      final VolatilityCubeDefinition<?, ?, ?> definition = source.getDefinition(name);
      if (definition != null) {
        return definition;
      }
    }
    return null;
  }

  @Override
  public VolatilityCubeDefinition<?, ?, ?> getDefinition(final String name, final VersionCorrection versionCorrection) {
    for (final VolatilityCubeDefinitionSource source : _sources) {
      final VolatilityCubeDefinition<?, ?, ?> definition = source.getDefinition(name, versionCorrection);
      if (definition != null) {
        return definition;
      }
    }
    return null;
  }
}
