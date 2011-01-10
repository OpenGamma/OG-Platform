/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class QuartileSkewnessCalculatorTest {
  private static final double STD = 2.;
  private static final Function1D<double[], Double> SKEW = new QuartileSkewnessCalculator();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, STD, new MersenneTwister64(MersenneTwister64.DEFAULT_SEED));
  private static final ProbabilityDistribution<Double> CHI_SQ = new ChiSquareDistribution(4);
  private static final double[] NORMAL_DATA = new double[50000];
  private static final double[] CHI_SQ_DATA = new double[50000];
  static {
    for (int i = 0; i < 50000; i++) {
      NORMAL_DATA[i] = NORMAL.nextRandom();
      CHI_SQ_DATA[i] = CHI_SQ.nextRandom();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    SKEW.evaluate((double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    SKEW.evaluate(new double[0]);
  }

  @Test
  public void test() {
    Double t = SKEW.evaluate(NORMAL_DATA);
    assertEquals(t, 0, 0.1);
    t = SKEW.evaluate(CHI_SQ_DATA);
    assertFalse(Math.abs(t) < 0.1);
  }
}
