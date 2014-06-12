/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.Plane;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.tuple.Triple;

/**
 * A cube that is constructed from a set of parallel surfaces (see {@link Surface}), with an interpolator to find points between these surfaces. The surfaces are assumed to
 * be coplanar (<i>x-y</i>, <i>x-z</i> or <i>y-z</i>), with their orientation supplied on construction of the cube.
 */
public class InterpolatedFromSurfacesDoublesCube extends Cube<Double, Double, Double, Double> {

  /**
   * @param plane The plane in which the surfaces lie
   * @param points An array of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Not null
   * @param surfaces An array of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @return A cube with an automatically-generated name
   */
  public static InterpolatedFromSurfacesDoublesCube from(final Plane plane, final double[] points, final Surface<Double, Double, Double>[] surfaces,
      final Interpolator1D interpolator) {
    return new InterpolatedFromSurfacesDoublesCube(plane, points, surfaces, interpolator, false);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points An array of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Not null
   * @param surfaces An array of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @return A cube with an automatically-generated name
   */
  public static InterpolatedFromSurfacesDoublesCube from(final Plane plane, final Double[] points, final Surface<Double, Double, Double>[] surfaces,
      final Interpolator1D interpolator) {
    return new InterpolatedFromSurfacesDoublesCube(plane, points, surfaces, interpolator, false);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points A list of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Not null
   * @param surfaces A list of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @return A cube with an automatically-generated name
   */
  public static InterpolatedFromSurfacesDoublesCube from(final Plane plane, final List<Double> points, final List<Surface<Double, Double, Double>> surfaces,
      final Interpolator1D interpolator) {
    return new InterpolatedFromSurfacesDoublesCube(plane, points, surfaces, interpolator, false);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param surfaces A map of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis) to surfaces. Not null
   * @param interpolator The interpolator
   * @return A cube with an automatically-generated name
   */
  public static InterpolatedFromSurfacesDoublesCube from(final Plane plane, final Map<Double, Surface<Double, Double, Double>> surfaces,
      final Interpolator1D interpolator) {
    return new InterpolatedFromSurfacesDoublesCube(plane, surfaces, interpolator, false);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points An array of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Not null
   * @param surfaces An array of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param name The name of the cube
   * @return A cube 
   */
  public static InterpolatedFromSurfacesDoublesCube from(final Plane plane, final double[] points, final Surface<Double, Double, Double>[] surfaces,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromSurfacesDoublesCube(plane, points, surfaces, interpolator, false, name);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points An array of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Not null
   * @param surfaces An array of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param name The name of the cube
   * @return A cube 
   */
  public static InterpolatedFromSurfacesDoublesCube from(final Plane plane, final Double[] points, final Surface<Double, Double, Double>[] surfaces,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromSurfacesDoublesCube(plane, points, surfaces, interpolator, false, name);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points A list of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Not null
   * @param surfaces A list of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param name The name of the cube
   * @return A cube
   */
  public static InterpolatedFromSurfacesDoublesCube from(final Plane plane, final List<Double> points, final List<Surface<Double, Double, Double>> surfaces,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromSurfacesDoublesCube(plane, points, surfaces, interpolator, false, name);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param surfaces A map of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis) to surfaces. Not null
   * @param interpolator The interpolator
   * @param name The name of the cube
   * @return A cube
   */
  public static InterpolatedFromSurfacesDoublesCube from(final Plane plane, final Map<Double, Surface<Double, Double, Double>> surfaces,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromSurfacesDoublesCube(plane, surfaces, interpolator, false, name);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points An array of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Assumed to be sorted ascending. Not null
   * @param surfaces An array of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @return A cube with an automatically-generated name
   */
  public static InterpolatedFromSurfacesDoublesCube fromSorted(final Plane plane, final double[] points, final Surface<Double, Double, Double>[] surfaces,
      final Interpolator1D interpolator) {
    return new InterpolatedFromSurfacesDoublesCube(plane, points, surfaces, interpolator, true);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points An array of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Assumed to be sorted ascending. Not null
   * @param surfaces An array of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @return A cube with an automatically-generated name
   */
  public static InterpolatedFromSurfacesDoublesCube fromSorted(final Plane plane, final Double[] points, final Surface<Double, Double, Double>[] surfaces,
      final Interpolator1D interpolator) {
    return new InterpolatedFromSurfacesDoublesCube(plane, points, surfaces, interpolator, true);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points A list of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Assumed to be sorted ascending. Not null
   * @param surfaces A list of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @return A cube with an automatically-generated name
   */
  public static InterpolatedFromSurfacesDoublesCube fromSorted(final Plane plane, final List<Double> points, final List<Surface<Double, Double, Double>> surfaces,
      final Interpolator1D interpolator) {
    return new InterpolatedFromSurfacesDoublesCube(plane, points, surfaces, interpolator, true);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param surfaces A map of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis) to surfaces. The points of intersection are assume to be sorted ascending. Not null
   * @param interpolator The interpolator
   * @return A cube with an automatically-generated name
   */
  public static InterpolatedFromSurfacesDoublesCube fromSorted(final Plane plane, final Map<Double, Surface<Double, Double, Double>> surfaces,
      final Interpolator1D interpolator) {
    return new InterpolatedFromSurfacesDoublesCube(plane, surfaces, interpolator, true);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points An array of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Assumed to be sorted ascending. Not null
   * @param surfaces An array of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param name The name of the cube
   * @return A cube
   */
  public static InterpolatedFromSurfacesDoublesCube fromSorted(final Plane plane, final double[] points, final Surface<Double, Double, Double>[] surfaces,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromSurfacesDoublesCube(plane, points, surfaces, interpolator, true, name);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points An array of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Assumed to be sorted ascending. Not null
   * @param surfaces An array of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param name The name of the cube
   * @return A cube
   */
  public static InterpolatedFromSurfacesDoublesCube fromSorted(final Plane plane, final Double[] points, final Surface<Double, Double, Double>[] surfaces,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromSurfacesDoublesCube(plane, points, surfaces, interpolator, false, name);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points A list of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Assumed to be sorted ascending. Not null
   * @param surfaces A list of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param name The name of the cube
   * @return A cube
   */
  public static InterpolatedFromSurfacesDoublesCube fromSorted(final Plane plane, final List<Double> points, final List<Surface<Double, Double, Double>> surfaces,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromSurfacesDoublesCube(plane, points, surfaces, interpolator, false, name);
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param surfaces A map of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis) to surfaces. The points of intersection are assume to be sorted ascending. Not null
   * @param interpolator The interpolator
   * @param name The name of the cube
   * @return A cube
   */
  public static InterpolatedFromSurfacesDoublesCube fromSorted(final Plane plane, final Map<Double, Surface<Double, Double, Double>> surfaces,
      final Interpolator1D interpolator, final String name) {
    return new InterpolatedFromSurfacesDoublesCube(plane, surfaces, interpolator, false, name);
  }

  private final Plane _plane;
  private final double[] _points;
  private final Surface<Double, Double, Double>[] _surfaces;
  private final int _nSurfaces;
  private final Interpolator1D _interpolator;

  /**
   * @param plane The plane in which the surfaces lie
   * @param points An array of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Not null
   * @param surfaces An array of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param isSorted Is the intersection point data sorted
   */
  public InterpolatedFromSurfacesDoublesCube(final Plane plane, final double[] points, final Surface<Double, Double, Double>[] surfaces,
      final Interpolator1D interpolator, final boolean isSorted) {
    super();
    Validate.notNull(plane, "plane");
    Validate.notNull(points, "points");
    Validate.notNull(surfaces, "surfaces");
    final int n = points.length;
    Validate.isTrue(n > 0);
    Validate.isTrue(n == surfaces.length);
    Validate.noNullElements(surfaces, "surfaces");
    Validate.notNull(interpolator, "interpolator");
    _plane = plane;
    _points = Arrays.copyOf(points, n);
    _surfaces = Arrays.copyOf(surfaces, n);
    _nSurfaces = n;
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _surfaces);
    }
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points An array of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Not null
   * @param surfaces An array of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param isSorted Is the intersection point data sorted
   */
  @SuppressWarnings("unchecked")
  public InterpolatedFromSurfacesDoublesCube(final Plane plane, final Double[] points, final Surface<Double, Double, Double>[] surfaces,
      final Interpolator1D interpolator, final boolean isSorted) {
    super();
    Validate.notNull(plane, "plane");
    Validate.notNull(points, "points");
    Validate.notNull(surfaces, "surfaces");
    final int n = points.length;
    Validate.isTrue(n > 0);
    Validate.isTrue(n == surfaces.length);
    Validate.notNull(interpolator, "interpolator");
    _plane = plane;
    _points = new double[n];
    _surfaces = new Surface[n];
    for (int i = 0; i < n; i++) {
      Validate.notNull(points[i], "point " + i);
      Validate.notNull(surfaces[i], "surface " + i);
      _points[i] = points[i];
      _surfaces[i] = surfaces[i];
    }
    _nSurfaces = n;
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _surfaces);
    }
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points A list of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Not null
   * @param surfaces A list of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param isSorted Is the intersection point data sorted
   */
  @SuppressWarnings("unchecked")
  public InterpolatedFromSurfacesDoublesCube(final Plane plane, final List<Double> points, final List<Surface<Double, Double, Double>> surfaces,
      final Interpolator1D interpolator, final boolean isSorted) {
    super();
    Validate.notNull(plane, "plane");
    Validate.notNull(points, "points");
    Validate.notNull(surfaces, "surfaces");
    final int n = points.size();
    Validate.isTrue(n > 0);
    Validate.isTrue(n == surfaces.size());
    Validate.noNullElements(points);
    Validate.noNullElements(surfaces);
    Validate.notNull(interpolator, "interpolator");
    _plane = plane;
    _points = new double[n];
    _surfaces = new Surface[n];
    for (int i = 0; i < n; i++) {
      _points[i] = points.get(i);
      _surfaces[i] = surfaces.get(i);
    }
    _nSurfaces = n;
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _surfaces);
    }
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param surfaces A map of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis) to surfaces. Not null
   * @param interpolator The interpolator
   * @param isSorted Is the intersection point data sorted
   */
  @SuppressWarnings("unchecked")
  public InterpolatedFromSurfacesDoublesCube(final Plane plane, final Map<Double, Surface<Double, Double, Double>> surfaces,
      final Interpolator1D interpolator, final boolean isSorted) {
    super();
    Validate.notNull(plane, "plane");
    Validate.notNull(surfaces, "surfaces");
    final int n = surfaces.size();
    Validate.isTrue(n > 0);
    Validate.notNull(interpolator, "interpolator");
    _plane = plane;
    _points = new double[n];
    _surfaces = new Surface[n];
    int i = 0;
    for (final Map.Entry<Double, Surface<Double, Double, Double>> entry : surfaces.entrySet()) {
      Validate.notNull(entry.getKey(), "point " + i);
      Validate.notNull(entry.getValue(), "surface " + i);
      _points[i] = entry.getKey();
      _surfaces[i] = entry.getValue();
      i++;
    }
    _nSurfaces = n;
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _surfaces);
    }
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points An array of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Not null
   * @param surfaces An array of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param isSorted Is the intersection point data sorted
   * @param name The name of the cube
   */
  public InterpolatedFromSurfacesDoublesCube(final Plane plane, final double[] points, final Surface<Double, Double, Double>[] surfaces,
      final Interpolator1D interpolator, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(plane, "plane");
    Validate.notNull(points, "points");
    Validate.notNull(surfaces, "surfaces");
    final int n = points.length;
    Validate.isTrue(n > 0);
    Validate.isTrue(n == surfaces.length);
    Validate.noNullElements(surfaces, "surfaces");
    Validate.notNull(interpolator, "interpolator");
    _plane = plane;
    _points = Arrays.copyOf(points, n);
    _surfaces = Arrays.copyOf(surfaces, n);
    _nSurfaces = n;
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _surfaces);
    }
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points An array of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Not null
   * @param surfaces An array of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param isSorted Is the intersection point data sorted
   * @param name The name of the cube
   */
  @SuppressWarnings("unchecked")
  public InterpolatedFromSurfacesDoublesCube(final Plane plane, final Double[] points, final Surface<Double, Double, Double>[] surfaces,
      final Interpolator1D interpolator, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(plane, "plane");
    Validate.notNull(points, "points");
    Validate.notNull(surfaces, "surfaces");
    final int n = points.length;
    Validate.isTrue(n > 0);
    Validate.isTrue(n == surfaces.length);
    Validate.notNull(interpolator, "interpolator");
    _plane = plane;
    _points = new double[n];
    _surfaces = new Surface[n];
    for (int i = 0; i < n; i++) {
      Validate.notNull(points[i], "point " + i);
      Validate.notNull(surfaces[i], "surface " + i);
      _points[i] = points[i];
      _surfaces[i] = surfaces[i];
    }
    _nSurfaces = n;
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _surfaces);
    }
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param points A list of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis). Not null
   * @param surfaces A list of surfaces, not null, must be the same length as the array of points of intersection
   * @param interpolator The interpolator
   * @param isSorted Is the intersection point data sorted
   * @param name The name of the cube
   */
  @SuppressWarnings("unchecked")
  public InterpolatedFromSurfacesDoublesCube(final Plane plane, final List<Double> points, final List<Surface<Double, Double, Double>> surfaces,
      final Interpolator1D interpolator, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(plane, "plane");
    Validate.notNull(points, "points");
    Validate.notNull(surfaces, "surfaces");
    final int n = points.size();
    Validate.isTrue(n > 0);
    Validate.isTrue(n == surfaces.size());
    Validate.noNullElements(points);
    Validate.noNullElements(surfaces);
    Validate.notNull(interpolator, "interpolator");
    _plane = plane;
    _points = new double[n];
    _surfaces = new Surface[n];
    for (int i = 0; i < n; i++) {
      _points[i] = points.get(i);
      _surfaces[i] = surfaces.get(i);
    }
    _nSurfaces = n;
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _surfaces);
    }
  }

  /**
   * @param plane The plane in which the surfaces lie
   * @param surfaces A map of points of intersection of the surfaces on the remaining axis (e.g. if the surfaces are in the <i>x-y</i> plane, the points indicate where
   * the surfaces cross the <i>z</i> axis) to surfaces. Not null
   * @param interpolator The interpolator
   * @param isSorted Is the intersection point data sorted
   * @param name The name of the cube
   */
  @SuppressWarnings("unchecked")
  public InterpolatedFromSurfacesDoublesCube(final Plane plane, final Map<Double, Surface<Double, Double, Double>> surfaces,
      final Interpolator1D interpolator, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(plane, "plane");
    Validate.notNull(surfaces, "surfaces");
    final int n = surfaces.size();
    Validate.isTrue(n > 0);
    Validate.notNull(interpolator, "interpolator");
    _plane = plane;
    _points = new double[n];
    _surfaces = new Surface[n];
    int i = 0;
    for (final Map.Entry<Double, Surface<Double, Double, Double>> entry : surfaces.entrySet()) {
      Validate.notNull(entry.getKey(), "point " + i);
      Validate.notNull(entry.getValue(), "surface " + i);
      _points[i] = entry.getKey();
      _surfaces[i] = entry.getValue();
      i++;
    }
    _nSurfaces = n;
    _interpolator = interpolator;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_points, _surfaces);
    }
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
  public Double[] getValues() {
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

  @Override
  public Double getValue(final Double x, final Double y, final Double z) {
    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    Validate.notNull(z, "z");
    final double[] values = new double[_nSurfaces];
    int i = 0;
    double x1, y1, z1;
    switch (_plane) {
      case XY:
        x1 = x;
        y1 = y;
        z1 = z;
        break;
      case ZX:
        x1 = x;
        y1 = z;
        z1 = y;
        break;
      case YZ:
        x1 = y;
        y1 = z;
        z1 = x;
        break;
      default:
        throw new IllegalArgumentException("Cannot handle type " + _plane);
    }
    final int index = Arrays.binarySearch(_points, z1);
    if (index < 0) {
      for (final Surface<Double, Double, Double> surface : _surfaces) {
        values[i++] = surface.getZValue(x1, y1);
      }
      return InterpolatedDoublesCurve.fromSorted(_points, values, _interpolator).getYValue(z1);
    }
    return _surfaces[index].getZValue(x1, y1);
  }

  @Override
  public Double getValue(final Triple<Double, Double, Double> xyz) {
    Validate.notNull(xyz, "xyz");
    return getValue(xyz.getFirst(), xyz.getSecond(), xyz.getThird());
  }


  /**
   * @return The plane of the surfaces
   */
  public Plane getPlane() {
    return _plane;
  }

  /**
   * @return The intersection point of the surfaces
   */
  public double[] getPoints() {
    return _points;
  }

  /**
   * @return The surfaces
   */
  public Surface<Double, Double, Double>[] getSurfaces() {
    return _surfaces;
  }

  /**
   * @return The interpolator
   */
  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _interpolator.hashCode();
    result = prime * result + _plane.hashCode();
    result = prime * result + Arrays.hashCode(_points);
    result = prime * result + Arrays.hashCode(_surfaces);
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
    final InterpolatedFromSurfacesDoublesCube other = (InterpolatedFromSurfacesDoublesCube) obj;
    if (!Arrays.equals(_surfaces, other._surfaces)) {
      return false;
    }
    if (!Arrays.equals(_points, other._points)) {
      return false;
    }
    if (!ObjectUtils.equals(_interpolator, other._interpolator)) {
      return false;
    }
    return _plane == other._plane;
  }

}
