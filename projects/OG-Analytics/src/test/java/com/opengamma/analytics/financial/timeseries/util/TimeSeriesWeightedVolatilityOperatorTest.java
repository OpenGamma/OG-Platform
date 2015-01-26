/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Tests {@link TimeSeriesWeightedVolatilityOperator}
 */
public class TimeSeriesWeightedVolatilityOperatorTest {

  private static final LocalDateDoubleTimeSeries TS_1 = TimeSeriesDataSet.timeSeriesGbpLibor3M2014Jan(
      LocalDate.of(2014, 2, 1));
  private static final int NB_DATA_1 = TS_1.size();

  private static final double LAMBDA = 0.98;
  private static final TimeSeriesPercentageChangeOperator OP_REL_1 = new TimeSeriesPercentageChangeOperator();
  private static final TimeSeriesWeightedVolatilityOperator OP_EWMA_1 =
      TimeSeriesWeightedVolatilityOperator.relative(LAMBDA);
  private static final TimeSeriesPercentageChangeOperator OP_REL_2 = new TimeSeriesPercentageChangeOperator(2);
  private static final TimeSeriesWeightedVolatilityOperator OP_EWMA_2 =
      new TimeSeriesWeightedVolatilityOperator(OP_REL_2, LAMBDA, 0);

