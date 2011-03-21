/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.DoubleFunction1D;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.special.GammaFunction;
import com.opengamma.math.function.special.JacobiPolynomialFunction;
import com.opengamma.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class GaussJacobiOrthogonalPolynomialGeneratingFunction implements QuadratureWeightAndAbscissaFunction {
  private static final JacobiPolynomialFunction JACOBI = new JacobiPolynomialFunction();
  private static final NewtonRaphsonSingleRootFinder ROOT_FINDER = new NewtonRaphsonSingleRootFinder(1e-12);
  private static final Function1D<Double, Double> GAMMA_FUNCTION = new GammaFunction();
  private final double _alpha;
  private final double _beta;

  public GaussJacobiOrthogonalPolynomialGeneratingFunction(final double alpha, final double beta) {
    super();
    _alpha = alpha;
    _beta = beta;
  }

  @Override
  public GaussianQuadratureData generate(final int n) {
    Validate.isTrue(n > 0, "n > 0");
    final double alphaBeta = _alpha + _beta;
    double alphaBeta2 = 2 + alphaBeta;
    final Pair<DoubleFunction1D, DoubleFunction1D>[] polynomials = JACOBI.getPolynomialsAndFirstDerivative(n, _alpha, _beta);
    final Pair<DoubleFunction1D, DoubleFunction1D> pair = polynomials[n];
    final DoubleFunction1D previous = polynomials[n - 1].getFirst();
    final DoubleFunction1D function = pair.getFirst();
    final DoubleFunction1D derivative = pair.getSecond();
    final double[] x = new double[n];
    final double[] w = new double[n];
    double root = 0;
    for (int i = 0; i < n; i++) {
      alphaBeta2 = 2 * n + alphaBeta;
      root = getInitialRootGuess(root, i, n, x);
      root = ROOT_FINDER.getRoot(function, derivative, root);
      x[i] = root;
      w[i] = GAMMA_FUNCTION.evaluate(_alpha + n) * GAMMA_FUNCTION.evaluate(_beta + n) / GAMMA_FUNCTION.evaluate(n + 1.) / GAMMA_FUNCTION.evaluate(n + alphaBeta + 1) * alphaBeta2
          * Math.pow(2, alphaBeta) / (derivative.evaluate(root) * previous.evaluate(root));
    }
    return new GaussianQuadratureData(x, w);
  }

  private double getInitialRootGuess(final double previousRoot, final int i, final int n, final double[] x) {
    if (i == 0) {
      final double a = _alpha / n;
      final double b = _beta / n;
      final double x1 = (1 + _alpha) * (2.78 / (4 + n * n) + 0.768 * a / n);
      final double x2 = 1 + 1.48 * a + 0.96 * b + 0.452 * a * a + 0.83 * a * b;
      return 1 - x1 / x2;
    }
    if (i == 1) {
      final double x1 = (4.1 + _alpha) / ((1 + _alpha) * (1 + 0.156 * _alpha));
      final double x2 = 1 + 0.06 * (n - 8) * (1 + 0.12 * _alpha) / n;
      final double x3 = 1 + 0.012 * _beta * (1 + 0.25 * Math.abs(_alpha)) / n;
      return previousRoot - (1 - previousRoot) * x1 * x2 * x3;
    }
    if (i == 2) {
      final double x1 = (1.67 + 0.28 * _alpha) / (1 + 0.37 * _alpha);
      final double x2 = 1 + 0.22 * (n - 8) / n;
      final double x3 = 1 + 8 * _beta / ((6.28 + _beta) * n * n);
      return previousRoot - (x[0] - previousRoot) * x1 * x2 * x3;
    }
    if (i == n - 2) {
      final double x1 = (1 + 0.235 * _beta) / (0.766 + 0.119 * _beta);
      final double x2 = 1. / (1 + 0.639 * (n - 4.) / (1 + 0.71 * (n - 4.)));
      final double x3 = 1. / (1 + 20 * _alpha / ((7.5 + _alpha) * n * n));
      return previousRoot + (previousRoot - x[n - 4]) * x1 * x2 * x3;
    }
    if (i == n - 1) {
      final double x1 = (1 + 0.37 * _beta) / (1.67 + 0.28 * _beta);
      final double x2 = 1. / (1 + 0.22 * (n - 8.) / n);
      final double x3 = 1. / (1 + 8. * _alpha / ((6.28 + _alpha) * n * n));
      return previousRoot + (previousRoot - x[n - 3]) * x1 * x2 * x3;
    }
    return 3. * x[i - 1] - 3. * x[i - 2] + x[i - 3];
  }
}
