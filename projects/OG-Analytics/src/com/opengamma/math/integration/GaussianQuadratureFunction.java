/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class GaussianQuadratureFunction extends Function1D<Function1D<Double, Double>, double[]> {
  private final double[] _weights;
  private final double[] _abscissas;

  public GaussianQuadratureFunction(final double[] abscissas, final double[] weights) {
    _weights = weights;
    _abscissas = abscissas;
  }

  public double[] getWeights() {
    return _weights;
  }

  public double[] getAbscissas() {
    return _abscissas;
  }

  @Override
  public double[] evaluate(final Function1D<Double, Double> x) {
    final double[] y = new double[_abscissas.length];
    for (int i = 0; i < _abscissas.length; i++) {
      y[i] = x.evaluate(_abscissas[i]);
    }
    return y;
  }
}
