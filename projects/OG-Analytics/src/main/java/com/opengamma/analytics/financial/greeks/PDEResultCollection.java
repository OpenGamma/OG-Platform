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

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
//TODO GRID_PV and GRID_IMPLIED_VOL don't belong in here
public class PDEResultCollection implements Iterable<Pair<Greek, double[]>> {
  /** The present value for each strike value on the space grid at expiry */
  public static final Greek GRID_PRICE = new Greek(new NthOrderUnderlying(0, UnderlyingType.FORWARD), "Local Volatility PV") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };

  /** The Black present value for each strike value on the space grid at expiry */
  public static final Greek GRID_BLACK_PRICE = new Greek(new NthOrderUnderlying(0, UnderlyingType.FORWARD), "Black PV") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };

  /** The Black implied volatility for each strike value of the space grid at expiry */
  public static final Greek GRID_IMPLIED_VOL = new Greek(new NthOrderUnderlying(0, UnderlyingType.IMPLIED_VOLATILITY), "Black Implied Volatility") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };

  /** The Black delta for each strike value on the space grid at expiry */
  public static final Greek GRID_BLACK_DELTA = new Greek(new NthOrderUnderlying(1, UnderlyingType.FORWARD), "Forward Black delta") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };
  /** The Black dual delta for each strike value on the space grid at expiry */
  public static final Greek GRID_BLACK_DUAL_DELTA = new Greek(new NthOrderUnderlying(1, UnderlyingType.STRIKE), "Dual Black delta") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };
  /** The Black gamma for each strike value on the space grid at expiry */
  public static final Greek GRID_BLACK_GAMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.FORWARD), "Forward Black gamma") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };
  /** The Black dual gamma for each strike value on the space grid at expiry */
  public static final Greek GRID_BLACK_DUAL_GAMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.STRIKE), "Dual Black gamma") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };
  /** The Black vega for each strike value on the space grid at expiry */
  public static final Greek GRID_BLACK_VEGA = new Greek(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY), "Forward Black vega") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };
  /** The Black driftless vanna for each strike value on the space grid at expiry */
  public static final Greek GRID_BLACK_VANNA = new Greek(new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(1, UnderlyingType.FORWARD),
      new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY))), "Forward Black vanna") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };
  /** The Black vomma for each strike value on the space grid at expiry */
  public static final Greek GRID_BLACK_VOMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.IMPLIED_VOLATILITY), "Forward Black vomma") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };
  /** The delta for each strike value on the space grid at expiry */
  public static final Greek GRID_DELTA = new Greek(new NthOrderUnderlying(1, UnderlyingType.FORWARD), "Forward delta") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };
  /** The dual delta for each strike value on the space grid at expiry */
  public static final Greek GRID_DUAL_DELTA = new Greek(new NthOrderUnderlying(1, UnderlyingType.STRIKE), "Dual delta") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };
  /** The gamma for each strike value on the space grid at expiry */
  public static final Greek GRID_GAMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.FORWARD), "Forward gamma") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };
  /** The dual gamma for each strike value on the space grid at expiry */
  public static final Greek GRID_DUAL_GAMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.STRIKE), "Dual gamma") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };
  /** The vega for each strike value on the space grid at expiry */
  public static final Greek GRID_VEGA = new Greek(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY), "Forward vega") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };
  /** The driftless vanna for each strike value on the space grid at expiry */
  public static final Greek GRID_VANNA = new Greek(new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(1, UnderlyingType.FORWARD), new NthOrderUnderlying(1,
      UnderlyingType.IMPLIED_VOLATILITY))), "Forward vanna") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };

  /** domestic and foreign prices*/
  public static final Greek GRID_DOMESTIC_PV_QUOTE = new Greek(new NthOrderUnderlying(0, UnderlyingType.SPOT_PRICE), "Forex PV quote") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };

  /** The vomma for each strike value on the space grid at expiry */
  public static final Greek GRID_VOMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.IMPLIED_VOLATILITY), "Forward vomma") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      throw new UnsupportedOperationException();
    }
  };

  private final Map<Greek, double[]> _gridDataMap = new TreeMap<>();
  private final double[] _strikes;
  private final int _n;

  public PDEResultCollection(final double[] strikes) {
    ArgumentChecker.notNull(strikes, "strikes");
    _strikes = strikes;
    _n = strikes.length;
  }

  public double[] getGridGreeks(final Greek greek) {
    ArgumentChecker.notNull(greek, "greek");
    return _gridDataMap.get(greek);
  }

  public Double getPointGreek(final Greek greek, final double strike, final Interpolator1D interpolator) {
    ArgumentChecker.notNull(greek, "greek");
    ArgumentChecker.notNull(interpolator, "interpolator");
    if (!(_gridDataMap.containsKey(greek)) || _gridDataMap.get(greek) == null) {
      return null;
    }
    final Interpolator1DDataBundle data = interpolator.getDataBundle(_strikes, _gridDataMap.get(greek));
    return interpolator.interpolate(data, strike);
  }

  public void put(final Greek greek, final double[] result) {
    ArgumentChecker.notNull(greek, "greek");
    if (result != null) {
      ArgumentChecker.isTrue(result.length == _n, "Result is the wrong length; have {}, need {}", result.length, _n);
    }
    _gridDataMap.put(greek, result);
  }

  public boolean isEmpty() {
    return _gridDataMap.isEmpty();
  }

  public boolean contains(final Greek greek) {
    return _gridDataMap.containsKey(greek);
  }

  public int size() {
    return _gridDataMap.size();
  }

  public Set<Greek> keySet() {
    return Collections.unmodifiableSet(_gridDataMap.keySet());
  }

  public Collection<double[]> values() {
    return Collections.unmodifiableCollection(_gridDataMap.values());
  }

  public double[] getStrikes() {
    return _strikes;
  }

  //TODO toPointGreekCollection(double strike, Interpolator1D interpolator)

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    for (final Map.Entry<Greek, double[]> entry : _gridDataMap.entrySet()) {
      result = prime * result + entry.getKey().hashCode();
      if (entry.getValue() != null) {
        result = prime * result + Arrays.hashCode(entry.getValue());
      }
    }
    result = prime * result + Arrays.hashCode(_strikes);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PDEResultCollection)) {
      return false;
    }
    final PDEResultCollection other = (PDEResultCollection) obj;
    if (!ObjectUtils.equals(_strikes, other._strikes)) {
      return false;
    }
    if (size() != other.size()) {
      return false;
    }
    for (final Map.Entry<Greek, double[]> entry : _gridDataMap.entrySet()) {
      if (!other._gridDataMap.containsKey(entry.getKey())) {
        return false;
      }
      if (!Arrays.equals(entry.getValue(), _gridDataMap.get(entry.getKey()))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public Iterator<Pair<Greek, double[]>> iterator() {
    return new BackingMapGreekIterator(_gridDataMap.entrySet().iterator());
  }

  private static class BackingMapGreekIterator implements Iterator<Pair<Greek, double[]>> {
    private final Iterator<Map.Entry<Greek, double[]>> _backingIterator;

    public BackingMapGreekIterator(final Iterator<Map.Entry<Greek, double[]>> backingIterator) {
      _backingIterator = backingIterator;
    }

    @Override
    public boolean hasNext() {
      return _backingIterator.hasNext();
    }

    @Override
    public Pair<Greek, double[]> next() {
      final Map.Entry<Greek, double[]> nextEntry = _backingIterator.next();
      return Pairs.<Greek, double[]>of(nextEntry.getKey(), nextEntry.getValue());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Cannot remove from this iterator");
    }
  }
}
