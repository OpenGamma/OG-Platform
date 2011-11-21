/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube.fitting;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SABRFittedSurfacesDataPointsHelper {
  private static final FirstThenSecondPairComparator<Tenor, Tenor> COMPARATOR = new FirstThenSecondPairComparator<Tenor, Tenor>();
  private final SortedMap<Pair<Tenor, Tenor>, ExternalId[]> _externalIds;
  private final SortedMap<Pair<Tenor, Tenor>, Double[]> _relativeStrikes;
  
  public SABRFittedSurfacesDataPointsHelper(final Map<Pair<Tenor, Tenor>, ExternalId[]> externalIds, final Map<Pair<Tenor, Tenor>, Double[]> relativeStrikes) {
    Validate.notNull(externalIds, "external ids");
    Validate.notNull(relativeStrikes, "relative strikes");
    Validate.isTrue(externalIds.keySet().equals(relativeStrikes.keySet()));
    _externalIds = new TreeMap<Pair<Tenor, Tenor>, ExternalId[]>(COMPARATOR);
    _externalIds.putAll(externalIds);
    _relativeStrikes = new TreeMap<Pair<Tenor, Tenor>, Double[]>(COMPARATOR);
    _relativeStrikes.putAll(relativeStrikes);
  }
  
  public SortedMap<Pair<Tenor, Tenor>, ExternalId[]> getExternalIds() {
    return _externalIds;
  }
  
  //public SortedMap<Pair<Tenor, Tenor> >
}
