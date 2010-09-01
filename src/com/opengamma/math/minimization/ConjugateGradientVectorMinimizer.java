/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static com.opengamma.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.differentiation.ScalarFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ConjugateGradientVectorMinimizer implements VectorMinimizerWithGradient {

  private static final double SMALL = 1e-25;
  private static final double DEFAULT_TOL = 1e-8;
  private static final int DEFAULT_MAX_STEPS = 100;
  private final double _relTol;
  private final double _absTol;
  private final int _maxInterations;
  private final LineSearch _lineSearch;

  public ConjugateGradientVectorMinimizer(final ScalarMinimizer minimizer) {
    this(minimizer, DEFAULT_TOL, DEFAULT_MAX_STEPS);
  }

  public ConjugateGradientVectorMinimizer(final ScalarMinimizer minimizer, double tolerance, int maxInterations) {
    this(minimizer, tolerance, tolerance, maxInterations);
  }

  public ConjugateGradientVectorMinimizer(final ScalarMinimizer minimizer, double relativeTolerance,
      double absoluteTolerance, int maxInterations) {
    ArgumentChecker.notNull(minimizer, "minimizer");
    if (relativeTolerance < 0.0 || relativeTolerance > 1.0) {
      throw new IllegalArgumentException("relativeTolerance must be greater than " + 0.0 + " and less than 1.0");
    }
    if (absoluteTolerance < SMALL) {
      throw new IllegalArgumentException("absoluteTolerance must be greater than " + SMALL);
    }
    if (maxInterations < 1) {
      throw new IllegalArgumentException("Need at lest one interation");
    }
    _lineSearch = new LineSearch(minimizer);
    _relTol = relativeTolerance;
    _absTol = absoluteTolerance;
    _maxInterations = maxInterations;
  }

  @Override
  public DoubleMatrix1D minimize(Function1D<DoubleMatrix1D, Double> function, DoubleMatrix1D startPosition) {
    ScalarFieldFirstOrderDifferentiator diff = new ScalarFieldFirstOrderDifferentiator();
    Function1D<DoubleMatrix1D, DoubleMatrix1D> grad = diff.derivative(function);
    return minimize(function, grad, startPosition);
  }

  @Override
  public DoubleMatrix1D minimize(Function1D<DoubleMatrix1D, Double> function,
      Function1D<DoubleMatrix1D, DoubleMatrix1D> grad, DoubleMatrix1D startPosition) {

    int n = startPosition.getNumberOfElements();
    DoubleMatrix1D x = startPosition;
    DoubleMatrix1D deltaX;
    DoubleMatrix1D g = grad.evaluate(x);
    DoubleMatrix1D d = (DoubleMatrix1D) OG_ALGEBRA.scale(g, -1.0);
    double delta0 = OG_ALGEBRA.getInnerProduct(g, g);
    double deltaOld;
    double deltaNew = delta0;
    double lambda = 0.0;
    int resetCount = 0;

    for (int count = 0; count < _maxInterations; count++, resetCount++) {

      lambda = _lineSearch.minimise(function, d, x);

      //    System.out.println("position:," + x.getEntry(0) + "," + x.getEntry(1));
      deltaX = (DoubleMatrix1D) OG_ALGEBRA.scale(d, lambda);
      x = (DoubleMatrix1D) OG_ALGEBRA.add(x, deltaX);
      DoubleMatrix1D gNew = grad.evaluate(x);
      double deltaMid = OG_ALGEBRA.getInnerProduct(g, gNew);
      g = gNew;
      deltaOld = deltaNew;
      deltaNew = OG_ALGEBRA.getInnerProduct(g, g);

      //      if (x.getEntry(0) > 0.99 && x.getEntry(1) > 0.99) {
      //        System.out.println("position:," + x.getEntry(0) + "," + x.getEntry(1));
      //      }
      if (Math.sqrt(deltaNew) < _relTol * delta0 + _absTol //in practice may never get exactly zero gradient (especially if using finite difference to find it), so if shouldn't be the critical stopping criteria 
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
      double beta = (deltaNew - deltaMid) / deltaOld;

      if (beta < 0 || resetCount == n) {
        d = (DoubleMatrix1D) OG_ALGEBRA.scale(g, -1.0);
        resetCount = 0;
      } else {
        d = (DoubleMatrix1D) OG_ALGEBRA.subtract(OG_ALGEBRA.scale(d, beta), g);
        double sanity = OG_ALGEBRA.getInnerProduct(d, g);
        if (sanity > 0) {
          System.out.println("arse");
        }
      }
    }
    String s = "ConjugateGradient Failed to converge after " + _maxInterations + " interations, with a tolerance of "
        + _relTol + " Final position reached was " + x.toString();
    throw new ConvergenceException(s);
  }
}
