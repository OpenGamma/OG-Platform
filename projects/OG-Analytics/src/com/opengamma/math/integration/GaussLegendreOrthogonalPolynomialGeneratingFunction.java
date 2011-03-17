/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.DoubleFunction1D;
import com.opengamma.math.function.special.LegendrePolynomialFunction;
import com.opengamma.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class GaussLegendreOrthogonalPolynomialGeneratingFunction implements QuadratureWeightAndAbscissaFunction {
  private static final LegendrePolynomialFunction LEGENDRE = new LegendrePolynomialFunction();
  private static final NewtonRaphsonSingleRootFinder ROOT_FINDER = new NewtonRaphsonSingleRootFinder(1e-15);

  @Override
  public GaussianQuadratureFunction generate(final int n, final Double... parameters) {
    Validate.isTrue(n > 0);
    Validate.notNull(parameters, "parameters");
    Validate.notEmpty(parameters, "parameters");
    Validate.noNullElements(parameters, "parameters");
    final double lower = parameters[0];
    final double upper = parameters[1];
    final int mid = (n + 1) / 2;
    final double x1 = (upper + lower) / 2.;
    final double x2 = (upper - lower) / 2.;
    final double[] x = new double[n];
    final double[] w = new double[n];
    final Pair<DoubleFunction1D, DoubleFunction1D>[] polynomials = LEGENDRE.getPolynomialsAndFirstDerivative(n);
    final Pair<DoubleFunction1D, DoubleFunction1D> pair = polynomials[n];
    final DoubleFunction1D function = pair.getFirst();
    final DoubleFunction1D derivative = pair.getSecond();
    for (int i = 0; i < mid; i++) {
      final double root = ROOT_FINDER.getRoot(function, derivative, getInitialRootGuess(i, n));
      x[i] = x1 - x2 * root;
      x[n - i - 1] = x1 + x2 * root;
      final double dp = derivative.evaluate(root);
      w[i] = 2 * x2 / ((1 - root * root) * dp * dp);
      w[n - i - 1] = w[i];
    }
    return new GaussianQuadratureFunction(x, w);
  }

  private double getInitialRootGuess(final int i, final int n) {
    return Math.cos(Math.PI * (i + 0.75) / (n + 0.5));
  }
}
