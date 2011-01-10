/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class ConstantCurveShiftFunction implements CurveShiftFunction<ConstantDoublesCurve> {

  @Override
  public ConstantDoublesCurve evaluate(final ConstantDoublesCurve curve, final double shift) {
    Validate.notNull(curve, "curve");
    return evaluate(curve, shift, "PARALLEL_SHIFT_" + curve.getName());
  }

  @Override
  public ConstantDoublesCurve evaluate(final ConstantDoublesCurve curve, final double shift, final String newName) {
    Validate.notNull(curve, "curve");
    final double y = curve.getYData()[0];
    return ConstantDoublesCurve.from(y + shift, newName);
  }

  @Override
  public ConstantDoublesCurve evaluate(final ConstantDoublesCurve curve, final double x, final double shift) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ConstantDoublesCurve evaluate(final ConstantDoublesCurve curve, final double x, final double shift, final String newName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ConstantDoublesCurve evaluate(final ConstantDoublesCurve curve, final double[] xShift, final double[] yShift) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ConstantDoublesCurve evaluate(final ConstantDoublesCurve curve, final double[] xShift, final double[] yShift, final String newName) {
    throw new UnsupportedOperationException();
  }
}
