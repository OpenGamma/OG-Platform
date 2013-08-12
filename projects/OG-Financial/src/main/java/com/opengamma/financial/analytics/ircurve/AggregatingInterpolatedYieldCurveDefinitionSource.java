/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;

/**
 * Aggregates an ordered set of sources into a single source.
 */
public class AggregatingInterpolatedYieldCurveDefinitionSource implements InterpolatedYieldCurveDefinitionSource {

  /**
   * The sources being aggregated.
   */
  private final Collection<InterpolatedYieldCurveDefinitionSource> _sources;
  
  /**
   * The aggregating change manager
   */
  private final AggregatingChangeManager _changeManager;

  /**
   * Creates an instance specifying the sources.
   * 
   * @param sources  the sources to aggregate, not null
   */
  public AggregatingInterpolatedYieldCurveDefinitionSource(final Iterable<InterpolatedYieldCurveDefinitionSource> sources) {
    _sources = ImmutableList.copyOf(sources);
    List<ChangeProvider> underlyingChangeProviders = new ArrayList<>();
    for (InterpolatedYieldCurveDefinitionSource source : _sources) {
      underlyingChangeProviders.add(source);
    }
    _changeManager = new AggregatingChangeManager(underlyingChangeProviders);;
  }

  //-------------------------------------------------------------------------
  @Override
  public YieldCurveDefinition getDefinition(final Currency currency, final String name) {
    for (InterpolatedYieldCurveDefinitionSource source : _sources) {
      YieldCurveDefinition definition = source.getDefinition(currency, name);
      if (definition != null) {
        return definition;
      }
    }
    return null;
  }

  @Override
  public YieldCurveDefinition getDefinition(Currency currency, String name, VersionCorrection versionCorrection) {
    for (InterpolatedYieldCurveDefinitionSource source : _sources) {
      YieldCurveDefinition definition = source.getDefinition(currency, name, versionCorrection);
      if (definition != null) {
        return definition;
      }
    }
    return null;
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
