/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class ExtendedTrapezoidalIntegrator1D extends Integrator1D<Double, Function1D<Double, Double>, Double> {
  private final int _n;

  public ExtendedTrapezoidalIntegrator1D() {
    this(20);
  }

  public ExtendedTrapezoidalIntegrator1D(final int n) {
    if (n <= 0)
      throw new IllegalArgumentException("Number of divisions must be greater than zero");
    _n = n;
  }

  @Override
  public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {
    if (f == null)
      throw new IllegalArgumentException("Function was null");
    if (lower == null)
      throw new IllegalArgumentException("Lower bound was null");
    if (upper == null)
      throw new IllegalArgumentException("Upper bound was null");
    double result = 0.5 * (upper - lower) * (f.evaluate(upper) + f.evaluate(lower));
    long m = 1;
    double delta, sum, x;
    for (int i = 1; i < _n; i++) {
      m = 1;
      for (int j = 1; j < i - 1; j++) {
        m <<= 1;
        if (m <= 1)
          throw new IntegrationException("Too many integration steps required");
      }
      delta = (upper - lower) / m;
      x = lower + 0.5 * delta;
      sum = 0;
      for (int j = 0; j < m; j++, x += delta) {
        sum += f.evaluate(x);
      }
      result = 0.5 * (result + (upper - lower) * sum / m);
    }
    return result;
  }
}
