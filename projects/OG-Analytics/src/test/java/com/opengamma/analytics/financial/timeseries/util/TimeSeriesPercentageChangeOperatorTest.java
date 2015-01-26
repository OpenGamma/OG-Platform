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
 * Tests {@link TimeSeriesPercentageChangeOperator}
 */
public class TimeSeriesPercentageChangeOperatorTest {

  private static final LocalDateDoubleTimeSeries TS_1 = TimeSeriesDataSet.timeSeriesGbpLibor3M2014Jan(
      LocalDate.of(2014, 2, 1));
  private static final int NB_DATA_1 = TS_1.size();

  private static final TimeSeriesPercentageChangeOperator OP_REL_1 = new TimeSeriesPercentageChangeOperator();
  private static final TimeSeriesPercentageChangeOperator OP_REL_2 = new TimeSeriesPercentageChangeOperator(2);

  private static final double TOLERANCE_DIFF = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTsException() {
    OP_REL_1.evaluate((LocalDateDoubleTimeSeries) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tooShortTimeSeriesException() {
    LocalDateDoubleTimeSeries tooShortTs = ImmutableLocalDateDoubleTimeSeries.of(
        new LocalDate[] {LocalDate.of(2014, 1, 2) }, new Double[] {1.0 });
    OP_REL_1.evaluate(tooShortTs);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void zeroValue() {
    LocalDateDoubleTimeSeries zeroValueTs = ImmutableLocalDateDoubleTimeSeries.of(
        new LocalDate[] {LocalDate.of(2014, 1, 2), LocalDate.of(2014, 1, 3), LocalDate.of(2014, 1, 4) }, 
        new Double[] {1.0, 0.0, 1.0 });
    OP_REL_1.evaluate(zeroValueTs);
  }

  /** Test the relative change operator for a standard lag of 1 element. */
  @Test
  public void relative1() {
    DateDoubleTimeSeries<?> tsRet1 = OP_REL_1.evaluate(TS_1);
    assertEquals(NB_DATA_1 - 1, tsRet1.size());
    for (int i = 1; i < NB_DATA_1; i++) {
      LocalDate dateTs = TS_1.getTimeAtIndex(i);
      LocalDate dateRet = (LocalDate) tsRet1.getTimeAtIndex(i - 1);
      assertEquals(dateTs, dateRet);
      double retComputed = tsRet1.getValueAtIndex(i - 1);
      double retExpected = (TS_1.getValueAtIndex(i) - TS_1.getValueAtIndex(i - 1)) / TS_1.getValueAtIndex(i - 1);
      assertEquals(retExpected, retComputed, TOLERANCE_DIFF);
    }
  }

  /** Tests the relative change operator for a lag of 2 elements. */
  @Test
  public void relative2() {
    DateDoubleTimeSeries<?> tsRet = OP_REL_2.evaluate(TS_1);
    assertEquals(NB_DATA_1 - 2, tsRet.size());
    for (int i = 2; i < NB_DATA_1; i++) {
      LocalDate dateTs = TS_1.getTimeAtIndex(i);
      LocalDate dateRet = (LocalDate) tsRet.getTimeAtIndex(i - 2);
      assertEquals(dateTs, dateRet);
      double retComputed = tsRet.getValueAtIndex(i - 2);
      double retExpected = (TS_1.getValueAtIndex(i) - TS_1.getValueAtIndex(i - 2)) / TS_1.getValueAtIndex(i - 2);
      assertEquals(retExpected, retComputed, TOLERANCE_DIFF);
    }
  }
  
}
