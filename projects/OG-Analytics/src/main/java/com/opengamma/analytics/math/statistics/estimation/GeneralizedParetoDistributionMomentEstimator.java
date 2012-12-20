/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.estimation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleSkewnessCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleVarianceCalculator;
import com.opengamma.analytics.math.statistics.distribution.GeneralizedParetoDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class GeneralizedParetoDistributionMomentEstimator extends DistributionParameterEstimator<Double> {
  private static final Function1D<double[], Double> MEAN = new MeanCalculator();
  private static final Function1D<double[], Double> VARIANCE = new SampleVarianceCalculator();
  private static final Function1D<double[], Double> SKEWNESS = new SampleSkewnessCalculator();
  private static final RealSingleRootFinder ROOT_FINDER = new BisectionSingleRootFinder();
  private static final BracketRoot BRACKETER = new BracketRoot();

  @Override
  public ProbabilityDistribution<Double> evaluate(final double[] x) {
    Validate.notNull(x);
    final double mean = MEAN.evaluate(x);
    final double variance = VARIANCE.evaluate(x);
    final double skewness = SKEWNESS.evaluate(x);
    final Function1D<Double, Double> ksiFunction = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double a) {
        return 2 * (1 + a) * Math.sqrt(1 - 2. * a) / (1 - 3. * a) - skewness;
      }

    };
    double[] bracket = BRACKETER.getBracketedPoints(ksiFunction, 0, 0.33333);
    final double ksi = ROOT_FINDER.getRoot(ksiFunction, bracket[0], bracket[1]);
    final double ksiP1 = 1 - ksi;
    final double sigma = Math.sqrt(variance * (1 - 2 * ksi) * ksiP1 * ksiP1);
    final double mu = mean - sigma / ksiP1;
    return new GeneralizedParetoDistribution(mu, sigma, ksi);
  }
}
