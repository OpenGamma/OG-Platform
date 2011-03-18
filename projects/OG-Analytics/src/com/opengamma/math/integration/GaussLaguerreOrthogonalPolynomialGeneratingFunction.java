/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.function.DoubleFunction1D;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.special.LaguerrePolynomialFunction;
import com.opengamma.math.function.special.NaturalLogGammaFunction;
import com.opengamma.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class GaussLaguerreOrthogonalPolynomialGeneratingFunction implements QuadratureWeightAndAbscissaFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(GaussLaguerreOrthogonalPolynomialGeneratingFunction.class);
  private static final LaguerrePolynomialFunction LAGUERRE = new LaguerrePolynomialFunction();
  private static final NewtonRaphsonSingleRootFinder ROOT_FINDER = new NewtonRaphsonSingleRootFinder(1e-10);
  private static final Function1D<Double, Double> LOG_GAMMA_FUNCTION = new NaturalLogGammaFunction();
  private final double _alpha;

  public GaussLaguerreOrthogonalPolynomialGeneratingFunction() {
    this(0);
  }

  public GaussLaguerreOrthogonalPolynomialGeneratingFunction(final double alpha) {
    _alpha = alpha;
  }

  @Override
  public GaussianQuadratureFunction generate(final int n, final Double... parameters) {
    if (parameters != null) {
      s_logger.info("Limits for this integration method are 0 and +infinity; ignoring bounds");
    }
    return generate(n);
  }

  public GaussianQuadratureFunction generate(final int n) {
    Validate.isTrue(n > 0);
    final Pair<DoubleFunction1D, DoubleFunction1D>[] polynomials = LAGUERRE.getPolynomialsAndFirstDerivative(n, _alpha);
    final Pair<DoubleFunction1D, DoubleFunction1D> pair = polynomials[n];
    final DoubleFunction1D p1 = polynomials[n - 1].getFirst();
    final DoubleFunction1D function = pair.getFirst();
    final DoubleFunction1D derivative = pair.getSecond();
    final double[] x = new double[n];
    final double[] w = new double[n];
    double root = 0;
    for (int i = 0; i < n; i++) {
      root = ROOT_FINDER.getRoot(function, derivative, getInitialRootGuess(root, i, n, x));
      x[i] = root;
      w[i] = -Math.exp(LOG_GAMMA_FUNCTION.evaluate(_alpha + n) - LOG_GAMMA_FUNCTION.evaluate(Double.valueOf(n))) / (derivative.evaluate(root) * n * p1.evaluate(root));
    }
    return new GaussianQuadratureFunction(x, w);
  }

  private double getInitialRootGuess(final double previousRoot, final int i, final int n, final double[] x) {
    if (i == 0) {
      return (1 + _alpha) * (3 + 0.92 * _alpha) / (1 + 1.8 * _alpha + 2.4 * n);
    }
    if (i == 1) {
      return previousRoot + (15 + 6.25 * _alpha) / (1 + 0.9 * _alpha + 2.5 * n);
    }
    final int j = i - 1;
    return previousRoot + ((1 + 2.55 * j) / 1.9 / j + 1.26 * j * _alpha / (1 + 3.5 * j)) * (previousRoot - x[i - 2]) / (1 + 0.3 * _alpha);
  }
}
