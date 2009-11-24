/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.random;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 */
public class NormalRandomNumberGenerator implements RandomNumberGenerator {
  private final ProbabilityDistribution<Double> _normal;

  public NormalRandomNumberGenerator(final double mean, final double sigma) {
    _normal = new NormalProbabilityDistribution(mean, sigma);
  }

  @Override
  public List<Double[]> getVectors(final int dimension, final int n) {
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
