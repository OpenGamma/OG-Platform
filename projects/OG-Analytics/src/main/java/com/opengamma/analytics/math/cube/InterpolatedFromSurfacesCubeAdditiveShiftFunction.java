/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.Plane;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.analytics.math.surface.SurfaceShiftFunctionFactory;

/**
 * Shifts an {@link InterpolatedFromSurfacesCubeAdditiveShiftFunction}. If an <i>x</i> (<i>y</i>) shift does not coincide with the one of the <i>x</i> (<i>y</i>) values
 * of the intersection of the curves with the axis, an exception is thrown.
 */
public class InterpolatedFromSurfacesCubeAdditiveShiftFunction implements CubeShiftFunction<InterpolatedFromSurfacesDoublesCube> {

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedFromSurfacesDoublesCube evaluate(final InterpolatedFromSurfacesDoublesCube surface, final double shift) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, shift, "PARALLEL_SHIFT_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public InterpolatedFromSurfacesDoublesCube evaluate(final InterpolatedFromSurfacesDoublesCube cube, final double shift, final String newName) {
    Validate.notNull(cube, "cube");
    final double[] points = cube.getPoints();
    final Surface<Double, Double, Double>[] surfaces = cube.getSurfaces();
    final int n = surfaces.length;
    final Surface<Double, Double, Double>[] newSurfaces = new Surface[surfaces.length];
    for (int i = 0; i < n; i++) {
      newSurfaces[i] = SurfaceShiftFunctionFactory.getShiftedSurface(surfaces[i], shift, true);
    }
    return InterpolatedFromSurfacesDoublesCube.from(cube.getPlane(), points, newSurfaces, cube.getInterpolator(), newName);
  }

  /**
   * {@inheritDoc}
   * @throws UnsupportedOperationException If the <i>x</i> (<i>y</i>) position of the shift does not coincide with one of the <i>x</i> (<i>y</i>) intersections 
   * of the curves with the axis
   */

  @Override
  public InterpolatedFromSurfacesDoublesCube evaluate(final InterpolatedFromSurfacesDoublesCube surface, final double x, final double y, final double z, final double shift) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, x, y, z, shift, "SINGLE_SHIFT_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   * @throws UnsupportedOperationException If the <i>x</i> (<i>y</i>) position of the shift does not coincide with one of the <i>x</i> (<i>y</i>) intersections 
   * of the curves with the axis
   */
  @Override
  public InterpolatedFromSurfacesDoublesCube evaluate(final InterpolatedFromSurfacesDoublesCube cube, final double x, final double y, final double z, final double shift, final String newName) {
    Validate.notNull(cube, "cube");
    final double[] points = cube.getPoints();

    if (cube.getPlane() == Plane.ZX) {
      final int index = Arrays.binarySearch(points, y);
      final Surface<Double, Double, Double>[] surfaces = cube.getSurfaces();
      if (index >= 0) {
        final Surface<Double, Double, Double>[] newSurfaces = Arrays.copyOf(cube.getSurfaces(), points.length);
        newSurfaces[index] = SurfaceShiftFunctionFactory.getShiftedSurface(surfaces[index], x, z, shift, true);
        return InterpolatedFromSurfacesDoublesCube.fromSorted(cube.getPlane(), points, newSurfaces, cube.getInterpolator(), newName);
      }
      throw new UnsupportedOperationException("Cannot get shift for y-value not in original list of curves: asked for " + y);
    } else if (cube.getPlane() == Plane.XY) {
      final int index = Arrays.binarySearch(points, z);
      final Surface<Double, Double, Double>[] surfaces = cube.getSurfaces();
      if (index >= 0) {
        final Surface<Double, Double, Double>[] newSurfaces = Arrays.copyOf(cube.getSurfaces(), points.length);
        newSurfaces[index] = SurfaceShiftFunctionFactory.getShiftedSurface(surfaces[index], x, y, shift, true);
        return InterpolatedFromSurfacesDoublesCube.fromSorted(cube.getPlane(), points, newSurfaces, cube.getInterpolator(), newName);
      }
      throw new UnsupportedOperationException("Cannot get shift for z-value not in original list of curves: asked for " + z);
    } else {
      final int index = Arrays.binarySearch(points, x);
      final Surface<Double, Double, Double>[] surfaces = cube.getSurfaces();
      if (index >= 0) {
        final Surface<Double, Double, Double>[] newSurfaces = Arrays.copyOf(cube.getSurfaces(), points.length);
        newSurfaces[index] = SurfaceShiftFunctionFactory.getShiftedSurface(surfaces[index], y, z, shift, true);
        return InterpolatedFromSurfacesDoublesCube.fromSorted(cube.getPlane(), points, newSurfaces, cube.getInterpolator(), newName);
      }
      throw new UnsupportedOperationException("Cannot get shift for x-value not in original list of curves: asked for " + x);
    }

  }

  /**
   * {@inheritDoc}
   * @throws UnsupportedOperationException If the <i>x</i> (<i>y</i>) positions of the shifts do not coincide with one of the <i>x</i> (<i>y</i>) intersections 
   * of the curves with the axis
   */
  @Override
  public InterpolatedFromSurfacesDoublesCube evaluate(final InterpolatedFromSurfacesDoublesCube surface, final double[] xShift, final double[] yShift, final double[] zShift, final double[] shift) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, xShift, yShift, zShift, shift, "MULTIPLE_SHIFT_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   * @throws UnsupportedOperationException If the <i>x</i> (<i>y</i>) positions of the shifts do not coincide with one of the <i>x</i> (<i>y</i>) intersections 
   * of the curves with the axis
   */
  @Override
  public InterpolatedFromSurfacesDoublesCube evaluate(final InterpolatedFromSurfacesDoublesCube cube, final double[] xShift, final double[] yShift, final double[] zShift, final double[] shift,
      final String newName) {
    Validate.notNull(cube, "surface");
    Validate.notNull(xShift, "x shifts");
    Validate.notNull(yShift, "y shifts");
    Validate.notNull(yShift, "z shifts");
    Validate.notNull(shift, "shifts");
    final int n = xShift.length;
    if (n == 0) {
      return InterpolatedFromSurfacesDoublesCube.from(cube.getPlane(), cube.getPoints(), cube.getSurfaces(), cube.getInterpolator(), newName);
    }
    Validate.isTrue(n == yShift.length && n == shift.length);
    final double[] points = cube.getPoints();
    if (cube.getPlane() == Plane.ZX) {
      final Surface<Double, Double, Double>[] newSurfaces = Arrays.copyOf(cube.getSurfaces(), points.length);
      for (int i = 0; i < n; i++) {
        final int index = Arrays.binarySearch(points, yShift[i]);
        boolean foundValue = false;
        if (index >= 0) {
          newSurfaces[index] = SurfaceShiftFunctionFactory.getShiftedSurface(newSurfaces[index], xShift[i], zShift[i], shift[i], true);
          foundValue = true;
        }
        if (!foundValue) {
          throw new UnsupportedOperationException("Cannot get shift for y-value not in original list of curves: asked for " + yShift[i]);
        }
      }
      return InterpolatedFromSurfacesDoublesCube.fromSorted(cube.getPlane(), points, newSurfaces, cube.getInterpolator(), newName);
    } else if (cube.getPlane() == Plane.XY) {
      final Surface<Double, Double, Double>[] newSurfaces = Arrays.copyOf(cube.getSurfaces(), points.length);
      for (int i = 0; i < n; i++) {
        final int index = Arrays.binarySearch(points, zShift[i]);
        boolean foundValue = false;
        if (index >= 0) {
          newSurfaces[index] = SurfaceShiftFunctionFactory.getShiftedSurface(newSurfaces[index], xShift[i], yShift[i], shift[i], true);
          foundValue = true;
        }
        if (!foundValue) {
          throw new UnsupportedOperationException("Cannot get shift for z-value not in original list of curves: asked for " + zShift[i]);
        }
      }
      return InterpolatedFromSurfacesDoublesCube.fromSorted(cube.getPlane(), points, newSurfaces, cube.getInterpolator(), newName);
    } else {
      final Surface<Double, Double, Double>[] newSurfaces = Arrays.copyOf(cube.getSurfaces(), points.length);
      for (int i = 0; i < n; i++) {
        final int index = Arrays.binarySearch(points, xShift[i]);
        boolean foundValue = false;
        if (index >= 0) {
          newSurfaces[index] = SurfaceShiftFunctionFactory.getShiftedSurface(newSurfaces[index], yShift[i], zShift[i], shift[i], true);
          foundValue = true;
        }
        if (!foundValue) {
          throw new UnsupportedOperationException("Cannot get shift for x-value not in original list of curves: asked for " + xShift[i]);
        }
      }
      return InterpolatedFromSurfacesDoublesCube.fromSorted(cube.getPlane(), points, newSurfaces, cube.getInterpolator(), newName);
    }

  }
}
