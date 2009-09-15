package com.opengamma.timeseries;

import java.util.List;
import java.util.SortedMap;

import javax.time.InstantProvider;

public class ArrayDoubleTimeSeriesTest extends DoubleTimeSeriesTest {

  @Override
  public DoubleTimeSeries createEmptyTimeSeries() {
    return ArrayDoubleTimeSeries.EMPTY_SERIES;
  }

  @Override
  public DoubleTimeSeries createTimeSeries(long[] times, double[] values) {
    return new ArrayDoubleTimeSeries(times, values);
  }

  @Override
  public DoubleTimeSeries createTimeSeries(List<InstantProvider> times, List<Double> values) {
    return new ArrayDoubleTimeSeries(times, values);
  }

  @Override
  public DoubleTimeSeries createTimeSeries(DoubleTimeSeries dts) {
    return new ArrayDoubleTimeSeries(dts);
  }

  @Override
  public DoubleTimeSeries createTimeSeries(
      SortedMap<InstantProvider, Double> initialMap) {
    return new ArrayDoubleTimeSeries(initialMap);
  }
}
