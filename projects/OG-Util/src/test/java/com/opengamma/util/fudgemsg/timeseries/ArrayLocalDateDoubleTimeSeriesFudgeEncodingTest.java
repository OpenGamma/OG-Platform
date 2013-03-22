/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class ArrayLocalDateDoubleTimeSeriesFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  public void testCycle() {
    LocalDate[] dates = {LocalDate.of(2012, 6, 30), LocalDate.of(2012, 7, 1)};
    double[] values = {1.1d, 2.2d};
    DoubleTimeSeries<LocalDate> ts = new ArrayLocalDateDoubleTimeSeries(dates, values);
    
    @SuppressWarnings("rawtypes")
    DoubleTimeSeries cycleObject = cycleObject(DoubleTimeSeries.class, ts);
    assertEquals(ArrayLocalDateDoubleTimeSeries.class, cycleObject.getClass());
    assertEquals(ts, cycleObject);
  }

}
