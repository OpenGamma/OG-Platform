package com.opengamma.timeseries;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.time.Duration;
import javax.time.InstantProvider;

public abstract class DoubleTimeSeries implements TimeSeries<Double> {
  public abstract int size();
  public abstract boolean isEmpty();
  public abstract InstantProvider getLatestInstant();
  public abstract Double getLatestValue();
  public abstract InstantProvider getEarliestInstant();
  public abstract Double getEarliestValue();
  public abstract Iterator<Double> valuesIterator();
  public abstract List<Double> values();
  public abstract Iterator<InstantProvider> timeIterator();
  public abstract List<InstantProvider> times();
  public abstract Iterator<Map.Entry<InstantProvider, Double>> iterator();
  public abstract Double getDataPoint(InstantProvider instant);
  public abstract DoubleTimeSeries subSeries(InstantProvider startTime, InstantProvider endTime);
  public DoubleTimeSeries subSeries(InstantProvider startTime, Duration duration) {
    return subSeries(startTime, startTime.toInstant().plus(duration));
  }
}