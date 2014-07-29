/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class StatisticsCalculatorFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadName() {
    StatisticsCalculatorFactory.getCalculator("x");
  }

  @Test
  public void testNullCalculator() {
    assertNull(StatisticsCalculatorFactory.getCalculatorName(null));
  }

  @Test
  public void test() {
    assertEquals(StatisticsCalculatorFactory.MEAN, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory.getCalculator(StatisticsCalculatorFactory.MEAN)));
    assertEquals(StatisticsCalculatorFactory.MEDIAN, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory.getCalculator(StatisticsCalculatorFactory.MEDIAN)));
    assertEquals(StatisticsCalculatorFactory.MODE, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory.getCalculator(StatisticsCalculatorFactory.MODE)));
    assertEquals(StatisticsCalculatorFactory.PEARSON_FIRST_SKEWNESS, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory
        .getCalculator(StatisticsCalculatorFactory.PEARSON_FIRST_SKEWNESS)));
    assertEquals(StatisticsCalculatorFactory.PEARSON_SECOND_SKEWNESS, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory
        .getCalculator(StatisticsCalculatorFactory.PEARSON_SECOND_SKEWNESS)));
    assertEquals(StatisticsCalculatorFactory.POPULATION_STANDARD_DEVIATION, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory
        .getCalculator(StatisticsCalculatorFactory.POPULATION_STANDARD_DEVIATION)));
    assertEquals(StatisticsCalculatorFactory.POPULATION_VARIANCE, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory
        .getCalculator(StatisticsCalculatorFactory.POPULATION_VARIANCE)));
    assertEquals(StatisticsCalculatorFactory.QUARTILE_SKEWNESS, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory.getCalculator(StatisticsCalculatorFactory.QUARTILE_SKEWNESS)));
    assertEquals(StatisticsCalculatorFactory.SAMPLE_FISHER_KURTOSIS, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory
        .getCalculator(StatisticsCalculatorFactory.SAMPLE_FISHER_KURTOSIS)));
    assertEquals(StatisticsCalculatorFactory.SAMPLE_PEARSON_KURTOSIS, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory
        .getCalculator(StatisticsCalculatorFactory.SAMPLE_PEARSON_KURTOSIS)));
    assertEquals(StatisticsCalculatorFactory.SAMPLE_SKEWNESS, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory.getCalculator(StatisticsCalculatorFactory.SAMPLE_SKEWNESS)));
    assertEquals(StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory
        .getCalculator(StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION)));
    assertEquals(StatisticsCalculatorFactory.SAMPLE_VARIANCE, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory.getCalculator(StatisticsCalculatorFactory.SAMPLE_VARIANCE)));
    assertEquals(StatisticsCalculatorFactory.GEOMETRIC_MEAN, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory.getCalculator(StatisticsCalculatorFactory.GEOMETRIC_MEAN)));
    assertEquals(StatisticsCalculatorFactory.SAMPLE_COVARIANCE, StatisticsCalculatorFactory.getCalculatorName(StatisticsCalculatorFactory.getCalculator(StatisticsCalculatorFactory.SAMPLE_COVARIANCE)));
  }
}
