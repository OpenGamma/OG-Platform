/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Data structure to hold a particular volatility surface's data points.
 * Note no interpolation or fitting is done in this code.
 * 
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 */
public class VolatilitySurfaceData<X, Y> {
  private static final Comparator<Pair<?, ?>> COMPARATOR = FirstThenSecondPairComparator.INSTANCE;
  private final String _definitionName;
  private final String _specificationName;
  private final UniqueIdentifiable _target;
  private final Map<Pair<X, Y>, Double> _values;
  private final X[] _xs;
  private final Y[] _ys;
  private final SortedSet<X> _uniqueXs;
  private final Map<X, List<ObjectsPair<Y, Double>>> _strips;

  public VolatilitySurfaceData(final String definitionName, final String specificationName, final UniqueIdentifiable target,
                               final X[] xs, final Y[] ys, final Map<Pair<X, Y>, Double> values) {
    Validate.notNull(definitionName, "Definition Name");
    Validate.notNull(specificationName, "Specification Name");
    Validate.notNull(target, "Target");
    Validate.notNull(ys, "Y axis values");
    Validate.notNull(xs, "X axis values");
    Validate.notNull(values, "Volatility Values Map");
    _definitionName = definitionName;
    _specificationName = specificationName;
    _target = target;
    _values = new HashMap<Pair<X, Y>, Double>(values);
    _xs = xs;
    _ys = ys;
    _uniqueXs = new TreeSet<X>(); 
    _strips = new HashMap<X, List<ObjectsPair<Y, Double>>>();
    for (Map.Entry<Pair<X, Y>, Double> entries : values.entrySet()) {
      if (_strips.containsKey(entries.getKey().getFirst())) {
        _strips.get(entries.getKey().getFirst()).add(Pair.of(entries.getKey().getSecond(), entries.getValue()));
      } else {
        _uniqueXs.add(entries.getKey().getFirst());
        final List<ObjectsPair<Y, Double>> list = new ArrayList<ObjectsPair<Y, Double>>();
        list.add(Pair.of(entries.getKey().getSecond(), entries.getValue()));
        _strips.put(entries.getKey().getFirst(), list);
      }
    }
  }

  public int size() {
    return _values.size();
  }
  
  public X[] getXs() {
    return _xs;
  }

  public Y[] getYs() {
    return _ys;
  }

  public Double getVolatility(final X x, final Y y) {
    return _values.get(Pair.of(x, y));
  }
  
  public SortedSet<X> getUniqueXValues() {
    return _uniqueXs;
  }
  
  public List<ObjectsPair<Y, Double>> getYValuesForX(final X x) {
    Validate.notNull(x, "x");
    if (!_strips.containsKey(x)) {
      throw new OpenGammaRuntimeException("Could not get strip for x value " + x);
    }
    List<ObjectsPair<Y, Double>> result = _strips.get(x);
    Collections.sort(result, COMPARATOR);
    return result;
  }
  
  public Map<Pair<X, Y>, Double> asMap() {
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
    if (!(o instanceof VolatilitySurfaceData)) {
      return false;
    }
    final VolatilitySurfaceData<?, ?> other = (VolatilitySurfaceData<?, ?>) o;
    return getDefinitionName().equals(other.getDefinitionName()) &&
           getSpecificationName().equals(other.getSpecificationName()) &&
           getTarget().equals(other.getTarget()) &&
           Arrays.equals(getXs(), other.getXs()) &&
           Arrays.equals(getYs(), other.getYs()) &&
           _values.equals(other._values);
  }

  @Override
  public int hashCode() {
    return getDefinitionName().hashCode() * getSpecificationName().hashCode() * getTarget().hashCode();
  }

}
