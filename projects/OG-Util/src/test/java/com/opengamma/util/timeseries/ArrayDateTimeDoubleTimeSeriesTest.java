/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.timeseries.date.time.ArrayDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;

@Test(groups = TestGroup.UNIT)
public class ArrayDateTimeDoubleTimeSeriesTest extends DateDoubleTimeSeriesTest {

  @Override
  protected DateTimeDoubleTimeSeries createEmptyTimeSeries() {
    return new ArrayDateTimeDoubleTimeSeries();
  }

  @Override
  protected DateTimeDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new ArrayDateTimeDoubleTimeSeries(times, values);
  }

  @Override
  protected DateTimeDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new ArrayDateTimeDoubleTimeSeries(times, values);
  }

  @Override
  protected DateTimeDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new ArrayDateTimeDoubleTimeSeries(dts);
  }

}
