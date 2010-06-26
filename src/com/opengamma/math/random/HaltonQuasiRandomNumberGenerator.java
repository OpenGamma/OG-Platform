/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.math.PrimeNumbers;

/**
 * 
 */
public class HaltonQuasiRandomNumberGenerator implements QuasiRandomNumberGenerator {
  private final VanDerCorputQuasiRandomNumberGenerator _vanDerCorput = new VanDerCorputQuasiRandomNumberGenerator(2);

  @Override
  public List<Double[]> getVectors(final int dimension, final int n) {
    if (dimension < 2) {
      throw new IllegalArgumentException("Dimension must be greater than one");
    }
    if (n < 0) {
      throw new IllegalArgumentException("Number of values must be greater than zero");
    }
    final Double[][] sequence = new Double[n][dimension];
    final List<Double[]> result = new ArrayList<Double[]>(n);
    List<Double[]> s;
    for (int i = 0; i < dimension; i++) {
      _vanDerCorput.setBase(PrimeNumbers.getNthPrime(i + 1));
      s = _vanDerCorput.getVectors(1, n);
      for (int j = 0; j < n; j++) {
        sequence[j][i] = s.get(j)[0];
      }
    }
    for (int i = 0; i < n; i++) {
      result.add(sequence[i]);
    }
    return result;
  }
}
