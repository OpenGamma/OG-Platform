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
  private final double _eps;
  private final int _maxInterations;
  private final LineSearch _lineSearch;

  public ConjugateGradientVectorMinimizer(final ScalarMinimizer minimizer) {
    ArgumentChecker.notNull(minimizer, "minimizer");
    _lineSearch = new LineSearch(minimizer);
    _eps = 1e-8;
    _maxInterations = 100;
  }

  public ConjugateGradientVectorMinimizer(final ScalarMinimizer minimizer, double tolerance, int maxInterations) {
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

      if (deltaNew < 1000 * _eps * _eps * delta0 //in practice may never get exactly zero gradient (especially if using finite difference to find it), so if shouldn't be the critical stopping criteria 
          && OG_ALGEBRA.getNorm2(deltaX) < _eps * OG_ALGEBRA.getNorm2(x) + SMALL) {
        boolean flag = true;
        for (int i = 0; i < n; i++) {
          if (Math.abs(deltaX.getEntry(i)) > _eps * Math.abs(x.getEntry(i)) + SMALL) {
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
        + _eps + " Final position reached was " + x.toString();
    throw new ConvergenceException(s);
  }
}
