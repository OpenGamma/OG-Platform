/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;

/**
 * 
 */
public class FunctionalCurveShiftFunction implements CurveShiftFunction<FunctionalDoublesCurve> {

  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double shift) {
    Validate.notNull(curve, "curve");
    return evaluate(curve, shift, "PARALLEL_SHIFT_" + curve.getName());
  }

  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double shift, final String newName) {
    Validate.notNull(curve, "curve");
    final Function<Double, Double> f = curve.getFunction();
    final Function<Double, Double> shiftedFunction = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        return f.evaluate(x) + shift;
      }

    };
    return FunctionalDoublesCurve.from(shiftedFunction, newName);
  }

  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double x, final double shift) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double x, final double shift, final String newName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double[] xShift, final double[] yShift) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double[] xShift, final double[] yShift, final String newName) {
    throw new UnsupportedOperationException();
  }

}
