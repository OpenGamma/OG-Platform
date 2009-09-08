package com.opengamma.math.statistics.distribution;

/**
 * 
 * @author emcleod
 * 
 */

public interface ProbabilityDistribution<T> {

  public double nextRandom();

  public double getPDF(T x);

  public double getCDF(T x);

}
