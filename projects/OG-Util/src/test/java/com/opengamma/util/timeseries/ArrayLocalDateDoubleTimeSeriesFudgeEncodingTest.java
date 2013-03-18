/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ArrayLocalDateDoubleTimeSeriesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private ArrayLocalDateDoubleTimeSeriesTest test = new ArrayLocalDateDoubleTimeSeriesTest();
  
  public void testCycle() {
    DoubleTimeSeries<LocalDate> ts =  test.createStandardTimeSeries();
    
    @SuppressWarnings("rawtypes")
    DoubleTimeSeries cycleObject = cycleObject(DoubleTimeSeries.class, ts);
    assertEquals(ArrayLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(ts, cycleObject);
  }

}
