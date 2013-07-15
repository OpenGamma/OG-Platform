/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.util.ArgumentChecker;

/**
 * Shifts an {@link InterpolatedDoublesSurface}. If the <i>(x, y)</i> value(s) of the shift(s) are not in the nodal points of the
 * original surface, they are added (with shift) to the nodal points of the new surface.
 */
public class InterpolatedSurfaceAdditiveShiftFunction implements SurfaceShiftFunction<InterpolatedDoublesSurface> {

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesSurface evaluate(final InterpolatedDoublesSurface surface, final double shift) {
    ArgumentChecker.notNull(surface, "surface");
    return evaluate(surface, shift, "PARALLEL_SHIFT_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesSurface evaluate(final InterpolatedDoublesSurface surface, final double shift, final String newName) {
    ArgumentChecker.notNull(surface, "surface");
    final double[] xData = surface.getXDataAsPrimitive();
    final double[] yData = surface.getYDataAsPrimitive();
    final int n = xData.length;
    final double[] shiftedZ = Arrays.copyOf(surface.getZDataAsPrimitive(), n);
    for (int i = 0; i < n; i++) {
      shiftedZ[i] += shift;
    }
    return InterpolatedDoublesSurface.from(xData, yData, shiftedZ, surface.getInterpolator(), newName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesSurface evaluate(final InterpolatedDoublesSurface surface, final double x, final double y, final double shift) {
    ArgumentChecker.notNull(surface, "surface");
    return evaluate(surface, x, y, shift, "SINGLE_SHIFT_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesSurface evaluate(final InterpolatedDoublesSurface surface, final double x, final double y, final double shift, final String newName) {
    ArgumentChecker.notNull(surface, "surface");
    final double[] xData = surface.getXDataAsPrimitive();
    final double[] yData = surface.getYDataAsPrimitive();
    final double[] zData = surface.getZDataAsPrimitive();
    final int n = xData.length;
    for (int i = 0; i < n; i++) {
      if (Double.doubleToLongBits(xData[i]) == Double.doubleToLongBits(x)) {
        if (Double.doubleToLongBits(yData[i]) == Double.doubleToLongBits(y)) {
          final double[] shiftedZ = Arrays.copyOf(zData, n);
          shiftedZ[i] += shift;
          return InterpolatedDoublesSurface.from(xData, yData, shiftedZ, surface.getInterpolator(), newName);
        }
      }
    }
    final double[] newX = new double[n + 1];
    final double[] newY = new double[n + 1];
    final double[] newZ = new double[n + 1];
    for (int i = 0; i < n; i++) {
      newX[i] = xData[i];
      newY[i] = yData[i];
      newZ[i] = zData[i];
    }
    newX[n] = x;
    newY[n] = y;
    newZ[n] = surface.getZValue(x, y) + shift;
    return InterpolatedDoublesSurface.from(newX, newY, newZ, surface.getInterpolator(), newName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesSurface evaluate(final InterpolatedDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] shift) {
    ArgumentChecker.notNull(surface, "surface");
    return evaluate(surface, xShift, yShift, shift, "MULTIPLE_SHIFT_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesSurface evaluate(final InterpolatedDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] shift, final String newName) {
    ArgumentChecker.notNull(surface, "surface");
    ArgumentChecker.notNull(xShift, "x shift");
    ArgumentChecker.notNull(yShift, "y shift");
    ArgumentChecker.notNull(shift, "shifts");
    final int n = xShift.length;
    if (n == 0) {
      return InterpolatedDoublesSurface.from(surface.getXDataAsPrimitive(), surface.getYDataAsPrimitive(), surface.getZDataAsPrimitive(), surface.getInterpolator(), newName);
    }
    ArgumentChecker.isTrue(n == yShift.length && n == shift.length, "number of shifts {} must be equal to number of x shift positions {} and y shift positions {}", shift.length, n, yShift.length);
    final Double[] x = surface.getXData();
    final Double[] y = surface.getYData();
    final Double[] z = surface.getZData();
    final List<Double> newX = new ArrayList<>(Arrays.asList(x));
    final List<Double> newY = new ArrayList<>(Arrays.asList(y));
    final List<Double> newZ = new ArrayList<>(Arrays.asList(z));
    final int size = surface.size();
    for (int i = 0; i < n; i++) {
      boolean foundValue = false;
      for (int j = 0; j < size; j++) {
        if (Double.doubleToLongBits(x[j]) == Double.doubleToLongBits(xShift[i]) && Double.doubleToLongBits(y[j]) == Double.doubleToLongBits(yShift[i])) {
          newZ.set(j, z[j] + shift[i]);
          foundValue = true;
        }
      }
      if (!foundValue) {
        newX.add(xShift[i]);
        newY.add(yShift[i]);
        newZ.add(surface.getZValue(xShift[i], yShift[i]) + shift[i]);
      }
    }
    return InterpolatedDoublesSurface.from(newX, newY, newZ, surface.getInterpolator(), newName);
  }
}
