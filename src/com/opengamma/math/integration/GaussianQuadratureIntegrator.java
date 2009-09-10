package com.opengamma.math.integration;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public class GaussianQuadratureIntegrator extends Integrator1D<Double, Function1D<Double, Double, MathException>, Double, MathException> {
  private final int _n;
  private final GeneratingFunction<Double, GaussianQuadratureFunction, MathException> _generator;

  public GaussianQuadratureIntegrator(int n, GeneratingFunction<Double, GaussianQuadratureFunction, MathException> generator) {
    _n = n;
    _generator = generator;
  }

  @Override
  public Double integrate(Function1D<Double, Double, MathException> function, Double lower, Double upper) throws MathException {
    GaussianQuadratureFunction quadrature = _generator.generate(_n, new Double[] { lower, upper });
    Double[] ordinals = quadrature.evaluate(function);
    Double[] weights = quadrature.getWeights();
    double sum = 0;
    for (int i = 0; i < ordinals.length; i++) {
      sum += ordinals[i] * weights[i];
    }
    return sum;
  }
}
