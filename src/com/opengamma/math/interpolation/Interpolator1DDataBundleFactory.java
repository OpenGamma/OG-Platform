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
 * Provides factory methods for instantiating instances of {@link Interpolator1DDataBundle}
 * from particular types of common inputs.
 */
public final class Interpolator1DDataBundleFactory {
  private Interpolator1DDataBundleFactory() {
  }

  public static Interpolator1DDataBundle fromArrays(final double[] keys, final double[] values) {
    return new ArrayInterpolator1DDataBundle(keys, values);
  }

  public static Interpolator1DDataBundle fromSortedArrays(final double[] keys, final double[] values) {
    return new ArrayInterpolator1DDataBundle(keys, values, true);
  }

  @SuppressWarnings("unchecked")
  public static Interpolator1DDataBundle fromMap(final Map<Double, Double> data) {
    Validate.notNull(data, "Backing data for interpolation must not be null.");
    Validate.notEmpty(data, "Backing data for interpolation must not be empty.");
    if (data instanceof SortedMap) {
      final double[] keys = ArrayUtils.toPrimitive(data.keySet().toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));
      final double[] values = ArrayUtils.toPrimitive(data.values().toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));
      return fromSortedArrays(keys, values);
    }
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

  @SuppressWarnings("unchecked")
  private static Interpolator1DDataBundle augmentModel(final Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult> interpolator, final Interpolator1DDataBundle baseModel) {
    // basis interpolators

    if (interpolator.getClass().equals(NaturalCubicSplineInterpolator1D.class)) {
      return new Interpolator1DCubicSplineDataBundle(baseModel);
    } else if (interpolator.getClass().equals(DoubleQuadraticInterpolator1D.class)) {
      return new Interpolator1DDoubleQuadraticDataBundle(baseModel);
    } else if (interpolator.getClass().equals(CubicSplineInterpolatorWithSensitivities1D.class)) {
      return new Interpolator1DCubicSplineWithSensitivitiesDataBundle(new Interpolator1DCubicSplineDataBundle(baseModel));
      //
      // } else if (interpolator.getClass().equals(Interpolator1DWithSensitivities.class)) {
      // final Interpolator1DWithSensitivities<? extends Interpolator1DDataBundle> interpolatorSense = (Interpolator1DWithSensitivities<? extends Interpolator1DDataBundle>) interpolator;
      // return augmentModel(interpolatorSense.getUnderlyingInterpolator(), baseModel);
    } else if (interpolator instanceof WrappedInterpolator) {
      final WrappedInterpolator wrapped = (WrappedInterpolator) interpolator;
      return augmentModel((Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult>) wrapped.getUnderlyingInterpolator(), baseModel);
    }

    return baseModel;
  }

  public static Interpolator1DDataBundle fromArrays(final double[] keys, final double[] values, final Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult> interpolator) {
    final Interpolator1DDataBundle baseModel = fromArrays(keys, values);
    return augmentModel(interpolator, baseModel);

  }

  public static Interpolator1DDataBundle fromSortedArrays(final double[] keys, final double[] values,
      final Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult> interpolator) {
    final Interpolator1DDataBundle baseModel = fromSortedArrays(keys, values);
    return augmentModel(interpolator, baseModel);

  }

  public static Interpolator1DDataBundle fromMap(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult> interpolator) {
    final Interpolator1DDataBundle baseModel = fromMap(data);
    return augmentModel(interpolator, baseModel);

  }
}
