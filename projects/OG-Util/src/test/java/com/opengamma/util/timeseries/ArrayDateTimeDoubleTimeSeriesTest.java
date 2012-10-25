/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.timeseries.date.time.ArrayDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;

@Test
public class ArrayDateTimeDoubleTimeSeriesTest extends DateDoubleTimeSeriesTest {

  @Override
  public DateTimeDoubleTimeSeries createEmptyTimeSeries() {
    return new ArrayDateTimeDoubleTimeSeries();
  }

  @Override
  public DateTimeDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new ArrayDateTimeDoubleTimeSeries(times, values);
  }

  @Override
  public DateTimeDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new ArrayDateTimeDoubleTimeSeries(times, values);
  }

  @Override
  public DateTimeDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new ArrayDateTimeDoubleTimeSeries(dts);
  }
}
