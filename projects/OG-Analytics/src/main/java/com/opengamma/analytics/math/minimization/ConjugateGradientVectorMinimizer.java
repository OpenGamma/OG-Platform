/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import static com.opengamma.analytics.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.differentiation.ScalarFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * This implementation of the conjugate gradient method is taken from <i>"An Introduction to the Conjugate Gradient Method Without the Agonizing Pain", Shewchuk</i>.
 */
public class ConjugateGradientVectorMinimizer implements MinimizerWithGradient<Function1D<DoubleMatrix1D, Double>, Function1D<DoubleMatrix1D, DoubleMatrix1D>, DoubleMatrix1D> {

  private static final double SMALL = 1e-25;
  private static final double DEFAULT_TOL = 1e-8;
  private static final int DEFAULT_MAX_STEPS = 100;
  private final double _relTol;
  private final double _absTol;
  private final int _maxIterations;
  private final LineSearch _lineSearch;

  /**
   * Constructs the object with default values for relative and absolute tolerance (10<sup>-8</sup>) and the number of iterations (100)
   * @param minimizer The minimizer, not null
   */
  public ConjugateGradientVectorMinimizer(final ScalarMinimizer minimizer) {
    this(minimizer, DEFAULT_TOL, DEFAULT_MAX_STEPS);
  }

  /**
   * Constructs the object with equal relative and absolute values of tolerance.
   * @param minimizer The minimizer, not null
   * @param tolerance The tolerance, greater than 10<sup>-25</sup> and less than one
   * @param maxInterations The number of iterations, greater than one
   */
  public ConjugateGradientVectorMinimizer(final ScalarMinimizer minimizer, final double tolerance, final int maxInterations) {
    this(minimizer, tolerance, tolerance, maxInterations);
  }

  /**
   * @param minimizer The minimizer, not null
   * @param relativeTolerance The relative tolerance, greater than zero and less than one
   * @param absoluteTolerance The absolute tolerance, greater than 10<sup>-25</sup>
   * @param maxIterations The number of iterations, greater than one
   */
  public ConjugateGradientVectorMinimizer(final ScalarMinimizer minimizer, final double relativeTolerance, final double absoluteTolerance, final int maxIterations) {
    Validate.notNull(minimizer, "minimizer");
    Validate.isTrue(relativeTolerance > 0.0 || relativeTolerance < 1.0, "relative tolerance must be greater than 0.0 and less than 1.0");
    Validate.isTrue(absoluteTolerance > SMALL, "absolute tolerance must be greater than " + SMALL);
    Validate.isTrue(maxIterations >= 1, "Need at least one iteration");
    _lineSearch = new LineSearch(minimizer);
    _relTol = relativeTolerance;
    _absTol = absoluteTolerance;
    _maxIterations = maxIterations;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix1D minimize(final Function1D<DoubleMatrix1D, Double> function, final DoubleMatrix1D startPosition) {
    Validate.notNull(function, "function");
    Validate.notNull(startPosition, "start position");
    final ScalarFieldFirstOrderDifferentiator diff = new ScalarFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> grad = diff.differentiate(function);
    return minimize(function, grad, startPosition);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DoubleMatrix1D minimize(final Function1D<DoubleMatrix1D, Double> function, final Function1D<DoubleMatrix1D, DoubleMatrix1D> grad, final DoubleMatrix1D startPosition) {
    Validate.notNull(function, "function");
    Validate.notNull(grad, "grad");
    Validate.notNull(startPosition, "start position");
    final int n = startPosition.getNumberOfElements();
    DoubleMatrix1D x = startPosition;
    DoubleMatrix1D deltaX;
    DoubleMatrix1D g = grad.evaluate(x);
    DoubleMatrix1D d = (DoubleMatrix1D) OG_ALGEBRA.scale(g, -1.0);

    final double delta0 = -OG_ALGEBRA.getInnerProduct(g, d);
    double deltaOld;
    double deltaNew = delta0;
    double lambda = 0.0;
    int resetCount = 0;

    for (int count = 0; count < _maxIterations; count++, resetCount++) {

      lambda = _lineSearch.minimise(function, d, x);

      deltaX = (DoubleMatrix1D) OG_ALGEBRA.scale(d, lambda);
      x = (DoubleMatrix1D) OG_ALGEBRA.add(x, deltaX);
      final DoubleMatrix1D gNew = grad.evaluate(x);
      final double deltaMid = OG_ALGEBRA.getInnerProduct(g, gNew);
      g = gNew;
      deltaOld = deltaNew;

      deltaNew = OG_ALGEBRA.getInnerProduct(g, g);

      if (Math.sqrt(deltaNew) < _relTol * delta0 + _absTol
          // in practice may never get exactly zero gradient (especially if using finite difference to find it), so it shouldn't be the critical stopping criterion
          && OG_ALGEBRA.getNorm2(deltaX) < _relTol * OG_ALGEBRA.getNorm2(x) + _absTol) {

        boolean flag = true;
        for (int i = 0; i < n; i++) {
          if (Math.abs(deltaX.getEntry(i)) > _relTol * Math.abs(x.getEntry(i)) + _absTol) {
            flag = false;
            break;
          }
        }
        if (flag) {
          return x;
        }
      }
      final double beta = (deltaNew - deltaMid) / deltaOld;

      if (beta < 0 || resetCount == n) {
        d = (DoubleMatrix1D) OG_ALGEBRA.scale(g, -1.0);
        resetCount = 0;
      } else {
        d = (DoubleMatrix1D) OG_ALGEBRA.subtract(OG_ALGEBRA.scale(d, beta), g);
        final double sanity = OG_ALGEBRA.getInnerProduct(d, g);
        if (sanity > 0) {
          d = (DoubleMatrix1D) OG_ALGEBRA.scale(g, -1.0);
          resetCount = 0;
        }
      }
    }
    final double value = function.evaluate(x);
    throw new MathException("ConjugateGradient failed to converge after " + _maxIterations + " iterations, with a tolerance of " + _relTol + ". Final value: " + value
        + ". Final position reached was " + x.toString());
  }
}
