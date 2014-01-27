/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Data structure to hold a particular volatility cube's data points.
 * Note no interpolation or fitting is done in this code.
 * 
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 * @param <Z> Type of the z-data
 */
public class VolatilityCubeData<X, Y, Z> {
  /** Default name for the x axis */
  public static final String DEFAULT_X_LABEL = "x";
  /** Default name for the y axis */
  public static final String DEFAULT_Y_LABEL = "y";
  /** Default name for the z axis */
  public static final String DEFAULT_Z_LABEL = "z";

  private static final Comparator<Pair<?, ?>> COMPARATOR = FirstThenSecondPairComparator.INSTANCE;
  private final String _definitionName;
  private final String _specificationName;
  private final UniqueIdentifiable _target;
  private final Map<Triple<X, Y, Z>, Double> _values;
  private final X[] _xs;
  private final Y[] _ys;
  private final Z[] _zs;
  private final Double[] _vs;
  private final SortedSet<X> _uniqueXs;
  private final SortedSet<Y> _uniqueYs;
  private final Map<X, Map<Y, List<ObjectsPair<Z, Double>>>> _strips;
  private final String _xLabel;
  private final String _yLabel;
  private final String _zLabel;

  //public VolatilityCubeData(final String definitionName,
  //    final String specificationName,
  //    final UniqueIdentifiable target,
  //    final X[] xs,
  //    final Y[] ys,
  //    final Z[] zs,
  //    final Map<Triple<X, Y, Z>, Double> values) {
  //  this(definitionName, specificationName, target, xs, DEFAULT_X_LABEL, ys, DEFAULT_Y_LABEL, zs, DEFAULT_Z_LABEL, values);
  //}

  public VolatilityCubeData(final String definitionName,
                            final String specificationName,
                            final UniqueIdentifiable target,
                            final Map<Triple<X, Y, Z>, Double> values) {
    ArgumentChecker.notNull(definitionName, "Definition Name");
    ArgumentChecker.notNull(specificationName, "Specification Name");
    ArgumentChecker.notNull(target, "Target");
    ArgumentChecker.notNull(values, "Volatility Values Map");
    _definitionName = definitionName;
    _specificationName = specificationName;
    _target = target;
    _values = newHashMap(values);
    _uniqueXs = new TreeSet<>();
    _uniqueYs = new TreeSet<>();
    _strips = newHashMap();
    List<X> xs = newArrayList();
    List<Y> ys = newArrayList();
    List<Z> zs = newArrayList();
    List<Double> vs = newArrayList();
    for (Map.Entry<Triple<X, Y, Z>, Double> entries : values.entrySet()) {
      if (!_strips.containsKey(entries.getKey().getFirst())) {
        Map<Y, List<ObjectsPair<Z, Double>>> map = newHashMap();
        _strips.put(entries.getKey().getFirst(), map);
        _uniqueXs.add(entries.getKey().getFirst());
      }
      if (!_strips.get(entries.getKey().getFirst()).containsKey(entries.getKey().getSecond())) {
        List<ObjectsPair<Z, Double>> list = newArrayList();
        _strips.get(entries.getKey().getFirst()).put(entries.getKey().getSecond(), list);
        _uniqueYs.add(entries.getKey().getSecond());
      }
      xs.add(entries.getKey().getFirst());
      ys.add(entries.getKey().getSecond());
      zs.add(entries.getKey().getThird());
      vs.add(entries.getValue());
      _strips.get(entries.getKey().getFirst()).get(entries.getKey().getSecond()).add(ObjectsPair.of(entries.getKey().getThird(), entries.getValue()));
    }
    _xs = (X[]) xs.toArray();
    _xLabel = DEFAULT_X_LABEL;
    _ys = (Y[]) ys.toArray();
    _yLabel = DEFAULT_Y_LABEL;
    _zs = (Z[]) zs.toArray();
    _zLabel = DEFAULT_Z_LABEL;
    _vs = (Double[]) vs.toArray();
  }

