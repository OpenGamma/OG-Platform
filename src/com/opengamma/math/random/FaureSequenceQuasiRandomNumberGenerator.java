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
 */
public class FaureSequenceQuasiRandomNumberGenerator implements QuasiRandomNumberGenerator {

  @Override
  public List<double[]> getVectors(final int dimension, final int n) {
    if (dimension < 2) {
      throw new IllegalArgumentException("Dimension must be greater than one");
    }
    if (n < 0) {
      throw new IllegalArgumentException("Number of values must be greater than zero");
    }
    final int base = PrimeNumbers.getNextPrime(dimension);
    final int[][] a = getFirstDimension(n, dimension, base);
    final int m = (int) Math.floor(Math.log(dimension) / Math.log(base));
    final double[][] coeff = getBinomialCoefficientMatrix(base + 1, base);
    int sum;
    for (int i = 0; i < n; i++) {
      sum = 0;
      for (int j = i; j <= base; j++) {
        sum += a[j][0] * coeff[i][j];
      }
      a[i][1] = sum % base;
    }
    return null;
  }

  protected int[][] getFirstDimension(final int n, final int dimension, final int base) {
    final int[][] result = new int[n][dimension];
    for (int i = 1; i < n + 1; i++) {
      result[i - 1][0] = i % base;
    }
    return result;
  }

  protected double[][] getBinomialCoefficientMatrix(final int n, final int m) {
    final double[][] result = new double[n][n];
    result[0][0] = 1;
    for (int i = 1; i < n; i++) {
      result[i][i] = 1;
      result[0][i] = 1;
    }
    for (int i = 1; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        result[i][j] = (result[i - 1][j - 1] + result[i][j - 1]) % m;
      }
    }
    return result;
  }
}
