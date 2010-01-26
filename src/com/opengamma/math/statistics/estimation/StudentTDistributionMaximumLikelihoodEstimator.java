/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.estimation;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.special.GammaFunction;
import com.opengamma.math.minimization.GoldenSectionMinimizer1D;
import com.opengamma.math.minimization.Minimizer1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.descriptive.PopulationStandardDeviationCalculator;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.statistics.distribution.StudentTDistribution;

/**
 * @author emcleod
 * 
 */
public class StudentTDistributionMaximumLikelihoodEstimator extends DistributionMaximumLikelihoodEstimator<Double> {
  private final Minimizer1D _minimizer = new GoldenSectionMinimizer1D();
  protected final Function1D<Double, Double> _gamma = new GammaFunction();
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
    final Double[] standardized = getStandardizedData(x);
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double nu) {
        double sum = 0;
        for (final double t : standardized) {
          sum += Math.log(_gamma.evaluate((nu + 1) / 2.) * Math.pow(1 + t * t / (nu - 2), -(nu + 1) / 2.) / Math.sqrt(Math.PI * (nu - 2)) / _gamma.evaluate(nu / 2.));
        }
        return -sum;
      }

    };
    return new StudentTDistribution(_minimizer.minimize(f, new Double[] { 3., 10. })[0]);
  }

  protected Double[] getStandardizedData(final Double[] x) {
    final double mean = _mean.evaluate(x);
    final double std = _std.evaluate(x);
    final Double[] z = new Double[x.length];
    for (int i = 0; i < x.length; i++) {
      z[i] = (x[i] - mean) / std;
    }
    return z;
  }
}
