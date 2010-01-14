package com.opengamma.timeseries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.time.Instant;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

public class MapDoubleTimeSeries extends DoubleTimeSeries  {
  public static final DoubleTimeSeries EMPTY_SERIES = new MapDoubleTimeSeries();
  private TreeMap<ZonedDateTime, Double> _data;
  
  public MapDoubleTimeSeries() {
    _data = new TreeMap<ZonedDateTime, Double>();
  }
  
  public MapDoubleTimeSeries(SortedMap<ZonedDateTime, Double> data) {
    _data = new TreeMap<ZonedDateTime, Double>(data);
  }
  
  public MapDoubleTimeSeries(List<ZonedDateTime> times, List<Double> values) {
    this();
    if (times.size() != values.size()) { throw new IllegalArgumentException("lists of different lengths"); }
    Iterator<Double> valuesIter = values.iterator();
    for (ZonedDateTime time : times) {
      Double value = valuesIter.next();
      if (time != null && value != null) {
        _data.put(time, value);
      } else {
        throw new IllegalArgumentException("list contains null");
      }
    }
  }
  
  public MapDoubleTimeSeries(long[] times, double[] values, TimeZone[] zones) {
    this();
    if (times.length != values.length || times.length != zones.length) { 
      throw new IllegalArgumentException("arrays of different lengths"); 
      }
    long timeMax = 0;
    for (int i=0; i<times.length; i++) {
      if (times[i] > timeMax) {
        _data.put(ZonedDateTime.fromInstant(Instant.millisInstant(times[i]), zones[i]), values[i]);
        timeMax = times[i];
      } else {
        throw new IllegalArgumentException("times array must be increasing");
      }
    }
  }
  
  public MapDoubleTimeSeries(DoubleTimeSeries dts) {
    this();
    Iterator<Entry<ZonedDateTime, Double>> iterator = dts.iterator();
    while (iterator.hasNext()) {
      Entry<ZonedDateTime, Double> entry = iterator.next();
      _data.put(entry.getKey(), entry.getValue());
    }
  }
  
  @Override
  public Double getDataPoint(ZonedDateTime instant) {
    return _data.get(instant);
  }

  @Override
  public Iterator<Map.Entry<ZonedDateTime, Double>> iterator() {
    return _data.entrySet().iterator();
  }
  
  @Override
  public Iterator<ZonedDateTime> timeIterator() {
    return _data.keySet().iterator();
  }
  
  @Override
  public List<ZonedDateTime> times() {
    return new ArrayList<ZonedDateTime>(_data.keySet());
  }
  
  @Override
  public Iterator<Double> valuesIterator() {
    return _data.values().iterator();
  }
  
  @Override 
  public List<Double> values() {
    return new ArrayList<Double>(_data.values());
  }
  
  @Override
  public Double getEarliestValue() {
    return _data.get(_data.firstKey());
  }
  
  @Override
  public ZonedDateTime getEarliestTime() {
    return _data.firstKey();
  }
  
  @Override
  public Double getLatestValue() {
    return _data.get(_data.lastKey());
  }
    
  @Override
  public ZonedDateTime getLatestTime() {
    return _data.lastKey();
  }
  
  @Override
  public int size() {
    return _data.size();
  }
  
  @Override
  public Double[] getValues() {
    return _data.values().toArray(new Double[0]);
  }
  
  public boolean isEmpty() {
    return _data.isEmpty();
  }
  
  public int hashCode() {
    return _data.hashCode();
  }
  
  public DoubleTimeSeries subSeries(ZonedDateTime startTime, ZonedDateTime endTime) {
    return new MapDoubleTimeSeries(_data.subMap(startTime, true, endTime, true));
  }
  
  /* oldest n items */
  public DoubleTimeSeries head(int numItems) {
    ZonedDateTime[] keys = _data.keySet().toArray(new ZonedDateTime[] {});
    if (numItems > keys.length) {
      throw new IllegalStateException("you asked for more head elements that are available");
    } else if (numItems == 0) {
      return EMPTY_SERIES;
    } else {
      return new MapDoubleTimeSeries(_data.headMap(keys[numItems]));
    }
  }
  
  public boolean equals(Object other) {
    if (other instanceof DoubleTimeSeries) {
      DoubleTimeSeries dts = (DoubleTimeSeries)other;
      if (dts.size() != size()) { return false; }
      Iterator<Entry<ZonedDateTime, Double>> iterator = _data.entrySet().iterator();
      for (Entry<?, ?> entry : dts) {
        Entry<ZonedDateTime, Double> myEntry = iterator.next();
        if (!myEntry.equals(entry)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
  
  protected SortedMap<ZonedDateTime, Double> getUnderlyingMap() {
    return _data;
  }

  @Override
  public Double getValue(int index) {
    // TODO: make this efficient
    if (index < _data.size()) { // this assumption has threading implications in Mutable subclass
      Iterator<Double> iter = _data.values().iterator();
      for (int i=0; i<index; i++) {
        iter.next();
      }
      return iter.next();
    } else {
      throw new IndexOutOfBoundsException("Cannot reference data point outside size of time series");
    }
  }
  

  @Override
  public TimeSeries<Double> tail(int numItems) {
    // TODO: make this efficient
    if(numItems == 0) return EMPTY_SERIES; 
    if (numItems < _data.size()) {
      ZonedDateTime[] keys = _data.keySet().toArray(new ZonedDateTime[] {});      
      return new MapDoubleTimeSeries(_data.tailMap(keys[keys.length - numItems]));
    } else {
      throw new IndexOutOfBoundsException("Cannot reference data point outside size of time series");
    }
  }

  @Override
  public ZonedDateTime getTime(int index) {
    // TODO: make this efficient
    if (index < _data.size()) { // this assumption has threading implications in Mutable subclass
      Iterator<ZonedDateTime> iter = _data.keySet().iterator();
      for (int i=0; i<index; i++) {
        iter.next();
      }
      return iter.next();
    } else {
      throw new IndexOutOfBoundsException("Cannot reference data point outside size of time series");
    }

  }

  @Override
  public Double getValue(ZonedDateTime instant) {
    Double value = _data.get(instant);
    if (value != null) {
      return value;
    } else {
      throw new NoSuchElementException();
    }
  }
}
