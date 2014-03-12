/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.cube.fitted;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class FittedSmileDataPoints {
  private static final FirstThenSecondPairComparator<Tenor, Tenor> COMPARATOR = new FirstThenSecondPairComparator<>();
  private final SortedMap<Pair<Tenor, Tenor>, Double[]> _fittedPoints;

  public FittedSmileDataPoints(final Map<Pair<Tenor, Tenor>, Double[]> fittedPoints) {
    ArgumentChecker.notNull(fittedPoints, "fitted pointed");
    _fittedPoints = new TreeMap<>(COMPARATOR);
    _fittedPoints.putAll(fittedPoints);
  }

  public SortedMap<Pair<Tenor, Tenor>, Double[]> getFittedPoints() {
    return _fittedPoints;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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
    return true;
  }

}
