package com.opengamma.timeseries;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.time.Duration;
import javax.time.Instant;
import javax.time.calendar.ZonedDateTime;

public abstract class DoubleTimeSeries implements TimeSeries<Double> {
  public abstract int size();
  public abstract boolean isEmpty();
  public abstract Double getValue(ZonedDateTime instant);
  public abstract Double getValue(int index);
  public abstract ZonedDateTime getTime(int index);
  public abstract ZonedDateTime getLatestTime();
  public abstract Double getLatestValue();
  public abstract ZonedDateTime getEarliestTime();
  public abstract Double getEarliestValue();
  public abstract Iterator<Double> valuesIterator();
  public abstract List<Double> values();
  public abstract Iterator<ZonedDateTime> timeIterator();
  public abstract List<ZonedDateTime> times();
  public abstract Iterator<Map.Entry<ZonedDateTime, Double>> iterator();
  public abstract Double getDataPoint(ZonedDateTime instant);
  public abstract DoubleTimeSeries subSeries(ZonedDateTime startTime, ZonedDateTime endTime);
  public abstract Double[] getValues();
  public DoubleTimeSeries subSeries(ZonedDateTime startTime, Duration duration) {
    Instant offset = startTime.toInstant().plus(duration);
    return subSeries(startTime, ZonedDateTime.fromInstant(offset, startTime.getZone()));
  }
}