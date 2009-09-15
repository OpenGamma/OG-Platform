package com.opengamma.timeseries;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.time.Instant;
import javax.time.InstantProvider;

public class MapDoubleTimeSeries extends DoubleTimeSeries  {
  public static final MapDoubleTimeSeries EMPTY_SERIES = new MapDoubleTimeSeries();
  private TreeMap<InstantProvider, Double> _data;
  
  public MapDoubleTimeSeries() {
    _data = new TreeMap<InstantProvider, Double>();
  }
  
  public MapDoubleTimeSeries(SortedMap<InstantProvider, Double> data) {
    _data = new TreeMap<InstantProvider, Double>(data);
  }
  
  public MapDoubleTimeSeries(List<InstantProvider> times, List<Double> values) {
    this();
    if (times.size() != values.size()) { throw new IllegalArgumentException("lists of different lengths"); }
    Iterator<Double> valuesIter = values.iterator();;
    for (InstantProvider time : times) {
      Double value = valuesIter.next();
      if (time != null && value != null) {
        _data.put(time, value);
      } else {
        throw new IllegalArgumentException("list contains null");
      }
    }
  }
  
  public MapDoubleTimeSeries(long[] times, double[] values) {
    this();
    if (times.length != values.length) { throw new IllegalArgumentException("arrays of different lengths"); }
    long timeMax = 0;
    for (int i=0; i<times.length; i++) {
      if (times[i] > timeMax) {
        _data.put(Instant.millisInstant(times[i]), values[i]);
        timeMax = times[i];
      } else {
        throw new IllegalArgumentException("times array must be increasing");
      }
    }
  }
  
  public MapDoubleTimeSeries(DoubleTimeSeries dts) {
    this();
    Iterator<Entry<InstantProvider, Double>> iterator = dts.iterator();
    while (iterator.hasNext()) {
      Entry<InstantProvider, Double> entry = iterator.next();
      _data.put(entry.getKey(), entry.getValue());
    }
  }
  
  @Override
  public Double getDataPoint(InstantProvider instant) {
    return _data.get(instant);
  }

  @Override
  public Iterator<Map.Entry<InstantProvider, Double>> iterator() {
    return _data.entrySet().iterator();
  }
  
  @Override
  public Iterator<InstantProvider> timeIterator() {
    return _data.keySet().iterator();
  }
  
  @Override
  public Iterator<Double> valuesIterator() {
    return _data.values().iterator();
  }
  
  @Override
  public Double getEarliestValue() {
    return _data.get(_data.firstKey());
  }
  
  @Override
  public InstantProvider getEarliestInstant() {
    return _data.firstKey();
  }
  
  @Override
  public Double getLatestValue() {
    return _data.get(_data.lastKey());
  }
    
  @Override
  public InstantProvider getLatestInstant() {
    return _data.lastKey();
  }
  
  @Override
  public int size() {
    return _data.size();
  }
  
  public boolean isEmpty() {
    return _data.isEmpty();
  }
  
  public int hashCode() {
    return _data.hashCode();
  }
  
  public DoubleTimeSeries subSeries(InstantProvider startTime, InstantProvider endTime) {
    return new MapDoubleTimeSeries(_data.subMap(startTime, true, endTime, true));
  }
  
  /* oldest n items */
  public DoubleTimeSeries head(int numItems) {
    InstantProvider[] keys = _data.keySet().toArray(new InstantProvider[] {});
    if (numItems > keys.length) {
      throw new IllegalStateException("you asked for more head elements that are available");
    } else if (numItems == 0) {
      return EMPTY_SERIES;
    } else {
      return new MapDoubleTimeSeries(_data.headMap(keys[keys.length - numItems]));
    }
  }
  
  public boolean equals(Object other) {
    if (other instanceof DoubleTimeSeries) {
      DoubleTimeSeries dts = (DoubleTimeSeries)other;
      if (dts.size() != size()) { return false; }
      Iterator<Entry<InstantProvider, Double>> iterator = _data.entrySet().iterator();
      for (Entry<?, ?> entry : dts) {
        Entry<InstantProvider, Double> myEntry = iterator.next();
        if (!myEntry.equals(entry)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
  
  protected SortedMap<InstantProvider, Double> getUnderlyingMap() {
    return _data;
  }

  @Override
  public Double getDataPoint(int index) {
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
    if (numItems < _data.size()) {
      Iterator<InstantProvider> iter = _data.keySet().iterator();
      for (int i=0; i<numItems; i++) {
        iter.next();
      }
      return new MapDoubleTimeSeries(_data.tailMap(iter.next()));
    } else {
      throw new IndexOutOfBoundsException("Cannot reference data point outside size of time series");
    }
  }
}
