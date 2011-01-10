/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static com.opengamma.math.UtilFunctions.square;
import static com.opengamma.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Standard version of powell's method. It is a good general purpose minimiser if you don't have the analytic form of the functions gradient 
 */
public class ConjugateDirectionVectorMinimizer implements VectorMinimizer {

  private static final double SMALL = Double.MIN_NORMAL;
  private final double _eps;
  private final int _maxInterations;
  private final LineSearch _lineSearch;

  public ConjugateDirectionVectorMinimizer(final ScalarMinimizer minimizer) {
    ArgumentChecker.notNull(minimizer, "minimizer");
    _lineSearch = new LineSearch(minimizer);
    _eps = 1e-8;
    _maxInterations = 100;
  }

  public ConjugateDirectionVectorMinimizer(final ScalarMinimizer minimizer, final double tolerance, final int maxInterations) {
    ArgumentChecker.notNull(minimizer, "minimizer");
    if (tolerance < SMALL || tolerance > 1.0) {
      throw new IllegalArgumentException("Tolerance must be greater than " + SMALL + " and less than 1.0");
    }
    if (maxInterations < 1) {
      throw new IllegalArgumentException("Need at lest one interation");
    }
    _lineSearch = new LineSearch(minimizer);
    _eps = tolerance;
    _maxInterations = maxInterations;
  }

  @Override
  public DoubleMatrix1D minimize(final Function1D<DoubleMatrix1D, Double> function, final DoubleMatrix1D startPosition) {
    final int n = startPosition.getNumberOfElements();
    final DoubleMatrix1D[] directionSet = getDefaultDirectionSet(n);

    DoubleMatrix1D x0 = startPosition;
    for (int count = 0; count < _maxInterations; count++) {
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
        final double temp = (f1 - f2); // LineSearch should return this
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
      if (extrapValue < startValue
          && (2 * (startValue - 2 * f2 * extrapValue) * square(startValue - f2 - delta)) < (square(startValue
              - extrapValue) * delta)) {
        lambda = _lineSearch.minimise(function, deltaX, x);
        x = (DoubleMatrix1D) OG_ALGEBRA.add(x, OG_ALGEBRA.scale(deltaX, lambda));
        directionSet[indexDelta] = directionSet[n - 1];
        directionSet[n - 1] = deltaX;
      }

      x0 = x;
    }
    throw new MathException("ConjugateDirection Failed to converge after " + _maxInterations + " interations, with a tolerance of "
        + _eps + " Final position reached was " + x0.toString());
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
