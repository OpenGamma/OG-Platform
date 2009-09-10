package com.opengamma.math.integration;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public class GaussianQuadratureFunction extends Function1D<Function1D<Double, Double, MathException>, Double[], MathException> {
  private final Double[] _weights;
  private final Double[] _abscissas;

  public GaussianQuadratureFunction(Double[] abscissas, Double[] weights) {
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
  public Double[] evaluate(Function1D<Double, Double, MathException> x) throws MathException {
    Double[] y = new Double[_abscissas.length];
    for (int i = 0; i < _abscissas.length; i++) {
      y[i] = x.evaluate(_abscissas[i]);
    }
    return y;
  }
}
