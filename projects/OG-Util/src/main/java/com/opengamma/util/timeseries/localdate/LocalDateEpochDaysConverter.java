/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.localdate;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * A converter for {@code LocalDate}.
 */
public class LocalDateEpochDaysConverter implements DateTimeConverter<LocalDate>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The time zone.
   */
  private final ZoneId _timeZone;

  public LocalDateEpochDaysConverter(final ZoneId timeZone) {
    _timeZone = timeZone;
  }

  public LocalDateEpochDaysConverter(TimeZone timeZone) {
    _timeZone = ZoneId.of(timeZone.getID());
  }

  public LocalDateEpochDaysConverter() {
    _timeZone = ZoneOffset.UTC; // TODO: clean this up
  }

  //-------------------------------------------------------------------------
  public TimeZone getTimeZone() {
    return TimeZone.getTimeZone(_timeZone.getId());
  }
  
  public ZoneId getTimeZone310() {
    return _timeZone;
  }

  @Override
  public LocalDate convertFromInt(final int dateTime) {
    return LocalDate.ofEpochDay(dateTime);
  }

  @Override
  public List<LocalDate> convertFromInt(final IntList dateTimes) {
    final List<LocalDate> dates = new ArrayList<LocalDate>(dateTimes.size());
    final IntIterator iterator = dateTimes.iterator();
    while (iterator.hasNext()) {
      dates.add(LocalDate.ofEpochDay(iterator.next()));
    }
    return dates;
  }

  @Override
  public LocalDate[] convertFromInt(final int[] dateTimes) {
    final LocalDate[] dates = new LocalDate[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      dates[i] = LocalDate.ofEpochDay(dateTimes[i]);
    }
    return dates;
  }

  @Override
  public DoubleTimeSeries<LocalDate> convertFromInt(final DoubleTimeSeries<LocalDate> templateTS, final FastIntDoubleTimeSeries pidts) {
    final LocalDate[] dates = new LocalDate[pidts.size()];
    final Double[] values = new Double[pidts.size()];
    final Iterator<Entry<Integer, Double>> iterator = pidts.iterator();
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<Integer, Double> entry = iterator.next();
      dates[i] = LocalDate.ofEpochDay(entry.getKey());
      values[i] = entry.getValue();
      i++;
    }
    return (DoubleTimeSeries<LocalDate>) templateTS.newInstance(dates, values);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> ObjectTimeSeries<LocalDate, T> convertFromInt(ObjectTimeSeries<LocalDate, T> templateTS, FastIntObjectTimeSeries<T> pidts) {
    final LocalDate[] dates = new LocalDate[pidts.size()];
    final T[] values = (T[]) new Object[pidts.size()];
    final Iterator<Entry<Integer, T>> iterator = pidts.iterator();
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<Integer, T> entry = iterator.next();
      dates[i] = LocalDate.ofEpochDay(entry.getKey());
      values[i] = entry.getValue();
      i++;
    }
    return (ObjectTimeSeries<LocalDate, T>) templateTS.newInstance(dates, values);
  }

  @Override
  public FastIntDoubleTimeSeries convertToInt(final FastIntDoubleTimeSeries templateTS, final DoubleTimeSeries<LocalDate> dts) {
    final Iterator<Entry<LocalDate, Double>> iterator = dts.iterator();
    final int[] dates = new int[dts.size()];
    final double[] values = new double[dts.size()];
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<LocalDate, Double> entry = iterator.next();
      dates[i] = (int) (entry.getKey().toEpochDay());
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dates, values);
  }


  @SuppressWarnings("unchecked")
  @Override
  public <T> FastIntObjectTimeSeries<T> convertToInt(FastIntObjectTimeSeries<T> templateTS, ObjectTimeSeries<LocalDate, T> dts) {
    final Iterator<Entry<LocalDate, T>> iterator = dts.iterator();
    final int[] dates = new int[dts.size()];
    final T[] values = (T[]) new Object[dts.size()];
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<LocalDate, T> entry = iterator.next();
      dates[i] = (int) (entry.getKey().toEpochDay());
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dates, values);
  }
  
  @Override
  public int convertToInt(final LocalDate dateTime) {
    return (int) dateTime.toEpochDay();
  }

  @Override
  public IntList convertToInt(final List<LocalDate> dateTimes) {
    final IntList result = new IntArrayList(dateTimes.size());
    for (final LocalDate date : dateTimes) {
      result.add((int) (date.toEpochDay()));
    }
    return result;
  }

  @Override
  public int[] convertToInt(final LocalDate[] dates) {
    final int[] results = new int[dates.length];
    for (int i = 0; i < dates.length; i++) {
      results[i] = (int) (dates[i].toEpochDay());
    }
    return results;
  }

  @Override
  public LocalDate convertFromLong(final long date) {
    return LocalDate.ofEpochDay(date);
  }

  @Override
  public List<LocalDate> convertFromLong(final LongList dateTimes) {
    final List<LocalDate> dates = new ArrayList<LocalDate>(dateTimes.size());
    final LongIterator iterator = dateTimes.iterator();
    while (iterator.hasNext()) {
      dates.add(LocalDate.ofEpochDay(iterator.nextLong()));
    }
    return dates;
  }

  @Override
  public LocalDate[] convertFromLong(final long[] dateTimes) {
    final LocalDate[] dates = new LocalDate[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      dates[i] = LocalDate.ofEpochDay(dateTimes[i]);
    }
    return dates;
  }

  @Override
  public long convertToLong(final LocalDate dateTime) {
    return dateTime.toEpochDay();
  }

  @Override
  public LongList convertToLong(final List<LocalDate> dateTimes) {
    final LongList result = new LongArrayList(dateTimes.size());
    for (final LocalDate date : dateTimes) {
      result.add(date.toEpochDay());
    }
    return result;
  }

  @Override
  public long[] convertToLong(final LocalDate[] dateTimes) {
    final long[] results = new long[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      results[i] = dateTimes[i].toEpochDay();
    }
    return results;
  }

  @Override
  public DoubleTimeSeries<LocalDate> convertFromLong(final DoubleTimeSeries<LocalDate> templateTS, final FastLongDoubleTimeSeries pldts) {
    final LocalDate[] dateTimes = new LocalDate[pldts.size()];
    final Double[] values = new Double[pldts.size()];
    int i = 0;
    final Iterator<Entry<Long, Double>> iterator = pldts.iterator();
    while (iterator.hasNext()) {
      final Entry<Long, Double> entry = iterator.next();
      dateTimes[i] = LocalDate.ofEpochDay(entry.getKey());
      values[i] = entry.getValue();
      i++;
    }
    return (DoubleTimeSeries<LocalDate>) templateTS.newInstance(dateTimes, values);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> ObjectTimeSeries<LocalDate, T> convertFromLong(ObjectTimeSeries<LocalDate, T> templateTS, FastLongObjectTimeSeries<T> pldts) {
    final LocalDate[] dateTimes = new LocalDate[pldts.size()];
    final T[] values = (T[]) new Object[pldts.size()];
    int i = 0;
    final Iterator<Entry<Long, T>> iterator = pldts.iterator();
    while (iterator.hasNext()) {
      final Entry<Long, T> entry = iterator.next();
      dateTimes[i] = LocalDate.ofEpochDay(entry.getKey());
      values[i] = entry.getValue();
      i++;
    }
    return (ObjectTimeSeries<LocalDate, T>) templateTS.newInstance(dateTimes, values);
  }

  @Override
  public FastLongDoubleTimeSeries convertToLong(final FastLongDoubleTimeSeries templateTS, final DoubleTimeSeries<LocalDate> dts) {
    final Iterator<Entry<LocalDate, Double>> iterator = dts.iterator();
    final long[] dateTimes = new long[dts.size()];
    final double[] values = new double[dts.size()];
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<LocalDate, Double> entry = iterator.next(); 
      dateTimes[i] = entry.getKey().toEpochDay();
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dateTimes, values);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> FastLongObjectTimeSeries<T> convertToLong(
      FastLongObjectTimeSeries<T> templateTS, ObjectTimeSeries<LocalDate, T> dts) {
    final Iterator<Entry<LocalDate, T>> iterator = dts.iterator();
    final long[] dateTimes = new long[dts.size()];
    final T[] values = (T[]) new Object[dts.size()];
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<LocalDate, T> entry = iterator.next(); 
      dateTimes[i] = entry.getKey().toEpochDay();
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dateTimes, values);
  }

  @Override
  public <T> Pair<LocalDate, T> makePair(LocalDate dateTime, T value) {
    return Pair.of(dateTime, value);
  }

}
