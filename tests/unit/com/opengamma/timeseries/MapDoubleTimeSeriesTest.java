package com.opengamma.timeseries;

import java.util.List;
import java.util.SortedMap;

import javax.time.InstantProvider;

public class MapDoubleTimeSeriesTest extends DoubleTimeSeriesTest {

  @Override
  public DoubleTimeSeries createEmptyTimeSeries() {
    return MapDoubleTimeSeries.EMPTY_SERIES;
  }

  @Override
  public DoubleTimeSeries createTimeSeries(long[] times, double[] values) {
    return new MapDoubleTimeSeries(times, values);
  }

  @Override
  public DoubleTimeSeries createTimeSeries(List<InstantProvider> times, List<Double> values) {
    return new MapDoubleTimeSeries(times, values);
  }

  @Override
  public DoubleTimeSeries createTimeSeries(DoubleTimeSeries dts) {
    return new MapDoubleTimeSeries(dts);
  }

  @Override
  public DoubleTimeSeries createTimeSeries(
      SortedMap<InstantProvider, Double> initialMap) {
    return new MapDoubleTimeSeries(initialMap);
  }

}
