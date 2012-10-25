/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;

/**
 * Test Fudge encoding.
 */
@Test
public class ArrayLocalDateDoubleTimeSeriesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private ArrayLocalDateDoubleTimeSeriesTest test = new ArrayLocalDateDoubleTimeSeriesTest();
  
  public void testCycle() {
    DoubleTimeSeries<LocalDate> ts =  test.createStandardTimeSeries();
    
    DoubleTimeSeries cycleObject = cycleObject(DoubleTimeSeries.class, ts);
    assertEquals(ArrayLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(ts, cycleObject);
  }

}
