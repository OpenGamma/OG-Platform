/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.InstantDoubleTimeSeries;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ImmutableInstantDoubleTimeSeriesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private Instant[] instants;
  private double[] values;
  private ImmutableInstantDoubleTimeSeries ts;

  @BeforeMethod
  public void setUp() {
    instants = new Instant[] {Instant.ofEpochSecond(30), Instant.ofEpochSecond(31)};
    values = new double[] {1.1d, 2.2d};
    ts = ImmutableInstantDoubleTimeSeries.of(instants, values);
  }

  public void testCycle1() {
    DoubleTimeSeries<?> cycleObject = cycleObject(DoubleTimeSeries.class, ts);
    assertEquals(ImmutableInstantDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(ts, cycleObject);
  }

  public void testCycle2() {
    PreciseDoubleTimeSeries<?> cycleObject = cycleObject(PreciseDoubleTimeSeries.class, ts);
    assertEquals(ImmutableInstantDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(ts, cycleObject);
  }

  public void testCycle3() {
    InstantDoubleTimeSeries cycleObject = cycleObject(InstantDoubleTimeSeries.class, ts);
    assertEquals(ImmutableInstantDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(ts, cycleObject);
  }

  public void testCycle4() {
    ImmutableInstantDoubleTimeSeries cycleObject = cycleObject(ImmutableInstantDoubleTimeSeries.class, ts);
    assertEquals(ImmutableInstantDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(ts, cycleObject);
  }

}
