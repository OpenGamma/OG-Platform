/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.math.ParallelArrayBinarySort;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class InterpolatedDoubleDoubleCurve extends DoubleDoubleCurve {

  public static InterpolatedDoubleDoubleCurve of(final double[] xData, final double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolator, false);
  }

  public static InterpolatedDoubleDoubleCurve of(final Double[] xData, final Double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolator, false);
  }

  public static InterpolatedDoubleDoubleCurve of(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoubleDoubleCurve(data, interpolator, false);
  }

  public static InterpolatedDoubleDoubleCurve of(final DoublesPair[] data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoubleDoubleCurve(data, interpolator, false);
  }

  public static InterpolatedDoubleDoubleCurve of(final Set<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoubleDoubleCurve(data, interpolator, false);
  }

  public static InterpolatedDoubleDoubleCurve of(final double[] xData, final double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolator, false, name);
  }

  public static InterpolatedDoubleDoubleCurve of(final Double[] xData, final Double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolator, false, name);
  }

  public static InterpolatedDoubleDoubleCurve of(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoubleDoubleCurve(data, interpolator, false, name);
  }

  public static InterpolatedDoubleDoubleCurve of(final DoublesPair[] data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoubleDoubleCurve(data, interpolator, false, name);
  }

  public static InterpolatedDoubleDoubleCurve of(final Set<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoubleDoubleCurve(data, interpolator, false, name);
  }

  public static InterpolatedDoubleDoubleCurve of(final double[] xData, final double[] yData, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolators, false);
  }

  public static InterpolatedDoubleDoubleCurve of(final Double[] xData, final Double[] yData, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolators, false);
  }

  public static InterpolatedDoubleDoubleCurve of(final Map<Double, Double> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    return new InterpolatedDoubleDoubleCurve(data, interpolators, false);
  }

  public static InterpolatedDoubleDoubleCurve of(final DoublesPair[] data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    return new InterpolatedDoubleDoubleCurve(data, interpolators, false);
  }

  public static InterpolatedDoubleDoubleCurve of(final Set<DoublesPair> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    return new InterpolatedDoubleDoubleCurve(data, interpolators, false);
  }

  public static InterpolatedDoubleDoubleCurve of(final double[] xData, final double[] yData, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final String name) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolators, false, name);
  }

  public static InterpolatedDoubleDoubleCurve of(final Double[] xData, final Double[] yData, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final String name) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolators, false, name);
  }

  public static InterpolatedDoubleDoubleCurve of(final Map<Double, Double> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final String name) {
    return new InterpolatedDoubleDoubleCurve(data, interpolators, false, name);
  }

  public static InterpolatedDoubleDoubleCurve of(final DoublesPair[] data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final String name) {
    return new InterpolatedDoubleDoubleCurve(data, interpolators, false, name);
  }

  public static InterpolatedDoubleDoubleCurve of(final Set<DoublesPair> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final String name) {
    return new InterpolatedDoubleDoubleCurve(data, interpolators, false, name);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final double[] xData, final double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolator, true);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final Double[] xData, final Double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolator, true);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoubleDoubleCurve(data, interpolator, true);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final DoublesPair[] data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoubleDoubleCurve(data, interpolator, true);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final Set<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoubleDoubleCurve(data, interpolator, true);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final double[] xData, final double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolator, true, name);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final Double[] xData, final Double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolator, true, name);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoubleDoubleCurve(data, interpolator, true, name);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final DoublesPair[] data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoubleDoubleCurve(data, interpolator, true, name);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final Set<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoubleDoubleCurve(data, interpolator, true, name);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final double[] xData, final double[] yData, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolators, true);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final Double[] xData, final Double[] yData, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolators, true);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final Map<Double, Double> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    return new InterpolatedDoubleDoubleCurve(data, interpolators, true);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final DoublesPair[] data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    return new InterpolatedDoubleDoubleCurve(data, interpolators, true);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final Set<DoublesPair> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    return new InterpolatedDoubleDoubleCurve(data, interpolators, true);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final double[] xData, final double[] yData, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators,
      final String name) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolators, true, name);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final Double[] xData, final Double[] yData, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators,
      final String name) {
    return new InterpolatedDoubleDoubleCurve(xData, yData, interpolators, true, name);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final Map<Double, Double> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final String name) {
    return new InterpolatedDoubleDoubleCurve(data, interpolators, true, name);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final DoublesPair[] data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final String name) {
    return new InterpolatedDoubleDoubleCurve(data, interpolators, true, name);
  }

  public static InterpolatedDoubleDoubleCurve ofSorted(final Set<DoublesPair> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final String name) {
    return new InterpolatedDoubleDoubleCurve(data, interpolators, true, name);
  }

  private Double[] _xForInterpolators;
  private Interpolator1D<? extends Interpolator1DDataBundle>[] _interpolators;
  private Map<Interpolator1D<? extends Interpolator1DDataBundle>, Interpolator1DDataBundle> _dataBundles;
  private Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> _interpolatorMap;

  public InterpolatedDoubleDoubleCurve(final double[] xData, final double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super(xData, yData, isSorted);
    init(interpolator);
  }

  public InterpolatedDoubleDoubleCurve(final Double[] xData, final Double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super(xData, yData, isSorted);
    init(interpolator);
  }

  public InterpolatedDoubleDoubleCurve(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super(data, isSorted);
    init(interpolator);
  }

  public InterpolatedDoubleDoubleCurve(final DoublesPair[] data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super(data, isSorted);
    init(interpolator);
  }

  public InterpolatedDoubleDoubleCurve(final Set<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super(data, isSorted);
    init(interpolator);
  }

  public InterpolatedDoubleDoubleCurve(final double[] xData, final double[] yData, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final boolean isSorted) {
    super(xData, yData, isSorted);
    init(interpolators);
  }

  public InterpolatedDoubleDoubleCurve(final Double[] xData, final Double[] yData, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final boolean isSorted) {
    super(xData, yData, isSorted);
    init(interpolators);
  }

  public InterpolatedDoubleDoubleCurve(final Map<Double, Double> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final boolean isSorted) {
    super(data, isSorted);
    init(interpolators);
  }

  public InterpolatedDoubleDoubleCurve(final DoublesPair[] data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final boolean isSorted) {
    super(data, isSorted);
    init(interpolators);
  }

  public InterpolatedDoubleDoubleCurve(final Set<DoublesPair> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final boolean isSorted) {
    super(data, isSorted);
    init(interpolators);
  }

  public InterpolatedDoubleDoubleCurve(final double[] xData, final double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
    init(interpolator);
  }

  public InterpolatedDoubleDoubleCurve(final Double[] xData, final Double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
    init(interpolator);
  }

  public InterpolatedDoubleDoubleCurve(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolator);
  }

  public InterpolatedDoubleDoubleCurve(final DoublesPair[] data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolator);
  }

  public InterpolatedDoubleDoubleCurve(final Set<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolator);
  }

  public InterpolatedDoubleDoubleCurve(final double[] xData, final double[] yData, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final boolean isSorted,
      final String name) {
    super(xData, yData, isSorted, name);
    init(interpolators);
  }

  public InterpolatedDoubleDoubleCurve(final Double[] xData, final Double[] yData, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final boolean isSorted,
      final String name) {
    super(xData, yData, isSorted, name);
    init(interpolators);
  }

  public InterpolatedDoubleDoubleCurve(final Map<Double, Double> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolators);
  }

  public InterpolatedDoubleDoubleCurve(final DoublesPair[] data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolators);
  }

  public InterpolatedDoubleDoubleCurve(final Set<DoublesPair> data, final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolators);
  }

  private void init(final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    init(Collections.<Double, Interpolator1D<? extends Interpolator1DDataBundle>> singletonMap(Double.POSITIVE_INFINITY, interpolator));
  }

  @SuppressWarnings("unchecked")
  private void init(final Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> interpolators) {
    Validate.notNull(interpolators, "interpolators");
    Validate.notEmpty(interpolators, "interpolators");
    Validate.noNullElements(interpolators.keySet(), "x values for interpolators");
    Validate.noNullElements(interpolators.values(), "interpolators");
    Validate.isTrue(size() > 2);
    _xForInterpolators = interpolators.keySet().toArray(new Double[0]);
    _interpolators = interpolators.values().toArray(new Interpolator1D[0]);
    _interpolatorMap = interpolators;
    ParallelArrayBinarySort.parallelBinarySort(_xForInterpolators, _interpolators);
    _dataBundles = new HashMap<Interpolator1D<? extends Interpolator1DDataBundle>, Interpolator1DDataBundle>();
    for (final Interpolator1D<? extends Interpolator1DDataBundle> interpolator : interpolators.values()) {
      if (!_dataBundles.containsKey(interpolator)) {
        _dataBundles.put(interpolator, interpolator.getDataBundleFromSortedArrays(getXDataAsPrimitive(), getYDataAsPrimitive()));
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Double getYValue(final Double x) {
    Validate.notNull(x, "x");
    int index;
    if (_interpolators.length == 1 || x < _xForInterpolators[0]) {
      index = 0;
    } else if (x > _xForInterpolators[_xForInterpolators.length - 1]) {
      index = _xForInterpolators.length - 1;
    } else {
      index = Arrays.binarySearch(_xForInterpolators, x);
    }
    if (index < 0) {
      index = -(index + 1);
    }
    final Interpolator1D interpolator = _interpolators[index];
    return interpolator.interpolate(_dataBundles.get(interpolator), x);
  }

  public Map<Double, Interpolator1D<? extends Interpolator1DDataBundle>> getInterpolators() {
    return _interpolatorMap;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_interpolators);
    result = prime * result + Arrays.hashCode(_xForInterpolators);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InterpolatedDoubleDoubleCurve other = (InterpolatedDoubleDoubleCurve) obj;
    return Arrays.equals(_interpolators, other._interpolators) && Arrays.equals(_xForInterpolators, other._xForInterpolators);
  }

}
