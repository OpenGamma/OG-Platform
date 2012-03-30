/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function;

/**
 * Shifts a {@link FunctionalDoublesCurve}. Only parallel shifts of the curve are supported.
 */
public class FunctionalCurveShiftFunction implements CurveShiftFunction<FunctionalDoublesCurve> {

  /**
   * {@inheritDoc}
   */
  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double shift) {
    Validate.notNull(curve, "curve");
    return evaluate(curve, shift, "PARALLEL_SHIFT_" + curve.getName());
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double x, final double shift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double x, final double shift, final String newName) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double[] xShift, final double[] yShift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public FunctionalDoublesCurve evaluate(final FunctionalDoublesCurve curve, final double[] xShift, final double[] yShift, final String newName) {
    throw new UnsupportedOperationException();
  }

}
