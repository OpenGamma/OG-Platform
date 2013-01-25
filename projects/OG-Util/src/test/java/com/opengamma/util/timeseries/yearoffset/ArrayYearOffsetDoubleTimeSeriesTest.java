/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.yearoffset;

import java.util.List;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.util.timeseries.DoubleTimeSeries;

@Test
public class ArrayYearOffsetDoubleTimeSeriesTest extends YearOffsetDoubleTimeSeriesTest {

  @Override
  public YearOffsetDoubleTimeSeries createEmptyTimeSeries() {
    return new ArrayYearOffsetDoubleTimeSeries(ZonedDateTime.of(2013, 1, 24, 12, 0, 0, 0, TimeZone.UTC), new Double[0], new double[0]);
  }

  @Override
  public YearOffsetDoubleTimeSeries createTimeSeries(final Double[] times, final double[] values) {
    return new ArrayYearOffsetDoubleTimeSeries(ZonedDateTime.of(2013, 1, 24, 12, 0, 0, 0, TimeZone.UTC), times, values);
  }

  @Override
  public YearOffsetDoubleTimeSeries createTimeSeries(final List<Double> times, final List<Double> values) {
    return new ArrayYearOffsetDoubleTimeSeries(ZonedDateTime.of(2013, 1, 24, 12, 0, 0, 0, TimeZone.UTC), times, values);
  }

  @Override
  public YearOffsetDoubleTimeSeries createTimeSeries(final DoubleTimeSeries<Double> dts) {
    return new ArrayYearOffsetDoubleTimeSeries(ZonedDateTime.of(2013, 1, 24, 12, 0, 0, 0, TimeZone.UTC), (YearOffsetDoubleTimeSeries) dts);
  }

}
