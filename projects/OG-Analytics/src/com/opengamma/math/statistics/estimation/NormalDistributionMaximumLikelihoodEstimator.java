/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.PopulationStandardDeviationCalculator;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class NormalDistributionMaximumLikelihoodEstimator extends DistributionParameterEstimator<Double> {
  // TODO add error estimates
  private final Function1D<double[], Double> _mean = new MeanCalculator();
  private final Function1D<double[], Double> _std = new PopulationStandardDeviationCalculator();

  @Override
  public ProbabilityDistribution<Double> evaluate(final double[] x) {
    Validate.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    return new NormalDistribution(_mean.evaluate(x), _std.evaluate(x));
  }

}
