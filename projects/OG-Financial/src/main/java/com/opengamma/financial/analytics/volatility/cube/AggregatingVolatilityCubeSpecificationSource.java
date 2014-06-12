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
 * Aggregates an ordered set of volatility cube specification sources into a single source.
 */
public class AggregatingVolatilityCubeSpecificationSource implements VolatilityCubeSpecificationSource {
  /** The specification sources */
  private final Collection<VolatilityCubeSpecificationSource> _sources;

  /**
   * @param sources A collection of sources, not null
   */
  public AggregatingVolatilityCubeSpecificationSource(final Collection<VolatilityCubeSpecificationSource> sources) {
    ArgumentChecker.notNull(sources, "sources");
    _sources = new ArrayList<>(sources);
  }

  @Override
  public VolatilityCubeSpecification getSpecification(final String name) {
    for (final VolatilityCubeSpecificationSource source : _sources) {
      final VolatilityCubeSpecification specification = source.getSpecification(name);
      if (specification != null) {
        return specification;
      }
    }
    return null;
  }

  @Override
  public VolatilityCubeSpecification getSpecification(final String name, final VersionCorrection versionCorrection) {
    for (final VolatilityCubeSpecificationSource source : _sources) {
      final VolatilityCubeSpecification specification = source.getSpecification(name, versionCorrection);
      if (specification != null) {
        return specification;
      }
    }
    return null;
  }
}
