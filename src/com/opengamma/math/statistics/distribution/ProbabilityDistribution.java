/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

/**
 * 
 * @author emcleod
 */

public interface ProbabilityDistribution<T> {

  public double nextRandom();

  public double getPDF(T x);

  public double getCDF(T x);

  public double getInverseCDF(Double p);

}
