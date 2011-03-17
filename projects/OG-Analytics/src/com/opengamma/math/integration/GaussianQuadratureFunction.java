/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class GaussianQuadratureFunction {
  private final double[] _weights;
  private final double[] _abscissas;

  public GaussianQuadratureFunction(final double[] abscissas, final double[] weights) {
    Validate.notNull(abscissas, "abscissas");
    Validate.notNull(weights, "weights");
    Validate.isTrue(abscissas.length == weights.length, "Abscissa and weight arrays must be the same length");
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_abscissas);
    result = prime * result + Arrays.hashCode(_weights);
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
    final GaussianQuadratureFunction other = (GaussianQuadratureFunction) obj;
    if (!Arrays.equals(_abscissas, other._abscissas)) {
      return false;
    }
    return Arrays.equals(_weights, other._weights);
  }

}
