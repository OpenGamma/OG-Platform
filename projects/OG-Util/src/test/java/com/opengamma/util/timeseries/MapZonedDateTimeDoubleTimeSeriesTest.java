/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;


import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.timeseries.zoneddatetime.MapZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;

@Test
public class MapZonedDateTimeDoubleTimeSeriesTest extends ZonedDateTimeDoubleTimeSeriesTest {

  @Override
  public ZonedDateTimeDoubleTimeSeries createEmptyTimeSeries() {
    return new MapZonedDateTimeDoubleTimeSeries(ZoneOffset.UTC);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries createTimeSeries(ZonedDateTime[] times, double[] values) {
    return new MapZonedDateTimeDoubleTimeSeries(ZoneOffset.UTC, times, values);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries createTimeSeries(List<ZonedDateTime> times, List<Double> values) {
    return new MapZonedDateTimeDoubleTimeSeries(ZoneOffset.UTC, times, values);
  }
  
  @Override
  public ZonedDateTimeDoubleTimeSeries createTimeSeries(DoubleTimeSeries<ZonedDateTime> dts) {
    return new MapZonedDateTimeDoubleTimeSeries(ZoneOffset.UTC, dts);
  }
}
