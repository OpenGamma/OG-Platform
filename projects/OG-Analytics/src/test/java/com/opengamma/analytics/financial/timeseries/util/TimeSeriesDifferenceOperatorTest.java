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
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Tests {@link TimeSeriesDifferenceOperator}
 */
public class TimeSeriesDifferenceOperatorTest {
  
  private static final LocalDateDoubleTimeSeries TS_1 = TimeSeriesDataSet.timeSeriesGbpLibor3M2014Jan(LocalDate.of(2014, 2, 1));
  private static final int NB_DATA_1 = TS_1.size();
  
  private static final TimeSeriesDifferenceOperator OP_DIF_1 = new TimeSeriesDifferenceOperator();
  private static final TimeSeriesDifferenceOperator OP_DIF_2 = new TimeSeriesDifferenceOperator(2);
  
  private static final double TOLERANCE_DIFF = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTsException() {
    OP_DIF_1.evaluate((LocalDateDoubleTimeSeries) null);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tooShortTimeSeriesException() {
    LocalDateDoubleTimeSeries tooShortTs = ImmutableLocalDateDoubleTimeSeries.of(
        new LocalDate[] {LocalDate.of(2014, 1, 2)}, new Double[] {1.0});
    OP_DIF_1.evaluate(tooShortTs);
  }
  
  /**
   * Test the difference operator for a standard lag of 1 element.
   */
  @Test
  public void difference1() {
    DateDoubleTimeSeries<?> tsDiff1 = OP_DIF_1.evaluate(TS_1);
    assertEquals(NB_DATA_1 - 1, tsDiff1.size());
    for (int i = 1; i < NB_DATA_1; i++) {
      LocalDate dateTs = TS_1.getTimeAtIndex(i);
      LocalDate dateDiff = (LocalDate) tsDiff1.getTimeAtIndex(i - 1);
      assertEquals(dateTs, dateDiff);
      double diffComputed = tsDiff1.getValueAtIndex(i - 1);
      double diffExpected = TS_1.getValueAtIndex(i) - TS_1.getValueAtIndex(i - 1);
      assertEquals(diffExpected, diffComputed, TOLERANCE_DIFF);
    }
  }
  
  /** Tests the difference operator for a lag of 2 elements. */
  @Test
  public void difference2() {
    DateDoubleTimeSeries<?> tsDiff = OP_DIF_2.evaluate(TS_1);
    assertEquals(NB_DATA_1 - 2, tsDiff.size());
    for (int i = 2; i < NB_DATA_1; i++) {
      LocalDate dateTs = TS_1.getTimeAtIndex(i);
      LocalDate dateDiff = (LocalDate) tsDiff.getTimeAtIndex(i - 2);
      assertEquals(dateTs, dateDiff);
      double diffComputed = tsDiff.getValueAtIndex(i - 2);
      double diffExpected = TS_1.getValueAtIndex(i) - TS_1.getValueAtIndex(i - 2);
      assertEquals(diffExpected, diffComputed, TOLERANCE_DIFF);
    }
  }
  
}
