/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * Shifts a {@link NodalDoublesSurface}. If the <i>x-y</i> value(s) of the shift(s) are not in the nodal points of the 
 * original surface, then the surface cannot be shifted.
 */
public class NodalSurfaceMultiplicativeShiftFunction implements SurfaceShiftFunction<NodalDoublesSurface> {

  /**
   * {@inheritDoc}
   */
  @Override
  public NodalDoublesSurface evaluate(final NodalDoublesSurface surface, final double percentage) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, percentage, "CONSTANT_MULTIPLIER_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NodalDoublesSurface evaluate(final NodalDoublesSurface surface, final double percentage, final String newName) {
    Validate.notNull(surface, "surface");
    final double[] xData = surface.getXDataAsPrimitive();
    final double[] yData = surface.getYDataAsPrimitive();
    final double[] zData = surface.getZDataAsPrimitive();
    final int n = zData.length;
    final double[] shiftedZ = new double[n];
    for (int i = 0; i < n; i++) {
      shiftedZ[i] = zData[i] * (1 + percentage);
    }
    return NodalDoublesSurface.from(xData, yData, shiftedZ, newName);
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the point to shift is not a nodal point of the surface 
   */
  @Override
  public NodalDoublesSurface evaluate(final NodalDoublesSurface surface, final double x, final double y, final double percentage) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, x, y, percentage, "SINGLE_MULTIPLIER_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the point to shift is not a nodal point of the surface 
   */
  @Override
  public NodalDoublesSurface evaluate(final NodalDoublesSurface surface, final double x, final double y, final double percentage, final String newName) {
    Validate.notNull(surface, "surface");
    final double[] xData = surface.getXDataAsPrimitive();
    final double[] yData = surface.getYDataAsPrimitive();
    final double[] zData = surface.getZDataAsPrimitive();
    final int n = zData.length;
    for (int i = 0; i < n; i++) {
      if (Double.doubleToLongBits(xData[i]) == Double.doubleToLongBits(x)) {
        if (Double.doubleToLongBits(yData[i]) == Double.doubleToLongBits(y)) {
          final double[] shiftedZ = Arrays.copyOf(zData, n);
          shiftedZ[i] *= 1 + percentage;
          return NodalDoublesSurface.from(xData, yData, shiftedZ, newName);
        }
      }
    }
    throw new IllegalArgumentException("No x-y data in surface for (" + x + ", " + y + ")");
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the points to shift are not nodal points of the surface 
   */
  @Override
  public NodalDoublesSurface evaluate(final NodalDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] zShift) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, xShift, yShift, zShift, "MULTIPLE_MULTIPLIER_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the points to shift are not nodal points of the surface 
   */
  @Override
  public NodalDoublesSurface evaluate(final NodalDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] percentage, final String newName) {
    Validate.notNull(surface, "surface");
    Validate.notNull(xShift, "x shift");
    Validate.notNull(yShift, "y shift");
    Validate.notNull(percentage, "shifts");
    final int m = xShift.length;
    if (m == 0) {
      return NodalDoublesSurface.from(surface.getXDataAsPrimitive(), surface.getYDataAsPrimitive(), surface.getZDataAsPrimitive(), newName);
    }
    Validate.isTrue(m == yShift.length && m == percentage.length);
    final double[] xData = surface.getXDataAsPrimitive();
    final double[] yData = surface.getYDataAsPrimitive();
    final double[] zData = surface.getZDataAsPrimitive();
    final int n = zData.length;
    final double[] shiftedZ = Arrays.copyOf(zData, n);
    for (int i = 0; i < xShift.length; i++) {
      final double x = xShift[i];
      final List<Integer> indices = new ArrayList<Integer>();
      for (int j = 0; j < n; j++) {
        if (Double.doubleToLongBits(xData[j]) == Double.doubleToLongBits(x)) {
          indices.add(j);
        }
      }
      if (indices.isEmpty()) {
        throw new IllegalArgumentException("No x data in surface for value " + x);
      }
      boolean foundValue = false;
      for (final int index : indices) {
        if (Double.doubleToLongBits(yData[index]) == Double.doubleToLongBits(yShift[i])) {
          shiftedZ[index] *= 1 + percentage[i];
          foundValue = true;
        }
      }
      if (!foundValue) {
        throw new IllegalArgumentException("No x-y data in surface for (" + x + ", " + yShift[i] + ")");
      }
    }
    return NodalDoublesSurface.from(xData, yData, shiftedZ, newName);
  }

}
