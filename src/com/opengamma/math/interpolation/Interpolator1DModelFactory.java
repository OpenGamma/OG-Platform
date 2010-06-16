/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

/**
 * Provides factory methods for instantiating instances of {@link Interpolator1DModel}
 * from particular types of common inputs.
 */
public final class Interpolator1DModelFactory {
  private Interpolator1DModelFactory() {
  }

  public static Interpolator1DModel fromArrays(final double[] keys, final double[] values) {
    return new ArrayInterpolator1DModel(keys, values);
  }

  public static Interpolator1DModel fromSortedArrays(final double[] keys, final double[] values) {
    return new ArrayInterpolator1DModel(keys, values, true);
  }

  @SuppressWarnings("unchecked")
  public static Interpolator1DModel fromMap(final Map<Double, Double> data) {
    Validate.notNull(data, "Backing data for interpolation must not be null.");
    Validate.notEmpty(data, "Backing data for interpolation must not be empty.");
    if (data instanceof SortedMap) {
      final double[] keys = ArrayUtils.toPrimitive(data.keySet().toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));
      final double[] values = ArrayUtils.toPrimitive(data.values().toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));
      return fromSortedArrays(keys, values);
    } else {
      final double[] keys = new double[data.size()];
      final double[] values = new double[data.size()];
      int i = 0;
      for (final Map.Entry<Double, Double> entry : data.entrySet()) {
        keys[i] = entry.getKey();
        values[i] = entry.getValue();
        i++;
      }
      return fromArrays(keys, values);
    }
  }

  public static Interpolator1DWithSecondDerivativeModel toModelWithSecondDerivative(final Interpolator1DModel model) {
    return new Interpolator1DWithSecondDerivativeModel(model);
  }

  public static Interpolator1DModel fromArrays(final double[] keys, final double[] values, final Interpolator1D<? extends Interpolator1DModel, ? extends InterpolationResult> interpolator) {
    if (interpolator.getClass().equals(NaturalCubicSplineInterpolator1D.class)) {
      return toModelWithSecondDerivative(fromArrays(keys, values));
    }
    return fromArrays(keys, values);
  }

  public static Interpolator1DModel fromSortedArrays(final double[] keys, final double[] values, final Interpolator1D<? extends Interpolator1DModel, ? extends InterpolationResult> interpolator) {
    if (interpolator.getClass().equals(NaturalCubicSplineInterpolator1D.class)) {
      return toModelWithSecondDerivative(fromSortedArrays(keys, values));
    }
    return fromSortedArrays(keys, values);
  }

  public static Interpolator1DModel fromMap(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DModel, ? extends InterpolationResult> interpolator) {
    if (interpolator.getClass().equals(NaturalCubicSplineInterpolator1D.class)) {
      return toModelWithSecondDerivative(fromMap(data));
    }
    return fromMap(data);
  }
}
