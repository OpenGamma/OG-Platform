/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;


import java.util.List;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

@Test
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
