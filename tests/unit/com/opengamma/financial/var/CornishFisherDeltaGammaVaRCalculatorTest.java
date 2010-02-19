/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * @author emcleod
 * 
 */
public class CornishFisherDeltaGammaVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(3.);
  private static final Function1D<NormalStatistics<?>, Double> NORMAL = new NormalLinearVaRCalculator(HORIZON, PERIODS, QUANTILE);
  private static final Function1D<SkewKurtosisStatistics<?>, Double> CF = new CornishFisherDeltaGammaVaRCalculator(HORIZON, PERIODS, QUANTILE);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeHorizon() {
    new CornishFisherDeltaGammaVaRCalculator(-HORIZON, PERIODS, QUANTILE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePeriods() {
    new CornishFisherDeltaGammaVaRCalculator(HORIZON, -PERIODS, QUANTILE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowQuantile() {
    new CornishFisherDeltaGammaVaRCalculator(HORIZON, PERIODS, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighQuantile() {
    new CornishFisherDeltaGammaVaRCalculator(-HORIZON, PERIODS, 2);
  }

  @Test
  public void testAgainstNormal() {
    final Function1D<Double, Double> mean = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 0.0;
      }

    };
    final Function1D<Double, Double> std = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 0.3;
      }

    };
    Function1D<Double, Double> skew = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 0.;
      }

    };
    Function1D<Double, Double> kurtosis = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 0.;
      }

    };
    final SkewKurtosisStatistics<Double> statistics = new SkewKurtosisStatistics<Double>(mean, std, skew, kurtosis, 0.);
    final double eps = 1e-6;
    assertEquals(NORMAL.evaluate(statistics), CF.evaluate(statistics), eps);
    skew = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 1.5;
      }

    };
    kurtosis = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 5.6;
      }

    };
    assertTrue(CF.evaluate(statistics) > NORMAL.evaluate(statistics));
  }
}
