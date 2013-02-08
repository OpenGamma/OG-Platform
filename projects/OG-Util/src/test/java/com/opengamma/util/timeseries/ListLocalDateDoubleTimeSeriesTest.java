/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;


import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

@Test
public class ListLocalDateDoubleTimeSeriesTest extends LocalDateDoubleTimeSeriesTest {

  @Override
  public LocalDateDoubleTimeSeries createEmptyTimeSeries() {
    return new ListLocalDateDoubleTimeSeries();
  }

  @Override
  public LocalDateDoubleTimeSeries createTimeSeries(LocalDate[] times, double[] values) {
    return new ListLocalDateDoubleTimeSeries(times, values);
  }

  @Override
  public LocalDateDoubleTimeSeries createTimeSeries(List<LocalDate> times, List<Double> values) {
    return new ListLocalDateDoubleTimeSeries(times, values);
  }

  @Override
  public LocalDateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<LocalDate> dts) {
    return new ListLocalDateDoubleTimeSeries(dts);
  }
  
}
