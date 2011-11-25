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
public class FittedSmileDataPoints {
  private static final FirstThenSecondPairComparator<Tenor, Tenor> COMPARATOR = new FirstThenSecondPairComparator<Tenor, Tenor>();
  private final SortedMap<Pair<Tenor, Tenor>, ExternalId[]> _externalIds;
  private final SortedMap<Pair<Tenor, Tenor>, Double[]> _relativeStrikes;
  
  public FittedSmileDataPoints(final Map<Pair<Tenor, Tenor>, ExternalId[]> externalIds, final Map<Pair<Tenor, Tenor>, Double[]> relativeStrikes) {
    Validate.notNull(externalIds, "external ids");
    Validate.notNull(relativeStrikes, "relative strikes");
    Validate.isTrue(externalIds.keySet().equals(relativeStrikes.keySet()));
    for (final Map.Entry<Pair<Tenor, Tenor>, ExternalId[]> entry : externalIds.entrySet()) {
      Validate.isTrue(entry.getValue().length == relativeStrikes.get(entry.getKey()).length);
    }
    _externalIds = new TreeMap<Pair<Tenor, Tenor>, ExternalId[]>(COMPARATOR);
    _externalIds.putAll(externalIds);
    _relativeStrikes = new TreeMap<Pair<Tenor, Tenor>, Double[]>(COMPARATOR);
    _relativeStrikes.putAll(relativeStrikes);
  }
  
  public SortedMap<Pair<Tenor, Tenor>, ExternalId[]> getExternalIds() {
    return _externalIds;
  }
  
  public SortedMap<Pair<Tenor, Tenor>, Double[]> getRelativeStrikes() {
    return _relativeStrikes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _externalIds.hashCode();
    result = prime * result + _relativeStrikes.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FittedSmileDataPoints other = (FittedSmileDataPoints) obj;
    if (_externalIds.size() != other._externalIds.size()) {
      return false;
    }
    for (final Map.Entry<Pair<Tenor, Tenor>, ExternalId[]> entry : _externalIds.entrySet()) {
      if (!other._externalIds.containsKey(entry.getKey())) {
        return false;
      }
      final ExternalId[] ids = entry.getValue();
      final ExternalId[] otherIds = other._externalIds.get(entry.getKey());
      if (ids.length != otherIds.length) {
        return false;
      }
      final Double[] relativeStrikes = _relativeStrikes.get(entry.getKey());
      final Double[] otherRelativeStrikes = other._relativeStrikes.get(entry.getKey());
      for (int i = 0; i < ids.length; i++) {
        if (!ids[i].equals(otherIds[i])) {
          return false;
        }
        if (Double.doubleToLongBits(relativeStrikes[i]) != Double.doubleToLongBits(otherRelativeStrikes[i])) {
          return false;
        }
      }
    }
    return true;
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (Pair<Tenor, Tenor> tenor : _externalIds.keySet()) {
      sb.append(tenor);
      sb.append(": [");
      for (ExternalId id : _externalIds.get(tenor)) {
        sb.append(id);
        sb.append(", ");
      }
      sb.append("], [");
      for (Double strike : _relativeStrikes.get(tenor)) {
        sb.append(strike);
        sb.append(", ");
      }
      sb.append("] \n");
    }
    return sb.toString();
  }
}
