/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.util.ArgumentChecker;

/**
 * Shifts a {@link NodalDoublesCube}. If the <i>x-y</i> value(s) of the shift(s) are not in the nodal points of the
 * original cube, then the cube cannot be shifted.
 */
public class NodalCubeAdditiveShiftFunction implements CubeShiftFunction<NodalDoublesCube> {

  /**
   * {@inheritDoc}
   */
  @Override
  public NodalDoublesCube evaluate(final NodalDoublesCube cube, final double shift) {
    ArgumentChecker.notNull(cube, "cube");
    return evaluate(cube, shift, "PARALLEL_SHIFT_" + cube.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public NodalDoublesCube evaluate(final NodalDoublesCube cube, final double shift, final String newName) {
    ArgumentChecker.notNull(cube, "cube");
    final double[] xData = cube.getXDataAsPrimitive();
    final double[] yData = cube.getYDataAsPrimitive();
    final double[] zData = cube.getZDataAsPrimitive();
    final double[] vData = cube.getValuesAsPrimitive();
    final int n = vData.length;
    final double[] shiftedV = new double[n];
    for (int i = 0; i < n; i++) {
      shiftedV[i] = vData[i] + shift;
    }
    return NodalDoublesCube.from(xData, yData, zData, shiftedV, newName);
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the point to shift is not a nodal point of the cube
   */
  @Override
  public NodalDoublesCube evaluate(final NodalDoublesCube cube, final double x, final double y, final double z, final double shift) {
    ArgumentChecker.notNull(cube, "cube");
    return evaluate(cube, x, y, z, shift, "SINGLE_SHIFT_" + cube.getName());
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the point to shift is not a nodal point of the cube
   */
  @Override
  public NodalDoublesCube evaluate(final NodalDoublesCube cube, final double x, final double y, final double z, final double shift, final String newName) {
    ArgumentChecker.notNull(cube, "cube");
    final double[] xData = cube.getXDataAsPrimitive();
    final double[] yData = cube.getYDataAsPrimitive();
    final double[] zData = cube.getZDataAsPrimitive();
    final double[] vData = cube.getValuesAsPrimitive();
    final int n = vData.length;
    for (int i = 0; i < n; i++) {
      if (Double.doubleToLongBits(xData[i]) == Double.doubleToLongBits(x) && Double.doubleToLongBits(yData[i]) == Double.doubleToLongBits(y) &&
          Double.doubleToLongBits(zData[i]) == Double.doubleToLongBits(z)) {
        final double[] shiftedV = Arrays.copyOf(vData, n);
        shiftedV[i] += shift;
        return NodalDoublesCube.from(xData, yData, zData, shiftedV, newName);
      }
    }
    throw new IllegalArgumentException("No x-y-z data in cube for (" + x + ", " + y + ", " + z + ")");
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the points to shift are not nodal points of the cube
   */
  @Override
  public NodalDoublesCube evaluate(final NodalDoublesCube cube, final double[] xShift, final double[] yShift, final double[] zShift, double[] shift) {
    ArgumentChecker.notNull(cube, "cube");
    return evaluate(cube, xShift, yShift, zShift, shift, "MULTIPLE_SHIFT_" + cube.getName());
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException If the points to shift are not nodal points of the cube
   */
  @Override
  public NodalDoublesCube evaluate(final NodalDoublesCube cube, final double[] xShift, final double[] yShift, final double[] zShift, final double[] shift, final String newName) {
    ArgumentChecker.notNull(cube, "cube");
    ArgumentChecker.notNull(xShift, "x shift");
    ArgumentChecker.notNull(yShift, "y shift");
    ArgumentChecker.notNull(zShift, "z shift");
    ArgumentChecker.notNull(shift, "shifts");
    final int m = xShift.length;
    if (m == 0) {
      return NodalDoublesCube.from(cube.getXDataAsPrimitive(), cube.getYDataAsPrimitive(), cube.getZDataAsPrimitive(), cube.getValuesAsPrimitive(), newName);
    }
    ArgumentChecker.isTrue(m == yShift.length && m == zShift.length && m == shift.length,
        "number of shifts {} must be equal to number of x shift positions {}, y shift positions {} and z shift positions {}", shift.length, m, yShift.length, zShift.length);
    final double[] xData = cube.getXDataAsPrimitive();
    final double[] yData = cube.getYDataAsPrimitive();
    final double[] zData = cube.getZDataAsPrimitive();
    final double[] vData = cube.getValuesAsPrimitive();
    final int n = vData.length;
    final double[] shiftedV = Arrays.copyOf(vData, n);
    for (int i = 0; i < xShift.length; i++) {
      final double x = xShift[i];
      final List<Integer> indices = new ArrayList<>();
      for (int j = 0; j < n; j++) {
        if (Double.doubleToLongBits(xData[j]) == Double.doubleToLongBits(x)) {
          indices.add(j);
        }
      }
      if (indices.isEmpty()) {
        throw new IllegalArgumentException("No x data in cube for value " + x);
      }
      boolean foundValue = false;
      for (final int index : indices) {
        if (Double.doubleToLongBits(yData[index]) == Double.doubleToLongBits(yShift[i]) && Double.doubleToLongBits(zData[index]) == Double.doubleToLongBits(zShift[i])) {
          shiftedV[index] += shift[i];
          foundValue = true;
        }
      }
      if (!foundValue) {
        throw new IllegalArgumentException("No x-y-z data in cube for (" + x + ", " + yShift[i] + ", " + zShift[i] + ")");
      }
    }
    return NodalDoublesCube.from(xData, yData, zData, shiftedV, newName);
  }

}
