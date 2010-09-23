/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

/**
 * @param <T> Type of the random number
 */
public interface ProbabilityDistribution<T> {

  double nextRandom();

  double getPDF(T x);

  double getCDF(T x);

  double getInverseCDF(T p);

}
