/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import static com.opengamma.analytics.math.FunctionUtils.square;
import static com.opengamma.analytics.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * Standard version of Powell's method. It is intended to be used when an analytic function for the gradient is not available.
 * This implementation is taken from <i>"An Introduction to the Conjugate Gradient Method Without the Agonizing Pain", Shewchuk</i>.
 * 
 */
public class ConjugateDirectionVectorMinimizer implements Minimizer<Function1D<DoubleMatrix1D, Double>, DoubleMatrix1D> {

  private static final double SMALL = Double.MIN_NORMAL;
  private final double _eps;
  private final int _maxIterations;
  private final LineSearch _lineSearch;

  /**
   * Constructs the object with default values for tolerance (10<sup>-8</sup>) and number of iterations (100).
   * @param minimizer The minimizer, not null
   */
  public ConjugateDirectionVectorMinimizer(final ScalarMinimizer minimizer) {
    this(minimizer, 1e-8, 100);
  }

  /**
   * @param minimizer The minimizer, not null
   * @param tolerance The tolerance, must be greater than the minimum value of a double and less than one
   * @param maxIterations The maximum number of iterations
   */
  public ConjugateDirectionVectorMinimizer(final ScalarMinimizer minimizer, final double tolerance, final int maxIterations) {
    Validate.notNull(minimizer, "minimizer");
    Validate.isTrue(tolerance > SMALL && tolerance < 1, "Tolerance must be greater than " + SMALL + " and less than 1.0");
    Validate.isTrue(maxIterations >= 1, "Need at least one iteration");
    _lineSearch = new LineSearch(minimizer);
    _eps = tolerance;
    _maxIterations = maxIterations;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix1D minimize(final Function1D<DoubleMatrix1D, Double> function, final DoubleMatrix1D startPosition) {
    Validate.notNull(function, "function");
    Validate.notNull(startPosition, "start position");
    final int n = startPosition.getNumberOfElements();
    final DoubleMatrix1D[] directionSet = getDefaultDirectionSet(n);

    DoubleMatrix1D x0 = startPosition;
    for (int count = 0; count < _maxIterations; count++) {
      double delta = 0.0;
      int indexDelta = 0;
      final double startValue = function.evaluate(x0);
      double f1 = startValue;
      double f2 = 0;
      double lambda = 0.0;

      DoubleMatrix1D x = x0;
      for (int i = 0; i < n; i++) {
        final DoubleMatrix1D direction = directionSet[i];
        lambda = _lineSearch.minimise(function, direction, x);
        x = (DoubleMatrix1D) OG_ALGEBRA.add(x, OG_ALGEBRA.scale(direction, lambda));
        f2 = function.evaluate(x);
        final double temp = (f1 - f2); //TODO LineSearch should return this
        if (temp > delta) {
          delta = temp;
          indexDelta = i;
        }
        f1 = f2;
      }

      if ((startValue - f2) < _eps * (Math.abs(startValue) + Math.abs(f2)) / 2.0 + SMALL) {
        return x;
      }

      final DoubleMatrix1D deltaX = (DoubleMatrix1D) OG_ALGEBRA.subtract(x, x0);
      final DoubleMatrix1D extrapolatedPoint = (DoubleMatrix1D) OG_ALGEBRA.add(x, deltaX);

      final double extrapValue = function.evaluate(extrapolatedPoint);
      // Powell's condition for updating the direction set
      if (extrapValue < startValue && (2 * (startValue - 2 * f2 * extrapValue) * square(startValue - f2 - delta)) < (square(startValue - extrapValue) * delta)) {
        lambda = _lineSearch.minimise(function, deltaX, x);
        x = (DoubleMatrix1D) OG_ALGEBRA.add(x, OG_ALGEBRA.scale(deltaX, lambda));
        directionSet[indexDelta] = directionSet[n - 1];
        directionSet[n - 1] = deltaX;
      }

      x0 = x;
    }
    throw new MathException("ConjugateDirection failed to converge after " + _maxIterations + " iterations, with a tolerance of " + _eps + ". Final position reached was " + x0.toString());
  }

  DoubleMatrix1D[] getDefaultDirectionSet(final int dim) {
    final DoubleMatrix1D[] res = new DoubleMatrix1D[dim];
    for (int i = 0; i < dim; i++) {
      final double[] temp = new double[dim];
      temp[i] = 1.0;
      res[i] = new DoubleMatrix1D(temp);
    }
    return res;
  }

}
