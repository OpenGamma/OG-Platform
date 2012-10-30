/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.estimation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.SampleMomentCalculator;
import com.opengamma.analytics.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ChiSquareDistributionMomentEstimator extends DistributionParameterEstimator<Double> {
  private final Function1D<double[], Double> _first = new SampleMomentCalculator(1);

  @Override
  public ProbabilityDistribution<Double> evaluate(final double[] x) {
    Validate.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    return new ChiSquareDistribution(_first.evaluate(x));
  }
}