  //public VolatilityCubeData(final String definitionName,
  //    final String specificationName,
  //    final UniqueIdentifiable target,
  //    final X[] xs,
  //    final String xLabel,
  //    final Y[] ys,
  //    final String yLabel,
  //    final Z[] zs,
  //    final String zLabel,
  //    final Map<Triple<X, Y, Z>, Double> values) {
  //  ArgumentChecker.notNull(definitionName, "Definition Name");
  //  ArgumentChecker.notNull(specificationName, "Specification Name");
  //  ArgumentChecker.notNull(target, "Target");
  //  ArgumentChecker.notNull(xs, "X axis values");
  //  ArgumentChecker.notNull(xLabel, "x label");
  //  ArgumentChecker.notNull(ys, "Y axis values");
  //  ArgumentChecker.notNull(yLabel, "y label");
  //  ArgumentChecker.notNull(ys, "Z axis values");
  //  ArgumentChecker.notNull(yLabel, "z label");
  //  ArgumentChecker.notNull(values, "Volatility Values Map");
  //  _definitionName = definitionName;
  //  _specificationName = specificationName;
  //  _target = target;
  //  _values = newHashMap(values);
  //  _xs = xs;
  //  _xLabel = xLabel;
  //  _ys = ys;
  //  _yLabel = yLabel;
  //  _zs = zs;
  //  _zLabel = zLabel;
  //  _uniqueXs = new TreeSet<>();
  //  _uniqueYs = new TreeSet<>();
  //  _strips = newHashMap();
  //  for (Map.Entry<Triple<X, Y, Z>, Double> entries : values.entrySet()) {
  //    if (!_strips.containsKey(entries.getKey().getFirst())) {
  //      Map<Y, List<ObjectsPair<Z, Double>>> map = newHashMap();
  //      _strips.put(entries.getKey().getFirst(), map);
  //      _uniqueXs.add(entries.getKey().getFirst());
  //    }
  //    if (!_strips.get(entries.getKey().getFirst()).containsKey(entries.getKey().getSecond())) {
  //      List<ObjectsPair<Z, Double>> list = newArrayList();
  //      _strips.get(entries.getKey().getFirst()).put(entries.getKey().getSecond(), list);
  //      _uniqueYs.add(entries.getKey().getSecond());
  //    }
  //    _strips.get(entries.getKey().getFirst()).get(entries.getKey().getSecond()).add(ObjectsPair.of(entries.getKey().getThird(), entries.getValue()));
  //  }
  //}

  public int size() {
    return _values.size();
  }

  public X[] getXs() {
    return _xs;
  }

  public String getXLabel() {
    return _xLabel;
  }

  public Y[] getYs() {
    return _ys;
  }

  public String getYLabel() {
    return _yLabel;
  }

  public Z[] getZs() {
    return _zs;
  }

  public Double[] getVs() {
    return _vs;
  }

  public String getZLabel() {
    return _zLabel;
  }

  public Double getVolatility(final X x, final Y y, final Z z) {
    return _values.get(Triple.of(x, y, z));
  }

  public SortedSet<X> getUniqueXValues() {
    return _uniqueXs;
  }

  public SortedSet<Y> getUniqueYValues() {
    return _uniqueYs;
  }

  public List<ObjectsPair<Z, Double>> getZValuesForXandY(final X x, final Y y) {
    ArgumentChecker.notNull(x, "x");
    if (!_strips.containsKey(x)) {
      throw new OpenGammaRuntimeException("Could not get strip for x&y value " + x + ":" + y);
    }
    Map<Y, List<ObjectsPair<Z, Double>>> result = _strips.get(x);
    if (result != null) {
      List<ObjectsPair<Z, Double>> rr = result.get(y);
      Collections.sort(rr, COMPARATOR);
      return rr;
    } else {
      return Collections.emptyList();
    }
  }

  public Map<Triple<X, Y, Z>, Double> asMap() {
    return _values;
  }

  public String getDefinitionName() {
    return _definitionName;
  }

  public String getSpecificationName() {
    return _specificationName;
  }

  public UniqueIdentifiable getTarget() {
    return _target;
  }

  /**
   * @deprecated use getTarget()
   * @throws ClassCastException if target not a currency
   * @return currency assuming that the target is a currency
   */
  @Deprecated
  public Currency getCurrency() {
    return (Currency) _target;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof VolatilityCubeData)) {
      return false;
    }
    final VolatilityCubeData<?, ?, ?> other = (VolatilityCubeData<?, ?, ?>) o;
    return getDefinitionName().equals(other.getDefinitionName()) &&
        getSpecificationName().equals(other.getSpecificationName()) &&
        getTarget().equals(other.getTarget()) &&
        Arrays.equals(getXs(), other.getXs()) &&
        Arrays.equals(getYs(), other.getYs()) &&
        Arrays.equals(getZs(), other.getZs()) &&
        getXLabel().equals(other.getXLabel()) &&
        getYLabel().equals(other.getYLabel()) &&
        getZLabel().equals(other.getZLabel()) &&
        _values.equals(other._values);
  }

  @Override
  public int hashCode() {
    return getDefinitionName().hashCode() * getSpecificationName().hashCode() * getTarget().hashCode();
  }

  @Override
  public String toString() {
    return "VolatilitySurfaceData [" +
        "_definitionName='" + _definitionName + "'" +
        ", _specificationName='" + _specificationName + "'" +
        ", _target=" + _target +
        ", _xLabel='" + _xLabel + "'" +
        ", _yLabel='" + _yLabel + "'" +
        ", _zLabel='" + _yLabel + "'" +
        ", _xs=" + (_xs == null ? null : Arrays.asList(_xs)) +
        ", _ys=" + (_ys == null ? null : Arrays.asList(_ys)) +
        ", _ys=" + (_zs == null ? null : Arrays.asList(_zs)) +
        ", _values=" + _values +
        ", _uniqueXs=" + _uniqueXs +
        ", _strips=" + _strips +
        "]";
  }
}
