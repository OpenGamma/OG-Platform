/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import org.apache.commons.lang.Validate;

/**
 * Shifts a {@link ConstantDoublesCurve}. Only parallel shifts of the curve are supported - the other methods would result in a curve that was
 * not constant in <i>y</i>.
 */
public class ConstantCurveShiftFunction implements CurveShiftFunction<ConstantDoublesCurve> {

  /**
   * {@inheritDoc}
   */
  @Override
  public ConstantDoublesCurve evaluate(final ConstantDoublesCurve curve, final double shift) {
    Validate.notNull(curve, "curve");
    return evaluate(curve, shift, "PARALLEL_SHIFT_" + curve.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConstantDoublesCurve evaluate(final ConstantDoublesCurve curve, final double shift, final String newName) {
    Validate.notNull(curve, "curve");
    final double y = curve.getYData()[0];
    return ConstantDoublesCurve.from(y + shift, newName);
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesCurve evaluate(final ConstantDoublesCurve curve, final double x, final double shift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesCurve evaluate(final ConstantDoublesCurve curve, final double x, final double shift, final String newName) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesCurve evaluate(final ConstantDoublesCurve curve, final double[] xShift, final double[] yShift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesCurve evaluate(final ConstantDoublesCurve curve, final double[] xShift, final double[] yShift, final String newName) {
    throw new UnsupportedOperationException();
  }
}
