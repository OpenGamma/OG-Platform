/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.Assert;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.math.statistics.distribution.StudentTDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MomentCalculatorTest {
  private static final double STD = 2.;
  private static final double DOF = 10;
  private static final Function1D<double[], Double> MEAN = new MeanCalculator();
  private static final Function1D<double[], Double> SAMPLE_VARIANCE = new SampleVarianceCalculator();
  private static final Function1D<double[], Double> POPULATION_VARIANCE = new PopulationVarianceCalculator();
  private static final Function1D<double[], Double> SAMPLE_STD = new SampleStandardDeviationCalculator();
  private static final Function1D<double[], Double> POPULATION_STD = new PopulationStandardDeviationCalculator();
  private static final Function1D<double[], Double> SAMPLE_SKEWNESS = new SampleSkewnessCalculator();
  private static final Function1D<double[], Double> SAMPLE_PEARSON_KURTOSIS = new SamplePearsonKurtosisCalculator();
  private static final Function1D<double[], Double> SAMPLE_FISHER_KURTOSIS = new SampleFisherKurtosisCalculator();
  private static final Function1D<double[], Double> SAMPLE_CENTRAL_MOMENT = new SampleCentralMomentCalculator(1);
  private static final RandomEngine ENGINE = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, STD, ENGINE);
  private static final ProbabilityDistribution<Double> STUDENT_T = new StudentTDistribution(DOF, ENGINE);
  private static final ProbabilityDistribution<Double> CHI_SQ = new ChiSquareDistribution(DOF, ENGINE);
  private static final double[] NORMAL_DATA = new double[500000];
  private static final double[] STUDENT_T_DATA = new double[500000];
  private static final double[] CHI_SQ_DATA = new double[500000];
  private static final double EPS = 0.1;
  static {
    for (int i = 0; i < 500000; i++) {
      NORMAL_DATA[i] = NORMAL.nextRandom();
      STUDENT_T_DATA[i] = STUDENT_T.nextRandom();
      CHI_SQ_DATA[i] = CHI_SQ.nextRandom();
    }
  }

  @Test
  public void testNull() {
    assertNullArg(SAMPLE_VARIANCE);
    assertNullArg(SAMPLE_STD);
    assertNullArg(POPULATION_VARIANCE);
    assertNullArg(POPULATION_STD);
    assertNullArg(SAMPLE_SKEWNESS);
    assertNullArg(SAMPLE_PEARSON_KURTOSIS);
    assertNullArg(SAMPLE_FISHER_KURTOSIS);
    assertNullArg(SAMPLE_CENTRAL_MOMENT);
  }

  @Test
  public void testInsufficientData() {
    assertInsufficientData(SAMPLE_VARIANCE);
    assertInsufficientData(SAMPLE_STD);
    assertInsufficientData(POPULATION_VARIANCE);
    assertInsufficientData(POPULATION_STD);
    assertInsufficientData(SAMPLE_SKEWNESS);
    assertInsufficientData(SAMPLE_PEARSON_KURTOSIS);
    assertInsufficientData(SAMPLE_FISHER_KURTOSIS);
    assertInsufficientData(SAMPLE_CENTRAL_MOMENT);
  }

  private void assertNullArg(final Function1D<double[], Double> f) {
    try {
      f.evaluate((double[]) null);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  private void assertInsufficientData(final Function1D<double[], Double> f) {
    try {
      f.evaluate(new double[] {1.});
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testNormal() {
    assertEquals(SAMPLE_VARIANCE.evaluate(NORMAL_DATA), STD * STD, EPS);
    assertEquals(POPULATION_VARIANCE.evaluate(NORMAL_DATA), STD * STD, EPS);
    assertEquals(SAMPLE_STD.evaluate(NORMAL_DATA), STD, EPS);
    assertEquals(POPULATION_STD.evaluate(NORMAL_DATA), STD, EPS);
    assertEquals(SAMPLE_SKEWNESS.evaluate(NORMAL_DATA), 0., EPS);
    assertEquals(SAMPLE_PEARSON_KURTOSIS.evaluate(NORMAL_DATA), 3., EPS);
    assertEquals(SAMPLE_FISHER_KURTOSIS.evaluate(NORMAL_DATA), 0., EPS);
  }

  @Test
  public void testStudentT() {
    final double variance = DOF / (DOF - 2);
    assertEquals(SAMPLE_VARIANCE.evaluate(STUDENT_T_DATA), variance, EPS);
    assertEquals(POPULATION_VARIANCE.evaluate(STUDENT_T_DATA), variance, EPS);
    assertEquals(SAMPLE_STD.evaluate(STUDENT_T_DATA), Math.sqrt(variance), EPS);
    assertEquals(POPULATION_STD.evaluate(STUDENT_T_DATA), Math.sqrt(variance), EPS);
    assertEquals(SAMPLE_SKEWNESS.evaluate(STUDENT_T_DATA), 0., EPS);
    assertEquals(SAMPLE_PEARSON_KURTOSIS.evaluate(STUDENT_T_DATA), 3. + 6 / (DOF - 4), EPS);
    assertEquals(SAMPLE_FISHER_KURTOSIS.evaluate(STUDENT_T_DATA), 6 / (DOF - 4), EPS);
  }

  @Test
  public void testChiSq() {
    final double variance = 2 * DOF;
    assertEquals(SAMPLE_VARIANCE.evaluate(CHI_SQ_DATA), variance, EPS);
    assertEquals(POPULATION_VARIANCE.evaluate(CHI_SQ_DATA), variance, EPS);
    assertEquals(SAMPLE_STD.evaluate(CHI_SQ_DATA), Math.sqrt(variance), EPS);
    assertEquals(POPULATION_STD.evaluate(CHI_SQ_DATA), Math.sqrt(variance), EPS);
    assertEquals(SAMPLE_SKEWNESS.evaluate(CHI_SQ_DATA), Math.sqrt(8 / DOF), EPS);
    assertEquals(SAMPLE_PEARSON_KURTOSIS.evaluate(CHI_SQ_DATA), 12 / DOF + 3, EPS);
    assertEquals(SAMPLE_FISHER_KURTOSIS.evaluate(CHI_SQ_DATA), 12 / DOF, EPS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testCentralMomentsConstructor() {
    new SampleCentralMomentCalculator(-1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNormalizedCentralMomentsConstructor() {
    new SampleNormalizedCentralMomentCalculator(-1);
  }

  @Test
  public void testCentralMoments() {
    Function1D<double[], Double> calculator = new SampleCentralMomentCalculator(0);
    assertEquals(calculator.evaluate(NORMAL_DATA), 1, EPS);
    assertEquals(calculator.evaluate(CHI_SQ_DATA), 1, EPS);
    assertEquals(calculator.evaluate(STUDENT_T_DATA), 1, EPS);
    calculator = new SampleCentralMomentCalculator(1);
    assertEquals(calculator.evaluate(NORMAL_DATA), MEAN.evaluate(NORMAL_DATA), EPS);
    calculator = new SampleCentralMomentCalculator(2);
    assertEquals(calculator.evaluate(NORMAL_DATA), SAMPLE_VARIANCE.evaluate(NORMAL_DATA), EPS);
  }

  @Test
  public void testNormalizedCentralMoments() {
    Function1D<double[], Double> calculator = new SampleNormalizedCentralMomentCalculator(0);
    assertEquals(calculator.evaluate(NORMAL_DATA), 1, EPS);
    assertEquals(calculator.evaluate(CHI_SQ_DATA), 1, EPS);
    assertEquals(calculator.evaluate(STUDENT_T_DATA), 1, EPS);
    calculator = new SampleNormalizedCentralMomentCalculator(1);
    assertEquals(calculator.evaluate(NORMAL_DATA), 0, EPS);
    assertEquals(calculator.evaluate(CHI_SQ_DATA), 0, EPS);
    assertEquals(calculator.evaluate(STUDENT_T_DATA), 0, EPS);
    calculator = new SampleNormalizedCentralMomentCalculator(2);
    assertEquals(calculator.evaluate(NORMAL_DATA), 1, EPS);
    assertEquals(calculator.evaluate(CHI_SQ_DATA), 1, EPS);
    assertEquals(calculator.evaluate(STUDENT_T_DATA), 1, EPS);
    calculator = new SampleNormalizedCentralMomentCalculator(3);
    assertEquals(calculator.evaluate(NORMAL_DATA), SAMPLE_SKEWNESS.evaluate(NORMAL_DATA), EPS);
    assertEquals(calculator.evaluate(CHI_SQ_DATA), SAMPLE_SKEWNESS.evaluate(CHI_SQ_DATA), EPS);
    assertEquals(calculator.evaluate(STUDENT_T_DATA), SAMPLE_SKEWNESS.evaluate(STUDENT_T_DATA), EPS);
    calculator = new SampleNormalizedCentralMomentCalculator(4);
    assertEquals(calculator.evaluate(NORMAL_DATA), SAMPLE_PEARSON_KURTOSIS.evaluate(NORMAL_DATA), EPS);
    assertEquals(calculator.evaluate(CHI_SQ_DATA), SAMPLE_PEARSON_KURTOSIS.evaluate(CHI_SQ_DATA), EPS);
    assertEquals(calculator.evaluate(STUDENT_T_DATA), SAMPLE_PEARSON_KURTOSIS.evaluate(STUDENT_T_DATA), EPS);
  }
}
