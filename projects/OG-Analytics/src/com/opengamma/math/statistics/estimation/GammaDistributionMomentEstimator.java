/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.SampleMomentCalculator;
import com.opengamma.math.statistics.distribution.GammaDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class GammaDistributionMomentEstimator extends DistributionParameterEstimator<Double> {
  private final Function1D<double[], Double> _first = new SampleMomentCalculator(1);
  private final Function1D<double[], Double> _second = new SampleMomentCalculator(2);

  @Override
  public ProbabilityDistribution<Double> evaluate(final double[] x) {
    Validate.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    final double m1 = _first.evaluate(x);
    final double m2 = _second.evaluate(x);
    final double m1Sq = m1 * m1;
    final double k = m1Sq / (m2 - m1Sq);
    final double theta = m1 / k;
    return new GammaDistribution(k, theta);
  }

}
