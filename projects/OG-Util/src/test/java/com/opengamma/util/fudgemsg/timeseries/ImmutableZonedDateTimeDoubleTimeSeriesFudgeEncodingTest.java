/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ImmutableZonedDateTimeDoubleTimeSeriesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final ZoneId LONDON = ZoneId.of("Europe/London");

  private ZonedDateTime[] instants;
  private double[] values;
  private ImmutableZonedDateTimeDoubleTimeSeries ts;

  @BeforeMethod
  public void setUp() {
    instants = new ZonedDateTime[] {ZonedDateTime.of(2012, 6, 30, 0, 0, 0, 0, LONDON), ZonedDateTime.of(2012, 7, 1, 0, 0, 0, 0, LONDON)};
    values = new double[] {1.1d, 2.2d};
    ts = ImmutableZonedDateTimeDoubleTimeSeries.of(instants, values, LONDON);
  }

  public void testCycle1() {
    DoubleTimeSeries<?> cycleObject1 = cycleObject(DoubleTimeSeries.class, ts);
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.class, cycleObject1.getClass());
    assertEquals(ts, cycleObject1);
  }

  public void testCycle2() {
    PreciseDoubleTimeSeries<?> cycleObject2 = cycleObject(PreciseDoubleTimeSeries.class, ts);
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.class, cycleObject2.getClass());
    assertEquals(ts, cycleObject2);
  }

  public void testCycle3() {
    ZonedDateTimeDoubleTimeSeries cycleObject3 = cycleObject(ZonedDateTimeDoubleTimeSeries.class, ts);
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.class, cycleObject3.getClass());
    assertEquals(ts, cycleObject3);
  }

  public void testCycle4() {
    ImmutableZonedDateTimeDoubleTimeSeries cycleObject4 = cycleObject(ImmutableZonedDateTimeDoubleTimeSeries.class, ts);
    assertEquals(ImmutableZonedDateTimeDoubleTimeSeries.class, cycleObject4.getClass());
    assertEquals(ts, cycleObject4);
  }

}
