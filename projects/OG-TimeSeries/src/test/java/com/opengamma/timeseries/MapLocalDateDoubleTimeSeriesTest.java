/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.localdate.MapLocalDateDoubleTimeSeries;

@Test(groups = "unit")
public class MapLocalDateDoubleTimeSeriesTest extends LocalDateDoubleTimeSeriesTest {

  @Override
  protected LocalDateDoubleTimeSeries createEmptyTimeSeries() {
    return new MapLocalDateDoubleTimeSeries();
  }

  @Override
  protected LocalDateDoubleTimeSeries createTimeSeries(LocalDate[] times, double[] values) {
    return new MapLocalDateDoubleTimeSeries(times, values);
  }

  @Override
  protected LocalDateDoubleTimeSeries createTimeSeries(List<LocalDate> times, List<Double> values) {
    return new MapLocalDateDoubleTimeSeries(times, values);
  }

  @Override
  protected LocalDateDoubleTimeSeries createTimeSeries(DoubleTimeSeries<LocalDate> dts) {
    return new MapLocalDateDoubleTimeSeries(dts);
  }

}
