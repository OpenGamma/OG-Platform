/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.tuple.Pair;

/**
 * A surface that is constructed from a set of parallel curves ({@link Curve}), with an interpolator to find points between these curves. The
 * curves are assumed to be coplanar (with the planes parallel to the <i>x</i> or <i>y</i> axis), with their orientation supplied on construction.
 * 
 */
public class InterpolatedFromCurvesDoublesSurface extends Surface<Double, Double, Double> {

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points An array of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null
   * @param curves An array of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @return A surface with an automatically-generated name
   */
  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, false);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points An array of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null
   * @param curves An array of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @return A surface with an automatically-generated name
   */
  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final Double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, false);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points A list of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null
   * @param curves A list of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @return A surface with an automatically-generated name
   */
  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final List<Double> points, final List<Curve<Double, Double>> curves,
      final Interpolator1D interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, false);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param curves A map of points of intersection of the curves to curves, not null
   * @param interpolator The interpolator
   * @return A surface with an automatically-generated name
   */
  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final Map<Double, Curve<Double, Double>> curves, 
      final Interpolator1D interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, curves, interpolator, false);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points An array of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null
   * @param curves An array of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param name The name of the surface
   * @return A surface 
   */
  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, false, name);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points An array of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null
   * @param curves An array of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param name The name of the surface
   * @return A surface 
   */
  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final Double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, false, name);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points A list of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null
   * @param curves A list of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param name The name of the surface
   * @return A surface 
   */
  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final List<Double> points, final List<Curve<Double, Double>> curves,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, false, name);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param curves A map of points of intersection of the curves to curves, not null
   * @param interpolator The interpolator
   * @param name The name of the surface
   * @return A surface 
   */
  public static InterpolatedFromCurvesDoublesSurface from(final boolean xzCurves, final Map<Double, Curve<Double, Double>> curves,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, curves, interpolator, false, name);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points An array of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null. Assumed to be sorted (increasing in <i>x</i> or <i>y</i>).
   * @param curves An array of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @return A surface with an automatically-generated name
   */
  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, true);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points An array of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null. Assumed to be sorted (increasing in <i>x</i> or <i>y</i>).
   * @param curves An array of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @return A surface with an automatically-generated name
   */
  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final Double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, true);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points A list of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null. Assumed to be sorted (increasing in <i>x</i> or <i>y</i>).
   * @param curves A list of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @return A surface with an automatically-generated name
   */
  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final List<Double> points, final List<Curve<Double, Double>> curves,
      final Interpolator1D interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, true);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param curves A map of points of intersection of the curves to curves, not null. Assumed to be sorted (increasing in <i>x</i> or <i>y</i>).
   * @param interpolator The interpolator
   * @return A surface with an automatically-generated name
   */
  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final Map<Double, Curve<Double, Double>> curves,
      final Interpolator1D interpolator) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, curves, interpolator, true);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points An array of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null. Assumed to be sorted (increasing in <i>x</i> or <i>y</i>).
   * @param curves An array of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param name The name of the surface
   * @return A surface 
   */
  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, true, name);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points An array of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null. Assumed to be sorted (increasing in <i>x</i> or <i>y</i>).
   * @param curves An array of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param name The name of the surface
   * @return A surface 
   */
  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final Double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, true, name);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points A list of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null. Assumed to be sorted (increasing in <i>x</i> or <i>y</i>).
   * @param curves A list of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param name The name of the surface
   * @return A surface 
   */
  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final List<Double> points, final List<Curve<Double, Double>> curves,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, points, curves, interpolator, true, name);
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param curves A map of points of intersection of the curves to curves, not null. Assumed to be sorted (increasing in <i>x</i> or <i>y</i>).
   * @param interpolator The interpolator
   * @param name The name of the surface
   * @return A surface 
   */
  public static InterpolatedFromCurvesDoublesSurface fromSorted(final boolean xzCurves, final Map<Double, Curve<Double, Double>> curves,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromCurvesDoublesSurface(xzCurves, curves, interpolator, true, name);
  }

  private final double[] _points;
  private final int _nCurves;
  private final Curve<Double, Double>[] _curves;
  private final Interpolator1D _interpolator;
  private final boolean _xzCurves;

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points An array of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null
   * @param curves An array of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param isSorted Are the intersection points of the curve sorted in increasing order
   */
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D interpolator, final boolean isSorted) {
    super();
    Validate.notNull(points, "points");
    Validate.notNull(curves, "curves");
    final int n = points.length;
    Validate.isTrue(points.length > 0 && points.length == curves.length);
    Validate.noNullElements(curves, "curves");
    Validate.notNull(interpolator, "interpolator");
    _xzCurves = xzCurves;
    _points = Arrays.copyOf(points, n);
    _curves = Arrays.copyOf(curves, n);
    _nCurves = n;
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _curves);
    }
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points An array of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null
   * @param curves An array of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param isSorted Are the intersection points of the curve sorted in increasing order
   */
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final Double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D interpolator, final boolean isSorted) {
    super();
    Validate.notNull(points, "points");
    Validate.notNull(curves, "curves");
    Validate.isTrue(points.length > 0 && points.length == curves.length);
    Validate.notNull(interpolator, "interpolator");
    _nCurves = points.length;
    _xzCurves = xzCurves;
    _curves = curves;
    _points = new double[_nCurves];
    for (int i = 0; i < _nCurves; i++) {
      final Double x = points[i];
      Validate.notNull(x, "x");
      Validate.notNull(_curves[i], "curve " + i);
      _points[i] = x;
    }
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _curves);
    }
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points A list of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null
   * @param curves A list of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param isSorted Are the intersection points of the curve sorted in increasing order
   */
  @SuppressWarnings("unchecked")
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final List<Double> points, final List<Curve<Double, Double>> curves,
      final Interpolator1D interpolator, final boolean isSorted) {
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

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param curves A map of points of intersection of the curves to curves, not null
   * @param interpolator The interpolator
   * @param isSorted Are the intersection points of the curve sorted in increasing order
   */
  @SuppressWarnings("unchecked")
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final Map<Double, Curve<Double, Double>> curves, final Interpolator1D interpolator,
      final boolean isSorted) {
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

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points An array of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null
   * @param curves An array of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param isSorted Are the intersection points of the curve sorted in increasing order
   * @param name The name of the surface
   */
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D interpolator, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(points, "points");
    Validate.notNull(curves, "curves");
    final int n = points.length;
    Validate.isTrue(points.length > 0 && points.length == curves.length);
    Validate.noNullElements(curves, "curves");
    Validate.notNull(interpolator, "interpolator");
    _xzCurves = xzCurves;
    _points = Arrays.copyOf(points, n);
    _curves = Arrays.copyOf(curves, n);
    _nCurves = n;
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _curves);
    }
  }

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points An array of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null
   * @param curves An array of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param isSorted Are the intersection points of the curve sorted in increasing order
   * @param name The name of the surface
   */
  @SuppressWarnings("unchecked")
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final Double[] points, final Curve<Double, Double>[] curves,
      final Interpolator1D interpolator, final boolean isSorted, final String name) {
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

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param points A list of points of intersection of the curves on the remaining axis (e.g. if the curves are in the <i>x-z</i> plane, the points indicate where
   * the curves cross the <i>y</i> axis). Not null
   * @param curves A list of curves, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param isSorted Are the intersection points of the curve sorted in increasing order
   * @param name The name of the surface
   */
  @SuppressWarnings("unchecked")
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final List<Double> points, final List<Curve<Double, Double>> curves,
      final Interpolator1D interpolator, final boolean isSorted, final String name) {
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

  /**
   * @param xzCurves Do the curves lie in the <i>x-z</i> plane or the <i>y-z</i> plane.
   * @param curves A map of points of intersection of the curves to curves, not null
   * @param interpolator The interpolator
   * @param isSorted Are the intersection points of the curve sorted in increasing order
   * @param name The name of the surface
   */
  @SuppressWarnings("unchecked")
  public InterpolatedFromCurvesDoublesSurface(final boolean xzCurves, final Map<Double, Curve<Double, Double>> curves, final Interpolator1D interpolator,
      final boolean isSorted, final String name) {
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

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public Double getZValue(final Pair<Double, Double> xy) {
    Validate.notNull(xy);
    return getZValue(xy.getFirst(), xy.getSecond());
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getZData() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public int size() {
    throw new UnsupportedOperationException();
  }

  /**
   * @return The interpolator
   */
  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  /**
   * @return The points at which the curve intersects that axis
   */
  public double[] getPoints() {
    return _points;
  }

  /**
   * @return The curves
   */
  public Curve<Double, Double>[] getCurves() {
    return _curves;
  }

  /**
   * @return Are the curves in the <i>x-z</i> plane
   */
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
