/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import com.opengamma.util.ArgumentChecker;

/**
 * Shifts a {@link SpreadDoublesCurve}. Only parallel shifts are supported.
 */
public class SpreadCurveShiftFunction implements CurveShiftFunction<SpreadDoublesCurve> {
  /** An additive curve function */
  private static final CurveSpreadFunction SPREAD_FUNCTION = AddCurveSpreadFunction.getInstance();

  /**
   * {@inheritDoc}
   */
  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double shift) {
    ArgumentChecker.notNull(curve, "curve");
    return evaluate(curve, shift, "PARALLEL_SHIFT_" + curve.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double shift, final String newName) {
    ArgumentChecker.notNull(curve, "curve");
    final int n = curve.getUnderlyingCurves().length;
    final DoublesCurve[] curves = new DoublesCurve[n + 1];
    int i = 0;
    for (final DoublesCurve c : curve.getUnderlyingCurves()) {
      curves[i++] = c;
    }
    curves[n] = ConstantDoublesCurve.from(shift);
    return SpreadDoublesCurve.from(SPREAD_FUNCTION, newName, curves);
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double x, final double shift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double x, final double shift, final String newName) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double[] x, final double[] y) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public SpreadDoublesCurve evaluate(final SpreadDoublesCurve curve, final double[] x, final double[] y, final String newName) {
    throw new UnsupportedOperationException();
  }

}
