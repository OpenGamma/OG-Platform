/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;


import java.util.List;

import javax.time.calendar.LocalDate;

import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

public class MapLocalDateDoubleTimeSeriesTest extends LocalDateDoubleTimeSeriesTest {

  @Override
  public LocalDateDoubleTimeSeries createEmptyTimeSeries() {
    return new MapLocalDateDoubleTimeSeries();
  }

  @Override
  public LocalDateDoubleTimeSeries createTimeSeries(LocalDate[] times, double[] values) {
    return new MapLocalDateDoubleTimeSeries(times, values);
  }

  @Override
  public LocalDateDoubleTimeSeries createTimeSeries(List<LocalDate> times, List<Double> values) {
    return new MapLocalDateDoubleTimeSeries(times, values);
  }

  @Override
  public LocalDateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<LocalDate> dts) {
    return new MapLocalDateDoubleTimeSeries(dts);
  }
}
