/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import java.util.List;

import com.opengamma.math.PrimeNumbers;

/**
 * 
 * @author emcleod
 */
public class FaureSequenceQuasiRandomNumberGenerator implements QuasiRandomNumberGenerator {

  @Override
  public List<Double[]> getVectors(final int dimension, final int n) {
    // TODO check dimension
    final int base = PrimeNumbers.getNextPrime(dimension);
    final QuasiRandomNumberGenerator vanDerCorput = new VanDerCorputQuasiRandomNumberGenerator(base);
    final Double[] x = new Double[n];
    for (int i = 1; i < n + 1; i++) {
      final int m = (int) Math.floor(Math.log(i) / Math.log(base));
      final int[] a = new int[m + 1];
      final double[] power = new double[m + 2];
      double number = i;
      power[m + 1] = Math.pow(base, m + 1);
      power[m] = power[m + 1] / base;
      for (int j = m; j >= 0; j--) {
        a[j] = (int) (number / power[j]);
        number = number % power[j];
        if (j > 0) {
          power[j - 1] = power[j] / base;
        }
      }
      x[i - 1] = 0.;
      for (int j = m; j >= 0; j--) {
        x[i - 1] += a[j] / power[j + 1];
      }
    }
    return null;
  }
}
