package com.opengamma.util.timeseries.zoneddatetime;


import java.util.List;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

public class MapDoubleTimeSeriesTest extends ZonedDateTimeDoubleTimeSeriesTest {

  @Override
  public ZonedDateTimeDoubleTimeSeries createEmptyTimeSeries() {
    return new MapZonedDateTimeDoubleTimeSeries(TimeZone.UTC);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries createTimeSeries(ZonedDateTime[] times, double[] values) {
    return new MapZonedDateTimeDoubleTimeSeries(TimeZone.UTC, times, values);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries createTimeSeries(List<ZonedDateTime> times, List<Double> values) {
    return new MapZonedDateTimeDoubleTimeSeries(TimeZone.UTC, times, values);
  }
  
  @Override
  public ZonedDateTimeDoubleTimeSeries createTimeSeries(ZonedDateTimeDoubleTimeSeries dts) {
    return new MapZonedDateTimeDoubleTimeSeries(TimeZone.UTC, dts);
  }
}
