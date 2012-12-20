/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.cube.fitted;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class FittedSmileDataPoints {
  private static final FirstThenSecondPairComparator<Tenor, Tenor> COMPARATOR = new FirstThenSecondPairComparator<Tenor, Tenor>();
  private final SortedMap<Pair<Tenor, Tenor>, ExternalId[]> _externalIds;
  private final SortedMap<Pair<Tenor, Tenor>, Double[]> _fittedPoints;

  public FittedSmileDataPoints(final Map<Pair<Tenor, Tenor>, ExternalId[]> externalIds, final Map<Pair<Tenor, Tenor>, Double[]> fittedPoints) {
    ArgumentChecker.notNull(externalIds, "external ids");
    ArgumentChecker.notNull(fittedPoints, "fitted pointed");
    ArgumentChecker.isTrue(externalIds.keySet().equals(fittedPoints.keySet()), "Must have same set of keys for external ids and points; have {} and {}", externalIds.keySet(),
        fittedPoints.keySet());
    for (final Map.Entry<Pair<Tenor, Tenor>, ExternalId[]> entry : externalIds.entrySet()) {
      ArgumentChecker.isTrue(entry.getValue().length == fittedPoints.get(entry.getKey()).length, "Must have same number of entries for external ids and points, have {} and {}, ",
          entry.getValue().length, fittedPoints.get(entry.getKey()).length);
    }
    _externalIds = new TreeMap<Pair<Tenor, Tenor>, ExternalId[]>(COMPARATOR);
    _externalIds.putAll(externalIds);
    _fittedPoints = new TreeMap<Pair<Tenor, Tenor>, Double[]>(COMPARATOR);
    _fittedPoints.putAll(fittedPoints);
  }

  public SortedMap<Pair<Tenor, Tenor>, ExternalId[]> getExternalIds() {
    return _externalIds;
  }

  public SortedMap<Pair<Tenor, Tenor>, Double[]> getFittedPoints() {
    return _fittedPoints;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _externalIds.hashCode();
    result = prime * result + _fittedPoints.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
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
      final Double[] relativeStrikes = _fittedPoints.get(entry.getKey());
      final Double[] otherRelativeStrikes = other._fittedPoints.get(entry.getKey());
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

}
