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
 * @author emcleod
 */
public class HaltonQuasiRandomNumberGenerator implements QuasiRandomNumberGenerator {

  @Override
  public List<Double[]> getVectors(final int dimension, final int n) {
    final Double[][] sequences = new Double[dimension][n];
    QuasiRandomNumberGenerator vanDerCorput;
    for (int i = 0; i < dimension; i++) {
      vanDerCorput = new VanDerCorputQuasiRandomNumberGenerator(PrimeNumbers.getNthPrime(i + 1));
      sequences[i] = vanDerCorput.getVectors(1, n).get(0);
    }
    final List<Double[]> result = new ArrayList<Double[]>();
    Double[] vector;
    for (int i = 0; i < n; i++) {
      vector = new Double[dimension];
      for (int j = 0; j < dimension; j++) {
        vector[j] = sequences[j][i];
      }
      result.add(vector);
    }
    return result;
  }
}
