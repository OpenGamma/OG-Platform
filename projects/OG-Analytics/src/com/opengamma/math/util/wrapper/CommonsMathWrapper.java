/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.util.wrapper;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableUnivariateRealFunction;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.RealPointValuePair;

import com.opengamma.math.MathException;
import com.opengamma.math.function.DoubleFunction1D;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.FunctionND;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 *   Wraps an OpenGamma class in the analogous Commons Math class.
 *
 */
public final class CommonsMathWrapper {

  private CommonsMathWrapper() {
  }

  public static UnivariateRealFunction wrapUnivariate(final Function1D<Double, Double> f) {
    Validate.notNull(f);
    return new UnivariateRealFunction() {

      @Override
      public double value(final double x) {
        return f.evaluate(x);
      }
    };
  }

  public static MultivariateRealFunction wrapMultivariate(final Function1D<DoubleMatrix1D, Double> f) {
    Validate.notNull(f);
    return new MultivariateRealFunction() {

      @Override
      public double value(final double[] point) throws FunctionEvaluationException, IllegalArgumentException {

        return f.evaluate(new DoubleMatrix1D(point));
      }
    };
  }

  public static MultivariateRealFunction wrap(final FunctionND<Double, Double> f) {
    Validate.notNull(f);
    return new MultivariateRealFunction() {

      @Override
      public double value(final double[] point) throws FunctionEvaluationException, IllegalArgumentException {
        final int n = point.length;
        final Double[] coordinate = new Double[n];
        for (int i = 0; i < n; i++) {
          coordinate[i] = point[i];
        }
        return f.evaluate(coordinate);
      }
    };
  }

  public static RealMatrix wrap(final DoubleMatrix2D x) {
    Validate.notNull(x);
    return new Array2DRowRealMatrix(x.getData());
  }

  public static RealMatrix wrapAsMatrix(final DoubleMatrix1D x) {
    Validate.notNull(x);
    final int n = x.getNumberOfElements();
    final double[][] y = new double[n][1];
    for (int i = 0; i < n; i++) {
      y[i][0] = x.getEntry(i);
    }
    return new Array2DRowRealMatrix(x.getData());
  }

  public static DoubleMatrix2D unwrap(final RealMatrix x) {
    Validate.notNull(x);
    return new DoubleMatrix2D(x.getData());
  }

  public static RealVector wrap(final DoubleMatrix1D x) {
    Validate.notNull(x);
    return new ArrayRealVector(x.getData());
  }

  public static DoubleMatrix1D unwrap(final RealVector x) {
    Validate.notNull(x);
    return new DoubleMatrix1D(x.getData());
  }

  public static Complex wrap(final ComplexNumber z) {
    Validate.notNull(z);
    return new Complex(z.getReal(), z.getImaginary());
  }

  public static Function1D<Double, Double> unwrap(final PolynomialFunctionLagrangeForm lagrange) {
    Validate.notNull(lagrange);
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        try {
          return lagrange.value(x);
        } catch (final org.apache.commons.math.MathException e) {
          throw new MathException(e);
        }
      }

    };
  }

  public static double[] unwrap(final RealPointValuePair x) {
    Validate.notNull(x);
    return x.getPoint();
  }

  public static DifferentiableUnivariateRealFunction wrapDifferentiable(final DoubleFunction1D f) {
    Validate.notNull(f);
    return new DifferentiableUnivariateRealFunction() {

      @Override
      public double value(final double x) throws FunctionEvaluationException {
        return f.evaluate(x);
      }

      @Override
      public UnivariateRealFunction derivative() {
        return wrapUnivariate(f.derivative());
      }
    };
  }
}
