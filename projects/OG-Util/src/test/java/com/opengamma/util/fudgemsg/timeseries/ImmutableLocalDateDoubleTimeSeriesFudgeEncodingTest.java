/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ImmutableLocalDateDoubleTimeSeriesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private LocalDate[] dates;
  private double[] values;
  private ImmutableLocalDateDoubleTimeSeries ts;

  @BeforeMethod
  public void setUp() {
    dates = new LocalDate[] {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    values = new double[] {1.1d, 2.2d};
    ts = ImmutableLocalDateDoubleTimeSeries.of(dates, values);
  }

  public void testCycle1() {
    DoubleTimeSeries<?> cycleObject = cycleObject(DoubleTimeSeries.class, ts);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(ts, cycleObject);
  }

  public void testCycle2() {
    DateDoubleTimeSeries<?> cycleObject = cycleObject(DateDoubleTimeSeries.class, ts);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(ts, cycleObject);
  }

  public void testCycle3() {
    LocalDateDoubleTimeSeries cycleObject = cycleObject(LocalDateDoubleTimeSeries.class, ts);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(ts, cycleObject);
  }

  public void testCycle4() {
    ImmutableLocalDateDoubleTimeSeries cycleObject = cycleObject(ImmutableLocalDateDoubleTimeSeries.class, ts);
    assertEquals(ImmutableLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(ts, cycleObject);
  }

}
