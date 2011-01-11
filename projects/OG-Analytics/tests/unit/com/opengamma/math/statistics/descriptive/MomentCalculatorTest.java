/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.statistics.distribution.StudentTDistribution;

/**
 * 
 */
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
  private static final RandomEngine ENGINE = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
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
    testNull(SAMPLE_VARIANCE);
    testNull(SAMPLE_STD);
    testNull(POPULATION_VARIANCE);
    testNull(POPULATION_STD);
    testNull(SAMPLE_SKEWNESS);
    testNull(SAMPLE_PEARSON_KURTOSIS);
    testNull(SAMPLE_FISHER_KURTOSIS);
    testNull(SAMPLE_CENTRAL_MOMENT);
  }

  @Test
  public void testInsufficientData() {
    testInsufficientData(SAMPLE_VARIANCE);
    testInsufficientData(SAMPLE_STD);
    testInsufficientData(POPULATION_VARIANCE);
    testInsufficientData(POPULATION_STD);
    testInsufficientData(SAMPLE_SKEWNESS);
    testInsufficientData(SAMPLE_PEARSON_KURTOSIS);
    testInsufficientData(SAMPLE_FISHER_KURTOSIS);
    testInsufficientData(SAMPLE_CENTRAL_MOMENT);
  }

  private void testNull(final Function1D<double[], Double> f) {
    try {
      f.evaluate((double[]) null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  private void testInsufficientData(final Function1D<double[], Double> f) {
    try {
      f.evaluate(new double[] {1.});
      fail();
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

  @Test(expected = IllegalArgumentException.class)
  public void testCentralMomentsConstructor() {
    new SampleCentralMomentCalculator(-1);
  }

  @Test(expected = IllegalArgumentException.class)
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
