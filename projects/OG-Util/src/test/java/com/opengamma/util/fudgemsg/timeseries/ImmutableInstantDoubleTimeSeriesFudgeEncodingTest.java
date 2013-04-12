/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ImmutableInstantDoubleTimeSeriesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void testCycle() {
    Instant[] instants = {Instant.ofEpochSecond(30), Instant.ofEpochSecond(31)};
    double[] values = {1.1d, 2.2d};
    DoubleTimeSeries<Instant> ts = ImmutableInstantDoubleTimeSeries.of(instants, values);
    
    @SuppressWarnings("rawtypes")
    DoubleTimeSeries cycleObject = cycleObject(DoubleTimeSeries.class, ts);
    assertEquals(ImmutableInstantDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(ts, cycleObject);
  }

}
