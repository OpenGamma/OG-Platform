/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.PopulationStandardDeviationCalculator;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * @author emcleod
 * 
 */
public class NormalDistributionMaximumLikelihoodEstimator extends DistributionMaximumLikelihoodEstimator<Double> {
  private final Function1D<Double[], Double> _mean = new MeanCalculator();
  private final Function1D<Double[], Double> _std = new PopulationStandardDeviationCalculator();

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public ProbabilityDistribution<Double> evaluate(final Double[] x) {
    if (x == null)
      throw new IllegalArgumentException("Array was null");
    if (x.length == 0)
      throw new IllegalArgumentException("Array was empty");
    return new NormalDistribution(_mean.evaluate(x), _std.evaluate(x));
  }

}
