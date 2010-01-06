/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.statistics.distribution.StudentTDistribution;

/**
 * @author emcleod
 * 
 */
public class PearsonSkewnessCoeffiecientCalculatorTest {
  private static final double STD = 2.;
  private static final double DOF = 10;
  private static final Function1D<Double[], Double> FIRST = new PearsonFirstSkewnessCoefficientCalculator();
  private static final Function1D<Double[], Double> SECOND = new PearsonSecondSkewnessCoefficientCalculator();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalProbabilityDistribution(0, STD);
  private static final ProbabilityDistribution<Double> STUDENT_T = new StudentTDistribution(DOF);
  private static final ProbabilityDistribution<Double> CHI_SQ = new ChiSquareDistribution(DOF);
  private static final Double[] NORMAL_DATA = new Double[50000];
  private static final Double[] STUDENT_T_DATA = new Double[50000];
  private static final Double[] CHI_SQ_DATA = new Double[50000];
  private static final double EPS = 0.1;
  static {
    for (int i = 0; i < 50000; i++) {
      NORMAL_DATA[i] = ((int) (100 * NORMAL.nextRandom())) / 100.;
      STUDENT_T_DATA[i] = ((int) (100 * STUDENT_T.nextRandom())) / 100.;
      CHI_SQ_DATA[i] = ((int) (100 * CHI_SQ.nextRandom())) / 100.;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFirst() {
    FIRST.evaluate((Double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSecond() {
    SECOND.evaluate((Double[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyFirst() {
    FIRST.evaluate(new Double[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptySecond() {
    FIRST.evaluate(new Double[0]);
  }

  @Test
  public void test() {
    assertEquals(FIRST.evaluate(NORMAL_DATA), SECOND.evaluate(NORMAL_DATA), 1e-3);
  }
}
