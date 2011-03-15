/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import org.apache.commons.lang.Validate;

/**
 * Shifts a {@link SpreadDoublesCurve}. Only parallel shifts are supported.
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

  /**
   * @param curve The curve
   * @param x The <i>x</i>-value of the shift
   * @param shift The shift
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double x, final double shift) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * @param curve The curve
   * @param x The <i>x</i>-value of the shift
   * @param shift The shift
   * @param newName The name of the new curve
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double x, final double shift, final String newName) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param curve The curve
   * @param x The <i>x</i>-values of the shifts
   * @param y The shifts
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double[] x, final double[] y) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param curve The curve
   * @param x The <i>x</i>-values of the shifts
   * @param y The shifts
   * @param newName The name of the new curve
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double[] x, final double[] y, final String newName) {
    throw new UnsupportedOperationException();
  }

}
