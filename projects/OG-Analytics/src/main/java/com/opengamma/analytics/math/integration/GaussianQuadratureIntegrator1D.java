/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Class that performs integration using Gaussian quadrature.
 * <p>
 * If a function $f(x)$ can be written as $f(x) = W(x)g(x)$, where $g(x)$ is
 * approximately polynomial, then for suitably chosen weights $w_i$ and points
 * $x_i$, the integral can be approximated as:
 * $$
 * \begin{align*}
 * \int_{-1}^1 f(x)dx 
 * &=\int_{-1}^1 W(x)g(x)dx\\
 * &\approx \sum_{\i=1}^{n} w_i f(x_i)
 * \end{align*}
 * $$
 * The evaluation points, weights and valid limits of integration depend on the type of orthogonal polynomials that are used 
 * (see {@link com.opengamma.analytics.math.function.special.OrthogonalPolynomialFunctionGenerator} and {@link GaussLaguerreWeightAndAbscissaFunction}).
 * 
 */
public abstract class GaussianQuadratureIntegrator1D extends Integrator1D<Double, Double> {
  private final int _n;
  private final QuadratureWeightAndAbscissaFunction _generator;

  /**
   * @param n The number of sample points to be used in the integration, not negative or zero
   * @param generator The generator of weights and abscissas
   */
  public GaussianQuadratureIntegrator1D(final int n, final QuadratureWeightAndAbscissaFunction generator) {
    Validate.isTrue(n > 0, "number of intervals must be > 0");
    Validate.notNull(generator, "generating function");
    _n = n;
    _generator = generator;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double integrate(final Function1D<Double, Double> function, final Double lower, final Double upper) {
    Validate.notNull(function);
    Validate.notNull(lower);
    Validate.notNull(upper);
    final Function1D<Double, Double> integral = getIntegralFunction(function, lower, upper);
    final GaussianQuadratureData quadrature = _generator.generate(_n);
    final double[] abscissas = quadrature.getAbscissas();
    final int n = abscissas.length;
    final double[] weights = quadrature.getWeights();
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += integral.evaluate(abscissas[i]) * weights[i];
    }
    return sum;
  }

  /**
   * @return The lower and upper limits for which the quadrature is valid
   */
  public abstract Double[] getLimits();

  /**
   * Returns a function that is valid for both the type of quadrature and the limits of integration. 
   * @param function The function to be integrated, not null
   * @param lower The lower integration limit, not null
   * @param upper The upper integration limit, not null
   * @return A function in the appropriate form for integration
   */
  public abstract Function1D<Double, Double> getIntegralFunction(final Function1D<Double, Double> function, final Double lower, final Double upper);

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _generator.hashCode();
    result = prime * result + _n;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final GaussianQuadratureIntegrator1D other = (GaussianQuadratureIntegrator1D) obj;
    if (_n != other._n) {
      return false;
    }
    return ObjectUtils.equals(_generator, other._generator);
  }

}
