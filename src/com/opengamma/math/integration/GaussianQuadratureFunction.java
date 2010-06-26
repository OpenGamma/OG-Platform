/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class GaussianQuadratureFunction extends Function1D<Function1D<Double, Double>, Double[]> {
  private final Double[] _weights;
  private final Double[] _abscissas;

  public GaussianQuadratureFunction(final Double[] abscissas, final Double[] weights) {
    _weights = weights;
    _abscissas = abscissas;
  }

  public Double[] getWeights() {
    return _weights;
  }

  public Double[] getAbscissas() {
    return _abscissas;
  }

  @Override
  public Double[] evaluate(final Function1D<Double, Double> x) {
    final Double[] y = new Double[_abscissas.length];
    for (int i = 0; i < _abscissas.length; i++) {
      y[i] = x.evaluate(_abscissas[i]);
    }
    return y;
  }
}
