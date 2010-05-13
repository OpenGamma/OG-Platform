/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.util.wrapper;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 *   Wraps an OpenGamma class in the analogous Commons Math class.
 *
 */
public class CommonsMathWrapper {

  private CommonsMathWrapper() {
  }

  public static UnivariateRealFunction wrap(final Function1D<Double, Double> f) {
    return new UnivariateRealFunction() {

      @Override
      public double value(final double x) throws FunctionEvaluationException {
        return f.evaluate(x);
      }
    };
  }

  public static RealMatrix wrap(final DoubleMatrix2D x) {
    return new Array2DRowRealMatrix(x.getDataAsPrimitiveArray());
  }

  public static DoubleMatrix2D wrap(final RealMatrix x) {
    return new DoubleMatrix2D(x.getData());
  }

  public static RealVector wrap(final DoubleMatrix1D x) {
    return new ArrayRealVector(x.getDataAsPrimitiveArray());
  }

  public static DoubleMatrix1D wrap(final RealVector x) {
    return new DoubleMatrix1D(x.getData());
  }
}
