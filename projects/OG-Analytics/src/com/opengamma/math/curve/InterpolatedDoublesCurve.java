/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class InterpolatedDoublesCurve extends DoublesCurve {

  public static InterpolatedDoublesCurve from(final double[] xData, final double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, false);
  }

  public static InterpolatedDoublesCurve from(final Double[] xData, final Double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, false);
  }

  public static InterpolatedDoublesCurve from(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, false);
  }

  public static InterpolatedDoublesCurve from(final DoublesPair[] data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, false);
  }

  public static InterpolatedDoublesCurve from(final Set<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, false);
  }

  public static InterpolatedDoublesCurve from(final List<Double> xData, final List<Double> yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, false);
  }

  public static InterpolatedDoublesCurve from(final List<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, false);
  }

  public static InterpolatedDoublesCurve from(final double[] xData, final double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, false, name);
  }

  public static InterpolatedDoublesCurve from(final Double[] xData, final Double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, false, name);
  }

  public static InterpolatedDoublesCurve from(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, false, name);
  }

  public static InterpolatedDoublesCurve from(final DoublesPair[] data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, false, name);
  }

  public static InterpolatedDoublesCurve from(final Set<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, false, name);
  }

  public static InterpolatedDoublesCurve from(final List<Double> xData, final List<Double> yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, false, name);
  }

  public static InterpolatedDoublesCurve from(final List<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, false, name);
  }

  public static InterpolatedDoublesCurve fromSorted(final double[] xData, final double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, true);
  }

  public static InterpolatedDoublesCurve fromSorted(final Double[] xData, final Double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, true);
  }

  public static InterpolatedDoublesCurve fromSorted(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, true);
  }

  public static InterpolatedDoublesCurve fromSorted(final DoublesPair[] data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, true);
  }

  public static InterpolatedDoublesCurve fromSorted(final Set<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, true);
  }

  public static InterpolatedDoublesCurve fromSorted(final List<Double> xData, final List<Double> yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, true);
  }

  public static InterpolatedDoublesCurve fromSorted(final List<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, true);
  }

  public static InterpolatedDoublesCurve fromSorted(final double[] xData, final double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, true, name);
  }

  public static InterpolatedDoublesCurve fromSorted(final Double[] xData, final Double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, true, name);
  }

  public static InterpolatedDoublesCurve fromSorted(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, true, name);
  }

  public static InterpolatedDoublesCurve fromSorted(final DoublesPair[] data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, true, name);
  }

  public static InterpolatedDoublesCurve fromSorted(final Set<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, true, name);
  }

  public static InterpolatedDoublesCurve fromSorted(final List<Double> xData, final List<Double> yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, true, name);
  }

  public static InterpolatedDoublesCurve fromSorted(final List<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, true, name);
  }

  private Interpolator1DDataBundle _dataBundle;
  @SuppressWarnings("rawtypes")
  private Interpolator1D _interpolator;

  public InterpolatedDoublesCurve(final double[] xData, final double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super(xData, yData, isSorted);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final Double[] xData, final Double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super(xData, yData, isSorted);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super(data, isSorted);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final DoublesPair[] data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super(data, isSorted);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final Set<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super(data, isSorted);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final List<Double> xData, final List<Double> yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super(xData, yData, isSorted);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final List<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super(data, isSorted);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final double[] xData, final double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final Double[] xData, final Double[] yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final Map<Double, Double> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final DoublesPair[] data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final Set<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final List<Double> xData, final List<Double> yData, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted, 
      final String name) {
    super(xData, yData, isSorted, name);
    init(interpolator);
  }

  public InterpolatedDoublesCurve(final List<DoublesPair> data, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolator);
  }

  private void init(final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    Validate.notNull(interpolator, "interpolator");
    Validate.isTrue(size() >= 2);
    _dataBundle = interpolator.getDataBundleFromSortedArrays(getXDataAsPrimitive(), getYDataAsPrimitive());
    _interpolator = interpolator;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Double getYValue(final Double x) {
    Validate.notNull(x, "x");
    return _interpolator.interpolate(_dataBundle, x);
  }

  @SuppressWarnings("unchecked")
  public Interpolator1D<? extends Interpolator1DDataBundle> getInterpolator() {
    return _interpolator;
  }

  public Interpolator1DDataBundle getDataBundle() {
    return _dataBundle;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _dataBundle.hashCode();
    result = prime * result + _interpolator.hashCode();
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
    final InterpolatedDoublesCurve other = (InterpolatedDoublesCurve) obj;
    return ObjectUtils.equals(_dataBundle, other._dataBundle) && ObjectUtils.equals(_interpolator, other._interpolator);
  }

}
