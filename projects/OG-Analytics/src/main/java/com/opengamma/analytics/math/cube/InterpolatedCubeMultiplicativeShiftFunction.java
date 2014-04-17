/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * Shifts an {@link InterpolatedDoublesCube}. If the <i>(x, y)</i> value(s) of the shift(s) are not in the nodal points of the
 * original cube, they are added (with shift) to the nodal points of the new cube.
 */
public class InterpolatedCubeMultiplicativeShiftFunction implements CubeShiftFunction<InterpolatedDoublesCube> {

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesCube evaluate(final InterpolatedDoublesCube cube, final double percentage) {
    Validate.notNull(cube, "cube");
    return evaluate(cube, percentage, "CONSTANT_MULTIPLIER_" + cube.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesCube evaluate(final InterpolatedDoublesCube cube, final double percentage, final String newName) {
    Validate.notNull(cube, "cube");
    final double[] xData = cube.getXDataAsPrimitive();
    final double[] yData = cube.getYDataAsPrimitive();
    final double[] zData = cube.getZDataAsPrimitive();
    final int n = xData.length;
    final double[] shiftedV = Arrays.copyOf(cube.getValuesAsPrimitive(), n);
    for (int i = 0; i < n; i++) {
      shiftedV[i] *= 1 + percentage;
    }
    return InterpolatedDoublesCube.from(xData, yData, zData, shiftedV, cube.getInterpolator(), newName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesCube evaluate(final InterpolatedDoublesCube cube, final double x, final double y, final double z, final double percentage) {
    Validate.notNull(cube, "cube");
    return evaluate(cube, x, y, z, percentage, "SINGLE_MULTIPLIER_" + cube.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesCube evaluate(final InterpolatedDoublesCube cube, final double x, final double y, final double z, final double percentage, final String newName) {
    Validate.notNull(cube, "cube");
    final double[] xData = cube.getXDataAsPrimitive();
    final double[] yData = cube.getYDataAsPrimitive();
    final double[] zData = cube.getZDataAsPrimitive();
    final double[] vData = cube.getValuesAsPrimitive();
    final int n = xData.length;
    for (int i = 0; i < n; i++) {
      if (Double.doubleToLongBits(xData[i]) == Double.doubleToLongBits(x)) {
        if (Double.doubleToLongBits(yData[i]) == Double.doubleToLongBits(y)) {
          final double[] shiftedV = Arrays.copyOf(vData, n);
          shiftedV[i] *= 1 + percentage;
          return InterpolatedDoublesCube.from(xData, yData, zData, shiftedV, cube.getInterpolator(), newName);
        }
      }
    }
    final double[] newX = new double[n + 1];
    final double[] newY = new double[n + 1];
    final double[] newZ = new double[n + 1];
    final double[] newV = new double[n + 1];
    for (int i = 0; i < n; i++) {
      newX[i] = xData[i];
      newY[i] = yData[i];
      newZ[i] = zData[i];
      newV[i] = vData[i];
    }
    newX[n] = x;
    newY[n] = y;
    newZ[n] = z;
    newV[n] = cube.getValue(x, y, z) + percentage;
    return InterpolatedDoublesCube.from(newX, newY, newZ, newV, cube.getInterpolator(), newName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesCube evaluate(final InterpolatedDoublesCube cube, final double[] xShift, final double[] yShift, final double[] zShift, final double[] percentage) {
    Validate.notNull(cube, "cube");
    return evaluate(cube, xShift, yShift, zShift, percentage, "MULTIPLE_MULTIPLIER_" + cube.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesCube evaluate(final InterpolatedDoublesCube cube, final double[] xShift, final double[] yShift, final double[] zShift, final double[] percentage, final String newName) {
    Validate.notNull(cube, "cube");
    Validate.notNull(xShift, "x shift");
    Validate.notNull(yShift, "y shift");
    Validate.notNull(zShift, "z shift");
    Validate.notNull(percentage, "shifts");
    final int n = xShift.length;
    if (n == 0) {
      return InterpolatedDoublesCube.from(cube.getXDataAsPrimitive(), cube.getYDataAsPrimitive(), cube.getZDataAsPrimitive(), cube.getValuesAsPrimitive(), cube.getInterpolator(), newName);
    }
    Validate.isTrue(n == yShift.length && n == zShift.length && n == percentage.length);
    final Double[] x = cube.getXData();
    final Double[] y = cube.getYData();
    final Double[] z = cube.getZData();
    final Double[] v = cube.getValues();
    final List<Double> newX = new ArrayList<>(Arrays.asList(x));
    final List<Double> newY = new ArrayList<>(Arrays.asList(y));
    final List<Double> newZ = new ArrayList<>(Arrays.asList(z));
    final List<Double> newV = new ArrayList<>(Arrays.asList(v));
    final int size = cube.size();
    for (int i = 0; i < n; i++) {
      boolean foundValue = false;
      for (int j = 0; j < size; j++) {
        if (Double.doubleToLongBits(x[j]) == Double.doubleToLongBits(xShift[i]) && Double.doubleToLongBits(y[j]) == Double.doubleToLongBits(yShift[i]) &&
            Double.doubleToLongBits(z[j]) == Double.doubleToLongBits(zShift[i])) {
          newV.set(j, v[j] * (1 + percentage[i]));
          foundValue = true;
        }
      }
      if (!foundValue) {
        newX.add(xShift[i]);
        newY.add(yShift[i]);
        newZ.add(zShift[i]);
        newV.add(cube.getValue(xShift[i], yShift[i], zShift[i]) * (1 + percentage[i]));
      }
    }
    return InterpolatedDoublesCube.from(newX, newY, newZ, newV, cube.getInterpolator(), newName);
  }
}
