/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class SpreadCurveShiftFunction implements CurveShiftFunction<SpreadDoublesCurve> {
  private static final CurveSpreadFunction SPREAD_FUNCTION = new AddCurveSpreadFunction();

  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double shift) {
    Validate.notNull(curve, "curve");
    return evaluate(curve, shift, "PARALLEL_SHIFT_" + curve.getName());
  }

  @SuppressWarnings("unchecked")
  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double shift, final String newName) {
    Validate.notNull(curve, "curve");
    final int n = curve.getUnderlyingCurves().length;
    final Curve<Double, Double>[] curves = new Curve[n + 1];
    int i = 0;
    for (final Curve<Double, Double> c : curve.getUnderlyingCurves()) {
      curves[i++] = c;
    }
    curves[n] = ConstantDoublesCurve.from(shift);
    return SpreadDoublesCurve.from(curves, SPREAD_FUNCTION, newName);
  }

  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double x, final double shift) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double x, final double shift, final String newName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double[] x, final double[] y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double[] x, final double[] y, final String newName) {
    throw new UnsupportedOperationException();
  }

}
