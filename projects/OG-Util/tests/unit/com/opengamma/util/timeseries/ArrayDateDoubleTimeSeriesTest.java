/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;


import java.util.Date;
import java.util.List;

import com.opengamma.util.timeseries.date.ArrayDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.DateDoubleTimeSeries;

public class ArrayDateDoubleTimeSeriesTest extends DateDoubleTimeSeriesTest {

  @Override
  public DoubleTimeSeries<Date> createEmptyTimeSeries() {
    return new ArrayDateDoubleTimeSeries();
  }

  @Override
  public DateDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new ArrayDateDoubleTimeSeries(times, values);
  }

  @Override
  public DateDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new ArrayDateDoubleTimeSeries(times, values);
  }

  @Override
  public DateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new ArrayDateDoubleTimeSeries(dts);
  }
}
