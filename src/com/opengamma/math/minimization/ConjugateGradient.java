/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static com.opengamma.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ConjugateGradient implements MinimizerNDWithFirstDerivative {

  private static final double SMALL = Double.MIN_NORMAL;
  private final double _eps;
  private final int _maxInterations;
  private final LineSearch _lineSearch;

  public ConjugateGradient(final Minimizer1D minimizer) {
    ArgumentChecker.notNull(minimizer, "minimizer");
    _lineSearch = new LineSearch(minimizer);
    _eps = 1e-8;
    _maxInterations = 100;
  }

  public ConjugateGradient(final Minimizer1D minimizer, double tolerance, int maxInterations) {
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
  public DoubleMatrix1D minimize(Function1D<DoubleMatrix1D, Double> function, Function1D<DoubleMatrix1D, DoubleMatrix1D> grad, DoubleMatrix1D startPosition) {

    int n = startPosition.getNumberOfElements();
    DoubleMatrix1D x = startPosition;
    DoubleMatrix1D g = grad.evaluate(x);
    DoubleMatrix1D d = (DoubleMatrix1D) OG_ALGEBRA.scale(g, -1.0);
    double delta0 = OG_ALGEBRA.getInnerProduct(g, g);
    double deltaOld;
    double deltaNew = delta0;
    double fOld = function.evaluate(x);
    double fNew;
    int resetCount = 0;

    for (int count = 0; count < _maxInterations; count++, resetCount++) {
      double lambda = _lineSearch.minimise(function, d, x);
      x = (DoubleMatrix1D) OG_ALGEBRA.add(x, OG_ALGEBRA.scale(d, lambda));
      fNew = function.evaluate(x);
      DoubleMatrix1D gNew = grad.evaluate(x);
      double deltaMid = OG_ALGEBRA.getInnerProduct(g, gNew);
      g = gNew;
      deltaOld = deltaNew;
      deltaNew = OG_ALGEBRA.getInnerProduct(g, g);

      if (deltaNew < _eps * _eps * delta0 + SMALL && (fOld - fNew) < _eps * (Math.abs(fOld) + Math.abs(fNew)) / 2 + SMALL) {
        return x;
      }
      fOld = fNew;
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
    String s = "ConjugateGradient Failed to converge after " + _maxInterations + " interations, with a tolerance of " + _eps + " Final position reached was " + x.toString();
    throw new ConvergenceException(s);
  }

}
