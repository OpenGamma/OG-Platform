/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class NormalRandomNumberGenerator implements RandomNumberGenerator {
  private final ProbabilityDistribution<Double> _normal;

  public NormalRandomNumberGenerator(final double mean, final double sigma) {
    if (sigma <= 0) {
      throw new IllegalArgumentException("Cannot have a negative standard deviation");
    }
    _normal = new NormalDistribution(mean, sigma);
  }

  @Override
  public List<Double[]> getVectors(final int dimension, final int n) {
    if (dimension < 0) {
      throw new IllegalArgumentException("Dimension must be greater than zero");
    }
    if (n < 0) {
      throw new IllegalArgumentException("Number of values must be greater than zero");
    }
    final List<Double[]> result = new ArrayList<Double[]>();
    Double[] x;
    for (int i = 0; i < n; i++) {
      x = new Double[dimension];
      for (int j = 0; j < dimension; j++) {
        x[j] = _normal.nextRandom();
      }
      result.add(x);
    }
    return result;
  }
}
