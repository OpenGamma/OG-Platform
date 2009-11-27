package com.opengamma.timeseries;

import java.util.List;
import java.util.SortedMap;

import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

public class ArrayDoubleTimeSeriesTest extends DoubleTimeSeriesTest {

  @Override
  public DoubleTimeSeries createEmptyTimeSeries() {
    return ArrayDoubleTimeSeries.EMPTY_SERIES;
  }

  @Override
  public DoubleTimeSeries createTimeSeries(long[] times, double[] values, TimeZone[] zones) {
    return new ArrayDoubleTimeSeries(times, values, zones);
  }

  @Override
  public DoubleTimeSeries createTimeSeries(List<ZonedDateTime> times, List<Double> values) {
    return new ArrayDoubleTimeSeries(times, values);
  }

  @Override
  public DoubleTimeSeries createTimeSeries(DoubleTimeSeries dts) {
    return new ArrayDoubleTimeSeries(dts);
  }

  @Override
  public DoubleTimeSeries createTimeSeries(SortedMap<ZonedDateTime, Double> initialMap) {
    return new ArrayDoubleTimeSeries(initialMap);
  }
}
