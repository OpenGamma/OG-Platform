/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;


import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.ListDateDoubleTimeSeries;

@Test
public class ListDateDoubleTimeSeriesTest extends DateDoubleTimeSeriesTest {

  @Override
  public DateDoubleTimeSeries createEmptyTimeSeries() {
    return ListDateDoubleTimeSeries.EMPTY_SERIES;
  }

  @Override
  public DateDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new ListDateDoubleTimeSeries(times, values);
  }

  @Override
  public DateDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new ListDateDoubleTimeSeries(times, values);
  }

  @Override
  public DateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<Date> dts) {
    return new ListDateDoubleTimeSeries(dts);
  }

}
