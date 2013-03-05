/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.util.ArgumentChecker;

/**
 * Shifts an {@link InterpolatedDoublesCurve}. If the <i>x</i> value(s) of the shift(s) are not in the nodal points of the
 * original curve, they are added (with shift) to the nodal points of the new curve.
 */
public class InterpolatedCurveShiftFunction implements CurveShiftFunction<InterpolatedDoublesCurve> {

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesCurve evaluate(final InterpolatedDoublesCurve curve, final double shift) {
    ArgumentChecker.notNull(curve, "curve");
    return evaluate(curve, shift, "PARALLEL_SHIFT_" + curve.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesCurve evaluate(final InterpolatedDoublesCurve curve, final double shift, final String newName) {
    ArgumentChecker.notNull(curve, "curve");
    final double[] xData = curve.getXDataAsPrimitive();
    final double[] yData = curve.getYDataAsPrimitive();
    final double[] shiftedY = new double[yData.length];
    int i = 0;
    for (final double y : yData) {
      shiftedY[i++] = y + shift;
    }
    return InterpolatedDoublesCurve.fromSorted(xData, shiftedY, curve.getInterpolator(), newName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesCurve evaluate(final InterpolatedDoublesCurve curve, final double x, final double shift) {
    ArgumentChecker.notNull(curve, "curve");
    return evaluate(curve, x, shift, "SINGLE_SHIFT_" + curve.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesCurve evaluate(final InterpolatedDoublesCurve curve, final double x, final double shift, final String newName) {
    ArgumentChecker.notNull(curve, "curve");
    final double[] xData = curve.getXDataAsPrimitive();
    final double[] yData = curve.getYDataAsPrimitive();
    final int n = xData.length;
    final int index = Arrays.binarySearch(xData, x);
    if (index >= 0) {
      final double[] shiftedY = Arrays.copyOf(curve.getYDataAsPrimitive(), n);
      shiftedY[index] += shift;
      return InterpolatedDoublesCurve.fromSorted(xData, shiftedY, curve.getInterpolator(), newName);
    }
    final double[] newX = new double[n + 1];
    final double[] newY = new double[n + 1];
    for (int i = 0; i < n; i++) {
      newX[i] = xData[i];
      newY[i] = yData[i];
    }
    newX[n] = x;
    newY[n] = curve.getYValue(x) + shift;
    return InterpolatedDoublesCurve.from(newX, newY, curve.getInterpolator(), newName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesCurve evaluate(final InterpolatedDoublesCurve curve, final double[] xShift, final double[] yShift) {
    ArgumentChecker.notNull(curve, "curve");
    return evaluate(curve, xShift, yShift, "MULTIPLE_POINT_SHIFT_" + curve.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InterpolatedDoublesCurve evaluate(final InterpolatedDoublesCurve curve, final double[] xShift, final double[] yShift, final String newName) {
    ArgumentChecker.notNull(curve, "curve");
    ArgumentChecker.notNull(xShift, "x shifts");
    ArgumentChecker.notNull(yShift, "y shifts");
    ArgumentChecker.isTrue(xShift.length == yShift.length, "number of x shifts {} must equal number of y shifts {}", xShift.length, yShift.length);
    if (xShift.length == 0) {
      return InterpolatedDoublesCurve.from(curve.getXDataAsPrimitive(), curve.getYDataAsPrimitive(), curve.getInterpolator(), newName);
    }
    final List<Double> newX = new ArrayList<>(Arrays.asList(curve.getXData()));
    final List<Double> newY = new ArrayList<>(Arrays.asList(curve.getYData()));
    for (int i = 0; i < xShift.length; i++) {
      final int index = newX.indexOf(xShift[i]);
      if (index >= 0) {
        newY.set(index, newY.get(index) + yShift[i]);
      } else {
        newX.add(xShift[i]);
        newY.add(curve.getYValue(xShift[i]) + yShift[i]);
      }
    }
    return InterpolatedDoublesCurve.from(newX, newY, curve.getInterpolator(), newName);
  }

}
