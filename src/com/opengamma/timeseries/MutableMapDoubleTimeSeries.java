package com.opengamma.timeseries;

import java.util.List;
import java.util.SortedMap;

import javax.time.calendar.ZonedDateTime;

public class MutableMapDoubleTimeSeries extends MapDoubleTimeSeries {
  public MutableMapDoubleTimeSeries() {
    super();
  }
  public MutableMapDoubleTimeSeries(SortedMap<ZonedDateTime, Double> data) {
    super(data);
  }
  public MutableMapDoubleTimeSeries(List<ZonedDateTime> times, List<Double> values) {
    super(times, values);
  }
  
  public void putDataPoint(ZonedDateTime time, Double value) {
    if (time == null || value == null) {
      throw new IllegalArgumentException("Cannot add null time or value to timeseries");
    }
    getUnderlyingMap().put(time, value);
  }
  
  public void removeDataPoint(ZonedDateTime time) {
    if (time == null) {
      throw new IllegalArgumentException("Cannot remove null time from timeseries");
    } 
    getUnderlyingMap().remove(time);
  }
  
  public void removeAll() {
    getUnderlyingMap().clear();
  }
}
