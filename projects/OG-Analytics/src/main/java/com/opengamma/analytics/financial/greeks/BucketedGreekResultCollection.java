/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
public class BucketedGreekResultCollection implements Iterable<Pair<Greek, double[][]>> {
  /** The bucketed vega for a strike / time surface */
  public static final Greek BUCKETED_VEGA = new Greek(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY), "Bucketed vega") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }

  };

  private final Map<Greek, double[][]> _dataMap = new TreeMap<>();
  private final double[][] _strikes;
  private final double[] _expiries;
  private final int _nExpiries;

  //TODO change strikes to StrikeType
  public BucketedGreekResultCollection(final double[] expiries, final double[][] strikes) {
    ArgumentChecker.notNull(expiries, "expiries");
    ArgumentChecker.notNull(strikes, "strikes");
    _expiries = expiries;
    _nExpiries = expiries.length;
    _strikes = strikes;
  }

  public double[][] getBucketedGreeks(final Greek greek) {
    ArgumentChecker.notNull(greek, "greek");
    return _dataMap.get(greek);
  }

  public void put(final Greek greek, final double[][] result) {
    ArgumentChecker.notNull(greek, "greek");
    if (result != null) {
      ArgumentChecker.isTrue(result.length == _nExpiries, "Wrong number of expiry buckets; have {}, need {}", result.length, _nExpiries);
      for (int i = 0; i < result.length; i++) {
        ArgumentChecker.isTrue(result[i].length == _strikes[i].length, "Wrong number of strike buckets; have {}, need {}", result[i].length, _strikes[i].length);
      }
    }
    _dataMap.put(greek, result);
  }

  public boolean isEmpty() {
    return _dataMap.isEmpty();
  }

  public boolean contains(final Greek greek) {
    return _dataMap.containsKey(greek);
  }

  public int size() {
    return _dataMap.size();
  }

  public Set<Greek> keySet() {
    return Collections.unmodifiableSet(_dataMap.keySet());
  }

  public Collection<double[][]> values() {
    return Collections.unmodifiableCollection(_dataMap.values());
  }

  public double[][] getStrikes() {
    return _strikes;
  }

  public double[] getExpiries() {
    return _expiries;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _dataMap.hashCode();
    result = prime * result + Arrays.hashCode(_expiries);
    result = prime * result + Arrays.hashCode(_strikes);
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
    final BucketedGreekResultCollection other = (BucketedGreekResultCollection) obj;
    if (!Arrays.equals(_expiries, other._expiries)) {
      return false;
    }
    if (!Arrays.equals(_strikes, other._strikes)) {
      return false;
    }
    if (size() != other.size()) {
      return false;
    }
    for (final Map.Entry<Greek, double[][]> entry : _dataMap.entrySet()) {
      if (!other._dataMap.containsKey(entry.getKey())) {
        return false;
      }
      if (!Arrays.deepEquals(entry.getValue(), _dataMap.get(entry.getKey()))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Iterator<Pair<Greek, double[][]>> iterator() {
    return new BackingMapGreekIterator(_dataMap.entrySet().iterator());
  }

  private static class BackingMapGreekIterator implements Iterator<Pair<Greek, double[][]>> {
    private final Iterator<Map.Entry<Greek, double[][]>> _backingIterator;

    public BackingMapGreekIterator(final Iterator<Map.Entry<Greek, double[][]>> backingIterator) {
      _backingIterator = backingIterator;
    }

    @Override
    public boolean hasNext() {
      return _backingIterator.hasNext();
    }

    @Override
    public Pair<Greek, double[][]> next() {
      final Map.Entry<Greek, double[][]> nextEntry = _backingIterator.next();
      return Pairs.<Greek, double[][]>of(nextEntry.getKey(), nextEntry.getValue());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Cannot remove from this iterator");
    }
  }

}
