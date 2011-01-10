/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * A base class for interpolation in one dimension.
 * @param <T> Type of Interpolator1DDataBundle
 */

public abstract class Interpolator1D<T extends Interpolator1DDataBundle> implements Interpolator<T, Double>, Serializable {
  /**
   * Default accuracy
   */
  private final double _eps;

  public Interpolator1D() {
    _eps = 1e-12;
  }

  public Interpolator1D(final double eps) {
    _eps = eps;
  }

  public double getEPS() {
    return _eps;
  }

  public abstract T getDataBundle(double[] x, double[] y);

  public abstract T getDataBundleFromSortedArrays(double[] x, double[] y);

  @SuppressWarnings("unchecked")
  public T getDataBundle(final Map<Double, Double> data) {
    Validate.notNull(data, "Backing data for interpolation must not be null.");
    Validate.notEmpty(data, "Backing data for interpolation must not be empty.");
    if (data instanceof SortedMap) {
      final double[] keys = ArrayUtils.toPrimitive(data.keySet().toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));
      final double[] values = ArrayUtils.toPrimitive(data.values().toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));
      return getDataBundleFromSortedArrays(keys, values);
    }
    final double[] keys = new double[data.size()];
    final double[] values = new double[data.size()];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      keys[i] = entry.getKey();
      values[i] = entry.getValue();
      i++;
    }
    return getDataBundle(keys, values);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result;
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

  @Override
  public abstract Double interpolate(T data, Double value);

  protected boolean classEquals(final Object o) {
    if (o == null) {
      return false;
    }
    return getClass().equals(o.getClass());
  }
}
