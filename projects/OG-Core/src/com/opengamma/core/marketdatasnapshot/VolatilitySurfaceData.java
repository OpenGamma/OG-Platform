/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.tuple.Pair;

/**
 * Data structure to hold a particular volatility surface's data points.  Note no interpolation or fitting is done in this code.
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 */
public class VolatilitySurfaceData<X, Y> {

  private String _definitionName;
  private String _specificationName;
  private UniqueIdentifiable _target;
  private Map<Pair<X, Y>, Double> _values;
  private X[] _xs;
  private Y[] _ys;;

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
