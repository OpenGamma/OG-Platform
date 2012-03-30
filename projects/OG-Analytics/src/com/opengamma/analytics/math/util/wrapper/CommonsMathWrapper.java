/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.util.wrapper;

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

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.FunctionND;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.number.ComplexNumber;

/**
 * Utility class for converting OpenGamma mathematical objects into <a href="http://commons.apache.org/math/api-2.1/index.html">Commons</a> objects and vice versa.
 */
public final class CommonsMathWrapper {

  private CommonsMathWrapper() {
  }

  /**
   * @param f An OG 1-D function mapping doubles onto doubles, not null 
   * @return A Commons univariate real function
   */
  public static UnivariateRealFunction wrapUnivariate(final Function1D<Double, Double> f) {
    Validate.notNull(f);
    return new UnivariateRealFunction() {

      @Override
      public double value(final double x) {
        return f.evaluate(x);
      }
    };
  }

  /**
   * @param f An OG 1-D function mapping vectors of doubles onto doubles, not null
   * @return A Commons multivariate real function
   */
  public static MultivariateRealFunction wrapMultivariate(final Function1D<DoubleMatrix1D, Double> f) {
    Validate.notNull(f);
    return new MultivariateRealFunction() {

      @Override
      public double value(final double[] point) throws FunctionEvaluationException, IllegalArgumentException {

        return f.evaluate(new DoubleMatrix1D(point));
      }
    };
  }

  /**
   * @param f An OG n-D function mapping doubles onto doubles, not null
   * @return A Commons multivariate real function
   */
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

  /**
   * @param x An OG 2-D matrix of doubles, not null
   * @return A Commons matrix
   */
  public static RealMatrix wrap(final DoubleMatrix2D x) {
    Validate.notNull(x);
    return new Array2DRowRealMatrix(x.getData());
  }

  /**
   * @param x An OG 1-D vector of doubles, not null
   * @return A Commons matrix 
   */
  public static RealMatrix wrapAsMatrix(final DoubleMatrix1D x) {
    Validate.notNull(x);
    final int n = x.getNumberOfElements();
    final double[][] y = new double[n][1];
    for (int i = 0; i < n; i++) {
      y[i][0] = x.getEntry(i);
    }
    return new Array2DRowRealMatrix(x.getData());
  }

  /**
   * @param x A Commons matrix, not null
   * @return An OG 2-D matrix of doubles
   */
  public static DoubleMatrix2D unwrap(final RealMatrix x) {
    Validate.notNull(x);
    return new DoubleMatrix2D(x.getData());
  }

  /**
   * @param x An OG vector of doubles, not null
   * @return A Commons vector
   */
  public static RealVector wrap(final DoubleMatrix1D x) {
    Validate.notNull(x);
    return new ArrayRealVector(x.getData());
  }

  /**
   * @param x A Commons vector, not null
   * @return An OG 1-D matrix of doubles
   */
  public static DoubleMatrix1D unwrap(final RealVector x) {
    Validate.notNull(x);
    return new DoubleMatrix1D(x.getData());
  }

  /**
   * @param z An OG complex number, not null
   * @return A Commons complex number
   */
  public static Complex wrap(final ComplexNumber z) {
    Validate.notNull(z);
    return new Complex(z.getReal(), z.getImaginary());
  }

  /**
   * @param lagrange A Commons polynomial in Lagrange form, not null
   * @return An OG 1-D function mapping doubles to doubles
   */
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

  /**
   * @param x A Commons pair of <i>(x, f(x))</i>, not null
   * @return A matrix of double with the <i>x</i> as the first element and <i>f(x)</i> the second
   */
  public static double[] unwrap(final RealPointValuePair x) {
    Validate.notNull(x);
    return x.getPoint();
  }

  /**
   * @param f An OG 1-D function mapping doubles to doubles, not null
   * @return A Commons differentiable univariate real function
   */
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
