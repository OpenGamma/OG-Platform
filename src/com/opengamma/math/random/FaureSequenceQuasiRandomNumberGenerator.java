/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * 
 * @author emcleod
 */
public class FaureSequenceQuasiRandomNumberGenerator implements QuasiRandomNumberGenerator {
  // TODO need a large table of primes
  private final List<Integer> PRIME_LIST = Arrays.asList(1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41);
  private final TreeSet<Integer> PRIME_SET = new TreeSet<Integer>(PRIME_LIST);

  @Override
  public List<Double[]> getQuasiRandomVectors(final int dimension, final int n) {
    final Integer p = PRIME_SET.ceiling(n);
    final int m = (int) Math.floor(Math.log(dimension) / Math.log(p));
    final List<Double[]> vectors = new ArrayList<Double[]>();
    final double[][] a = new double[n][m];
    final double[][] binomial = new double[m][m];
    binomial[0][0] = 1;
    for (int i = 1; i < m; i++) {
      binomial[i][0] = 1;
      for (int j = 1; j < m; j++) {
        binomial[i][j] = binomial[i - 1][j] + binomial[i - 1][j - 1];
      }
    }
    Double[] x;
    for (int k = 0; k < n; k++) {
      x = new Double[dimension];
      if (k == 0) {
        for (int l = 0; l < m; l++) {
          a[k][l] = (int) Math.floor(dimension % Math.pow(p, l + 1) / p);
        }
      }
      vectors.add(x);
    }
    return vectors;
  }
}
