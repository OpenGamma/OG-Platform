/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.ParallelArrayBinarySort;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterpolatedFromCurvesDoublesSurface extends Surface<Double, Double, Double> {

  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, false);
  }

  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final Double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, false);
  }

  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final List<Double> points, final List<Curve<Double, Double>> curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, false);
  }

  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final Map<Double, Curve<Double, Double>> curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, curves, interpolator, false);
  }

  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, false, name);
  }

  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final Double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, false, name);
  }

  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final List<Double> points, final List<Curve<Double, Double>> curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, false, name);
  }

  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final Map<Double, Curve<Double, Double>> curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, curves, interpolator, false, name);
  }

  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, true);
  }

  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final Double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, true);
  }

  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final List<Double> points, final List<Curve<Double, Double>> curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, true);
  }

  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final Map<Double, Curve<Double, Double>> curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, curves, interpolator, true);
  }

  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, true, name);
  }

  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final Double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, true, name);
  }

  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final List<Double> points, final List<Curve<Double, Double>> curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, true, name);
  }

  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final Map<Double, Curve<Double, Double>> curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, curves, interpolator, true, name);
  }

  private final double[] _points;
  private final int _nCurves;
  private final Curve<Double, Double>[] _curves;
  private final Interpolator1D<? extends Interpolator1DDataBundle> _interpolator;
  private final boolean _xzCurves;

  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final boolean isSorted) {
    super();
    Validate.notNull(points, "points");
    Validate.notNull(curves, "curves");
    Validate.isTrue(points.length > 0 && points.length == curves.length);
    Validate.noNullElements(curves, "curves");
    Validate.notNull(interpolator, "interpolator");
    _xzCurves = xzCurves;
    _points = points;
    _curves = curves;
    _nCurves = curves.length;
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _curves);
    }
  }

  @SuppressWarnings("unchecked")
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final Double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final boolean isSorted) {
    super();
    Validate.notNull(points, "points");
    Validate.notNull(curves, "curves");
    Validate.isTrue(points.length > 0 && points.length == curves.length);
    Validate.notNull(interpolator, "interpolator");
    _nCurves = points.length;
    _xzCurves = xzCurves;
    _curves = new Curve[_nCurves];
    _points = new double[_nCurves];
    for (int i = 0; i < _nCurves; i++) {
      final Double x = points[i];
      final Curve<Double, Double> curve = curves[i];
      Validate.notNull(x, "x");
      Validate.notNull(curve, "curve");
      _points[i] = x;
      _curves[i] = curve;
    }

    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _curves);
    }
  }

  @SuppressWarnings("unchecked")
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final List<Double> points, final List<Curve<Double, Double>> curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final boolean isSorted) {
    super();
    Validate.notNull(points, "points");
    Validate.notNull(curves, "curves");
    Validate.isTrue(points.size() > 0 && points.size() == curves.size());
    Validate.notNull(interpolator, "interpolator");
    _nCurves = points.size();
    _xzCurves = xzCurves;
    _points = new double[_nCurves];
    _curves = new Curve[_nCurves];
    for (int i = 0; i < _nCurves; i++) {
      final Double x = points.get(i);
      final Curve<Double, Double> curve = curves.get(i);
      Validate.notNull(x, "x");
      Validate.notNull(curve, "curve");
      _points[i] = x;
      _curves[i] = curve;
    }
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _curves);
    }
  }

  @SuppressWarnings("unchecked")
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final Map<Double, Curve<Double, Double>> curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted) {
    super();
    Validate.notNull(curves, "curves");
    Validate.notNull(interpolator, "interpolator");
    Validate.notEmpty(curves, "curves");
    _nCurves = curves.size();
    _xzCurves = xzCurves;
    _points = new double[_nCurves];
    _curves = new Curve[_nCurves];
    int i = 0;
    for (final Map.Entry<Double, Curve<Double, Double>> entry : curves.entrySet()) {
      final Double x = entry.getKey();
      final Curve<Double, Double> curve = entry.getValue();
      Validate.notNull(x, "x");
      Validate.notNull(curve, "curve");
      _points[i] = x;
      _curves[i++] = curve;
    }
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _curves);
    }
  }

  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(points, "points");
    Validate.notNull(curves, "curves");
    Validate.isTrue(points.length > 0 && points.length == curves.length);
    Validate.noNullElements(curves, "curves");
    Validate.notNull(interpolator, "interpolator");
    _xzCurves = xzCurves;
    _points = points;
    _curves = curves;
    _nCurves = curves.length;
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _curves);
    }
  }

  @SuppressWarnings("unchecked")
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final Double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(points, "points");
    Validate.notNull(curves, "curves");
    Validate.isTrue(points.length > 0 && points.length == curves.length);
    Validate.notNull(interpolator, "interpolator");
    _nCurves = points.length;
    _xzCurves = xzCurves;
    _points = new double[_nCurves];
    _curves = new Curve[_nCurves];
    for (int i = 0; i < _nCurves; i++) {
      final Double x = points[i];
      final Curve<Double, Double> curve = curves[i];
      Validate.notNull(x, "x");
      Validate.notNull(curve, "curve");
      _points[i] = x;
      _curves[i] = curve;
    }
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _curves);
    }
  }

  @SuppressWarnings("unchecked")
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final List<Double> points, final List<Curve<Double, Double>> curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator,
      final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(points, "points");
    Validate.notNull(curves, "curves");
    Validate.isTrue(points.size() > 0 && points.size() == curves.size());
    Validate.notNull(interpolator, "interpolator");
    _nCurves = points.size();
    _xzCurves = xzCurves;
    _points = new double[_nCurves];
    _curves = new Curve[_nCurves];
    for (int i = 0; i < _nCurves; i++) {
      final Double x = points.get(i);
      final Curve<Double, Double> curve = curves.get(i);
      Validate.notNull(x, "x");
      Validate.notNull(curve, "curve");
      _points[i] = x;
      _curves[i] = curve;
    }
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _curves);
    }
  }

  @SuppressWarnings("unchecked")
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final Map<Double, Curve<Double, Double>> curves,
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator, final boolean isSorted,
      final String name) {
    super(name);
    Validate.notNull(curves, "curves");
    Validate.notNull(interpolator, "interpolator");
    Validate.notEmpty(curves, "curves");
    _nCurves = curves.size();
    _xzCurves = xzCurves;
    _points = new double[_nCurves];
    _curves = new Curve[_nCurves];
    int i = 0;
    for (final Map.Entry<Double, Curve<Double, Double>> entry : curves.entrySet()) {
      final Double x = entry.getKey();
      final Curve<Double, Double> curve = entry.getValue();
      Validate.notNull(x, "x");
      Validate.notNull(curve, "curve");
      _points[i] = x;
      _curves[i++] = curve;
    }
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _curves);
    }
  }

  @Override
  public Double getZValue(final Double x, final Double y) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    final double[] z = new double[_nCurves];
    int i = 0;
    if (_xzCurves) {
      final int index = Arrays.binarySearch(_points, y);
      if (index < 0) {
        for (final Curve<Double, Double> curve : _curves) {
          z[i++] = curve.getYValue(x);
        }
        return InterpolatedDoublesCurve.fromSorted(_points, z, _interpolator).getYValue(y);
      }
      return _curves[index].getYValue(x);
    }
    final int index = Arrays.binarySearch(_points, x);
    if (index < 0) {
      for (final Curve<Double, Double> curve : _curves) {
        z[i++] = curve.getYValue(y);
      }
      return InterpolatedDoublesCurve.fromSorted(_points, z, _interpolator).getYValue(x);
    }
    return _curves[index].getYValue(y);
  }

  @Override
  public Double getZValue(final Pair<Double, Double> xy) {
    Validate.notNull(xy);
    return getZValue(xy.getFirst(), xy.getSecond());
  }

  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Double[] getZData() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException();
  }

  public Interpolator1D<? extends Interpolator1DDataBundle> getInterpolator() {
    return _interpolator;
  }

  public double[] getPoints() {
    return _points;
  }

  public Curve<Double, Double>[] getCurves() {
    return _curves;
  }

  public boolean isXZCurves() {
    return _xzCurves;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_curves);
    result = prime * result + _interpolator.hashCode();
    result = prime * result + Arrays.hashCode(_points);
    result = prime * result + (_xzCurves ? 1231 : 1237);
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
    final InterpolatedFromCurvesDoublesSurface other = (InterpolatedFromCurvesDoublesSurface) obj;
    if (!Arrays.equals(_curves, other._curves)) {
      return false;
    }
    if (!Arrays.equals(_points, other._points)) {
      return false;
    }
    if (_xzCurves != other._xzCurves) {
      return false;
    }
    return ObjectUtils.equals(_interpolator, other._interpolator);
  }

}
