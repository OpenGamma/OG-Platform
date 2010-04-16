/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;


import java.util.Date;
import java.util.List;

import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.ListDateTimeDoubleTimeSeries;

public class ListDateTimeDoubleTimeSeriesTest extends DateDoubleTimeSeriesTest {

  @Override
  public DateTimeDoubleTimeSeries createEmptyTimeSeries() {
    return ListDateTimeDoubleTimeSeries.EMPTY_SERIES;
  }

  @Override
  public DateTimeDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new ListDateTimeDoubleTimeSeries(times, values);
  }

  @Override
  public DateTimeDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new ListDateTimeDoubleTimeSeries(times, values);
  }

  @Override
  public DateTimeDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new ListDateTimeDoubleTimeSeries(dts);
  }

}
