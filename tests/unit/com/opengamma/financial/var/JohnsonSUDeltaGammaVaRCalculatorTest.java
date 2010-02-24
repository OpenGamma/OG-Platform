/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * @author emcleod
 * 
 */
public class JohnsonSUDeltaGammaVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(3.);
  private static final VaRCalculator<SkewKurtosisStatistics<?>> F = new JohnsonSUDeltaGammaVaRCalculator(HORIZON, PERIODS, QUANTILE);
  private static final VaRCalculator<NormalStatistics<?>> NORMAL = new NormalLinearVaRCalculator(HORIZON, PERIODS, QUANTILE);
  private static final Function1D<Double, Double> ZERO = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 0.;
    }

  };

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    F.evaluate((SkewKurtosisStatistics<?>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeKurtosis() {
    final SkewKurtosisStatistics<?> stats = new SkewKurtosisStatistics<Double>(ZERO, ZERO, ZERO, new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return -1.;
      }

    }, 0.);
    F.evaluate(stats);
  }

  @Test
  public void testNormal() {
    final Function1D<Double, Double> SIGMA = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 0.6;
      }

    };
  }
}
