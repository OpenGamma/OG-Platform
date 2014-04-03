/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * A base class for interpolation in one dimension.
 */
public abstract class Interpolator1D implements Interpolator<Interpolator1DDataBundle, Double>, Serializable {

  private static final long serialVersionUID = 1L;
  private static final double EPS = 1e-6;

  @Override
  public abstract Double interpolate(Interpolator1DDataBundle data, Double value);

  //TODO: [PLAT-6334] Add documentation
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    final double vm = value - EPS;
    final double vp = value + EPS;

    if (vm < data.firstKey()) {
      final double up = interpolate(data, value + EPS);
      final double mid = interpolate(data, value);
      return (up - mid) / EPS;
    } else if (vp > data.lastKey()) {
      final double down = interpolate(data, vm);
      final double mid = interpolate(data, value);
      return (mid - down) / EPS;
    }
    final double up = interpolate(data, value + EPS);
    final double down = interpolate(data, vm);
    return (up - down) / 2 / EPS;
  }

  //TODO: [PLAT-6334] Add documentation
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value, final boolean useFiniteDifferenceSensitivities) {
    return useFiniteDifferenceSensitivities ? getFiniteDifferenceSensitivities(data, value) : getNodeSensitivitiesForValue(data, value);
  }

  /**
   * Computes the sensitivities of the interpolated value to the input data y.
   * @param data The interpolation data.
   * @param value The value for which the interpolation is computed.
   * @return The sensitivity.
   */
  public abstract double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value);

  //TODO: [PLAT-6334] Add documentation
  protected double[] getFiniteDifferenceSensitivities(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    final double[] x = data.getKeys();
    final double[] y = data.getValues();
    final int n = x.length;
    final double[] result = new double[n];
    final Interpolator1DDataBundle dataUp = getDataBundleFromSortedArrays(x, y);
    final Interpolator1DDataBundle dataDown = getDataBundleFromSortedArrays(x, y);
    for (int i = 0; i < n; i++) {
      if (i != 0) {
        dataUp.setYValueAtIndex(i - 1, y[i - 1]);
        dataDown.setYValueAtIndex(i - 1, y[i - 1]);
      }
      dataUp.setYValueAtIndex(i, y[i] + EPS);
      dataDown.setYValueAtIndex(i, y[i] - EPS);
      final double up = interpolate(dataUp, value);
      final double down = interpolate(dataDown, value);
      result[i] = (up - down) / 2 / EPS;
    }
    return result;
  }

  public abstract Interpolator1DDataBundle getDataBundle(double[] x, double[] y);

  public abstract Interpolator1DDataBundle getDataBundleFromSortedArrays(double[] x, double[] y);

  public Interpolator1DDataBundle getDataBundle(final Map<Double, Double> data) {
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

  protected boolean classEquals(final Object o) {
    if (o == null) {
      return false;
    }
    return getClass().equals(o.getClass());
  }
}
