/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.estimation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.SampleMomentCalculator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class NormalDistributionMomentEstimator extends DistributionParameterEstimator<Double> {
  private final Function1D<double[], Double> _first = new SampleMomentCalculator(1);
  private final Function1D<double[], Double> _second = new SampleMomentCalculator(2);

  @Override
  public ProbabilityDistribution<Double> evaluate(final double[] x) {
    Validate.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    final double m1 = _first.evaluate(x);
    return new NormalDistribution(m1, Math.sqrt(_second.evaluate(x) - m1 * m1));
  }

}
