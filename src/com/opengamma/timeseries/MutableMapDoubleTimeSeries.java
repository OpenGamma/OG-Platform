package com.opengamma.timeseries;

import java.util.List;
import java.util.SortedMap;

import javax.time.InstantProvider;

public class MutableMapDoubleTimeSeries extends MapDoubleTimeSeries {
  public MutableMapDoubleTimeSeries() {
    super();
  }
  public MutableMapDoubleTimeSeries(SortedMap<InstantProvider, Double> data) {
    super(data);
  }
  public MutableMapDoubleTimeSeries(List<InstantProvider> times, List<Double> values) {
    super(times, values);
  }
  
  public void putDataPoint(InstantProvider time, Double value) {
    if (time == null || value == null) {
      throw new IllegalArgumentException("Cannot add null time or value to timeseries");
    }
    getUnderlyingMap().put(time, value);
  }
  
  public void removeDataPoint(InstantProvider time) {
    if (time == null) {
      throw new IllegalArgumentException("Cannot remove null time from timeseries");
    } 
    getUnderlyingMap().remove(time);
  }
  
  public void removeAll() {
    getUnderlyingMap().clear();
  }
}
