/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.id.UniqueIdentifiable;

/**
 * Data structure to hold the data points for a future price curve.
 * @param <X> Type of the x-data
 */
public class FuturePriceCurveData<X> {
  private String _definitionName;
  private String _specificationName;
  private UniqueIdentifiable _target;
  private Map<X, Double> _values;
  private X[] _xs;

  public FuturePriceCurveData(final String definitionName, final String specificationName, final UniqueIdentifiable target,
                               final X[] xs, final Map<X, Double> values) {
    Validate.notNull(definitionName, "Definition Name");
    Validate.notNull(specificationName, "Specification Name");
    Validate.notNull(target, "Target");
    Validate.notNull(xs, "X axis values");
    Validate.notNull(values, "Volatility Values Map");
    _definitionName = definitionName;
    _specificationName = specificationName;
    _target = target;
    _values = new HashMap<X, Double>(values);
    _xs = xs;
  }

  public X[] getXs() {
    return _xs;
  }

  public Double getFuturePrice(final X x) {
    return _values.get(x);
  }

  public Map<X, Double> asMap() {
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
    if (!(o instanceof FuturePriceCurveData)) {
      return false;
    }
    final FuturePriceCurveData<?> other = (FuturePriceCurveData<?>) o;
    return getDefinitionName().equals(other.getDefinitionName()) &&
           getSpecificationName().equals(other.getSpecificationName()) &&
           getTarget().equals(other.getTarget()) &&
           Arrays.equals(getXs(), other.getXs()) &&
           _values.equals(other._values);
  }

  @Override
  public int hashCode() {
    return getDefinitionName().hashCode() * getSpecificationName().hashCode() * getTarget().hashCode();
  }

}