  private static final double TOLERANCE_DIFF = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void incorrectLambda0Exception() {
    TimeSeriesWeightedVolatilityOperator.relative(0.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void incorrectLambda1Exception() {
    TimeSeriesWeightedVolatilityOperator.relative(1.0);
  }

  /** Test the EWMA for a relative change with 1 period lag. No seed period. */
  @Test
  public void ewmaRelative1NoSeed() {
    DateDoubleTimeSeries<?> tsEwma = OP_EWMA_1.evaluate(TS_1);
    DateDoubleTimeSeries<?> tsRet = OP_REL_1.evaluate(TS_1);
    assertEquals(NB_DATA_1 - 1, tsEwma.size());
    double variancePrevious = tsRet.getValueAtIndex(0) * tsRet.getValueAtIndex(0);
    double volExpected0 = Math.sqrt(variancePrevious);
    double volComputed0 = tsEwma.getValueAtIndex(0);
    assertEquals("TimeSeriesWeightedVolatilityOperator: volatility - " + 0, volExpected0, volComputed0, TOLERANCE_DIFF);
    for (int i = 1; i < tsEwma.size(); i++) {
      assertEquals(tsEwma.getTimeAtIndex(i), tsRet.getTimeAtIndex(i));
      double varianceExpected = LAMBDA * variancePrevious + (1.0d - LAMBDA) * tsRet.getValueAtIndex(i) *
          tsRet.getValueAtIndex(i);
      double volExpected = Math.sqrt(varianceExpected);
      double volComputed = tsEwma.getValueAtIndex(i);
      assertEquals("TimeSeriesWeightedVolatilityOperator: volatility - " + i, volExpected, volComputed, TOLERANCE_DIFF);
      variancePrevious = varianceExpected;
    }
  }

  /** Test the EWMA for a relative change with 2 period lag. No seed period. */
  @Test
  public void ewmaRelative2NoSeed() {
    DateDoubleTimeSeries<?> tsEwma = OP_EWMA_2.evaluate(TS_1);
    DateDoubleTimeSeries<?> tsRet = OP_REL_2.evaluate(TS_1);
    assertEquals(NB_DATA_1 - 2, tsEwma.size());
    double variancePrevious = tsRet.getValueAtIndex(0) * tsRet.getValueAtIndex(0);
    double volExpected0 = Math.sqrt(variancePrevious);
    double volComputed0 = tsEwma.getValueAtIndex(0);
    assertEquals("TimeSeriesWeightedVolatilityOperator: volatility - " + 0, volExpected0, volComputed0, TOLERANCE_DIFF);
    for (int i = 1; i < tsEwma.size(); i++) {
      assertEquals(tsEwma.getTimeAtIndex(i), tsRet.getTimeAtIndex(i));
      double varianceExpected = LAMBDA * variancePrevious + (1.0d - LAMBDA) * tsRet.getValueAtIndex(i) *
          tsRet.getValueAtIndex(i);
      double volExpected = Math.sqrt(varianceExpected);
      double volComputed = tsEwma.getValueAtIndex(i);
      assertEquals("TimeSeriesWeightedVolatilityOperator: volatility - " + i, volExpected, volComputed, TOLERANCE_DIFF);
      variancePrevious = varianceExpected;
    }
  }

  /** Test the EWMA for a relative change with 1 period lag and seed period of length 1. Should be equal to no seed. */
  @Test
  public void ewmaRelative1Seed1() {
    DateDoubleTimeSeries<?> tsEwmaNS = OP_EWMA_1.evaluate(TS_1);
    TimeSeriesWeightedVolatilityOperator opEwmaS1 = new TimeSeriesWeightedVolatilityOperator(OP_REL_1, LAMBDA, 1);
    DateDoubleTimeSeries<?> tsEwmaS1 = opEwmaS1.evaluate(TS_1);
    assertEquals(tsEwmaNS.size(), tsEwmaS1.size());
    for (int i = 0; i < tsEwmaNS.size(); i++) {
      assertEquals(tsEwmaNS.getTimeAtIndex(i), tsEwmaS1.getTimeAtIndex(i));
      assertEquals("TimeSeriesWeightedVolatilityOperator: volatility - " + i,
          tsEwmaNS.getValueAtIndexFast(i), tsEwmaS1.getValueAtIndexFast(i), TOLERANCE_DIFF);
    }
  }

  /** Test the EWMA for a relative change with 1 period lag and seed period of length 1. Should be equal to no seed. */
  @Test
  public void ewmaRelative1Seed10() {
    int seedLength = 10;
    DateDoubleTimeSeries<?> tsRet = OP_REL_1.evaluate(TS_1);
    TimeSeriesWeightedVolatilityOperator opEwmaS10 = new TimeSeriesWeightedVolatilityOperator(OP_REL_1, LAMBDA,
        seedLength);
    DateDoubleTimeSeries<?> tsEwmaS1 = opEwmaS10.evaluate(TS_1);
    assertEquals(tsRet.size() - seedLength + 1, tsEwmaS1.size());
    int outputLength = tsEwmaS1.size();
    // Seed variance
    double seedVariance = 0.0;
    for (int i = 0; i < seedLength; i++) {
      double returnTs = tsRet.getValueAtIndexFast(i);
      seedVariance += returnTs * returnTs;
    }
    seedVariance /= seedLength;
    assertEquals(tsRet.getTimeAtIndexFast(seedLength - 1), tsEwmaS1.getTimeAtIndexFast(0));
    assertEquals(Math.sqrt(seedVariance), tsEwmaS1.getValueAtIndexFast(0), TOLERANCE_DIFF);
    // EWMA part
    double varianceEwma = seedVariance;
    for (int i = 1; i < outputLength; i++) {
      varianceEwma = LAMBDA * varianceEwma + 
          (1.0d - LAMBDA) * tsRet.getValueAtIndexFast(i + seedLength - 1) * tsRet.getValueAtIndexFast(i + seedLength - 1);
      assertEquals(tsRet.getTimeAtIndexFast(seedLength - 1 + i), tsEwmaS1.getTimeAtIndexFast(i));
      assertEquals("TimeSeriesWeightedVolatilityOperator: volatility - " + i,
          Math.sqrt(varianceEwma), tsEwmaS1.getValueAtIndexFast(i), TOLERANCE_DIFF);      
    }
  }

  /** Test the EWMA for a relative change with 2 period lag and seed period of length 1. Should be equal to no seed. */
  @Test
  public void ewmaRelative2Seed10() {
    int seedLength = 10;
    DateDoubleTimeSeries<?> tsRet = OP_REL_2.evaluate(TS_1);
    TimeSeriesWeightedVolatilityOperator opEwmaS10 = new TimeSeriesWeightedVolatilityOperator(OP_REL_2, LAMBDA,
        seedLength);
    DateDoubleTimeSeries<?> tsEwmaS1 = opEwmaS10.evaluate(TS_1);
    assertEquals(tsRet.size() - seedLength + 1, tsEwmaS1.size());
    int outputLength = tsEwmaS1.size();
    // Seed variance
    double seedVariance = 0.0;
    for (int i = 0; i < seedLength; i++) {
      double returnTs = tsRet.getValueAtIndexFast(i);
      seedVariance += returnTs * returnTs;
    }
    seedVariance /= seedLength;
    assertEquals(tsRet.getTimeAtIndexFast(seedLength - 1), tsEwmaS1.getTimeAtIndexFast(0));
    assertEquals(Math.sqrt(seedVariance), tsEwmaS1.getValueAtIndexFast(0), TOLERANCE_DIFF);
    // EWMA part
    double varianceEwma = seedVariance;
    for (int i = 1; i < outputLength; i++) {
      varianceEwma = LAMBDA * varianceEwma + 
          (1.0d - LAMBDA) * tsRet.getValueAtIndexFast(i + seedLength - 1) * tsRet.getValueAtIndexFast(i + seedLength - 1);
      assertEquals(tsRet.getTimeAtIndexFast(seedLength - 1 + i), tsEwmaS1.getTimeAtIndexFast(i));
      assertEquals("TimeSeriesWeightedVolatilityOperator: volatility - " + i,
          Math.sqrt(varianceEwma), tsEwmaS1.getValueAtIndexFast(i), TOLERANCE_DIFF);      
    }
  }

}
