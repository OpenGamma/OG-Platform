/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * Test SimpleHistoricalTimeSeries Fudge support.
 */
public class SimpleHistoricalTimeSeriesFudgeEncodingTest {
  
  private static final UniqueId UID = UniqueId.of("A", "B", "V");
  private static final double[] VALUES = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
  private static final LocalDate[] TIMES = testTimes();
  private final FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
  
  @Test
  public void test_cycling_array() {
    SimpleHistoricalTimeSeries simpleHistoricalTimeSeries = new SimpleHistoricalTimeSeries(UID, 
        new ArrayLocalDateDoubleTimeSeries(TIMES, VALUES));
    cycleTimeSeries(simpleHistoricalTimeSeries);
  }
  
  @Test
  public void test_cycling_list() {
    SimpleHistoricalTimeSeries simpleHistoricalTimeSeries = new SimpleHistoricalTimeSeries(UID, 
        new ListLocalDateDoubleTimeSeries(TIMES, VALUES));
    cycleTimeSeries(simpleHistoricalTimeSeries);
  }
  
  @Test
  public void test_cycling_map() {
    SimpleHistoricalTimeSeries simpleHistoricalTimeSeries = new SimpleHistoricalTimeSeries(UID, 
        new MapLocalDateDoubleTimeSeries(TIMES, VALUES));
    cycleTimeSeries(simpleHistoricalTimeSeries);
  }
  
  private void cycleTimeSeries(final SimpleHistoricalTimeSeries original) {
    FudgeMsgEnvelope msgEnvelope = _fudgeContext.toFudgeMsg(original);
    assertNotNull(msgEnvelope);
    SimpleHistoricalTimeSeries fromFudgeMsg = _fudgeContext.fromFudgeMsg(SimpleHistoricalTimeSeries.class, msgEnvelope.getMessage());
    assertTrue(original != fromFudgeMsg);
    assertEquals(original, fromFudgeMsg);
  }

 
  private static LocalDate[] testTimes() {
    LocalDate one = LocalDate.of(2010, MonthOfYear.FEBRUARY, 8);
    LocalDate two = LocalDate.of(2010, MonthOfYear.FEBRUARY, 9);
    LocalDate three = LocalDate.of(2010, MonthOfYear.FEBRUARY, 10);
    LocalDate four = LocalDate.of(2010, MonthOfYear.FEBRUARY, 11);
    LocalDate five = LocalDate.of(2010, MonthOfYear.FEBRUARY, 12);
    LocalDate six = LocalDate.of(2010, MonthOfYear.FEBRUARY, 13);
    return new LocalDate[] {one, two, three, four, five, six };
  }

}
