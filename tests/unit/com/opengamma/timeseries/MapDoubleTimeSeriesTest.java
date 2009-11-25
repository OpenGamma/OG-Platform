package com.opengamma.timeseries;

import java.util.List;
import java.util.SortedMap;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

public class MapDoubleTimeSeriesTest extends DoubleTimeSeriesTest {

  @Override
  public DoubleTimeSeries createEmptyTimeSeries() {
    return MapDoubleTimeSeries.EMPTY_SERIES;
  }

  @Override
  public DoubleTimeSeries createTimeSeries(long[] times, double[] values, TimeZone[] zones) {
    return new MapDoubleTimeSeries(times, values, zones);
  }

  @Override
  public DoubleTimeSeries createTimeSeries(List<ZonedDateTime> times, List<Double> values) {
    return new MapDoubleTimeSeries(times, values);
  }
  
  @Override
  public DoubleTimeSeries createTimeSeries(DoubleTimeSeries dts) {
    return new MapDoubleTimeSeries(dts);
  }

  @Override
  public DoubleTimeSeries createTimeSeries(SortedMap<ZonedDateTime, Double> initialMap) {
    return new MapDoubleTimeSeries(initialMap);
  }

}
