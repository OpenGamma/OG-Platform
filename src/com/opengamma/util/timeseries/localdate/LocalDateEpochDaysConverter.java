/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.Map.Entry;

import javax.time.calendar.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * @author jim
 * 
 */
public class LocalDateEpochDaysConverter implements DateTimeConverter<LocalDate> {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(LocalDateEpochDaysConverter.class);
  public static final long MILLIS_PER_DAY = 1000 * 3600 * 24;

  final javax.time.calendar.TimeZone _timeZone;

  public LocalDateEpochDaysConverter(final javax.time.calendar.TimeZone timeZone) {
    _timeZone = timeZone;
  }
  
  public LocalDateEpochDaysConverter(TimeZone timeZone) {
    _timeZone = javax.time.calendar.TimeZone.of(timeZone.getID());
  }

  public LocalDateEpochDaysConverter() {
    _timeZone = javax.time.calendar.TimeZone.UTC; // TODO: clean this up
  }
  
  public TimeZone getTimeZone() {
    return TimeZone.getTimeZone(_timeZone.getID());
  }
  
  public javax.time.calendar.TimeZone getTimeZone310() {
    return _timeZone;
  }

  @Override
  public LocalDate convertFromInt(final int dateTime) {
    return LocalDate.fromEpochDays(dateTime);
  }

  @Override
  public List<LocalDate> convertFromInt(final IntList dateTimes) {
    final List<LocalDate> dates = new ArrayList<LocalDate>(dateTimes.size());
    final IntIterator iterator = dateTimes.iterator();
    while (iterator.hasNext()) {
      dates.add(LocalDate.fromEpochDays(iterator.next()));
    }
    return dates;
  }

  @Override
  public LocalDate[] convertFromInt(final int[] dateTimes) {
    final LocalDate[] dates = new LocalDate[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      dates[i] = LocalDate.fromEpochDays(dateTimes[i]);
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
      dates[i] = LocalDate.fromEpochDays(entry.getKey());
      values[i] = entry.getValue();
      i++;
    }
    return (DoubleTimeSeries<LocalDate>) templateTS.newInstance(dates, values);
  }

  @Override
  public FastIntDoubleTimeSeries convertToInt(final FastIntDoubleTimeSeries templateTS, final DoubleTimeSeries<LocalDate> dts) {
    final Iterator<Entry<LocalDate, Double>> iterator = dts.iterator();
    final int[] dates = new int[dts.size()];
    final double[] values = new double[dts.size()];
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<LocalDate, Double> entry = iterator.next();
      dates[i] = (int) (entry.getKey().toEpochDays());
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dates, values);
  }

  @Override
  public int convertToInt(final LocalDate dateTime) {
    return (int) dateTime.toEpochDays();
  }

  @Override
  public IntList convertToInt(final List<LocalDate> dateTimes) {
    final IntList result = new IntArrayList(dateTimes.size());
    for (final LocalDate date : dateTimes) {
      result.add((int) (date.toEpochDays()));
    }
    return result;
  }

  @Override
  public int[] convertToInt(final LocalDate[] dates) {
    final int[] results = new int[dates.length];
    for (int i = 0; i < dates.length; i++) {
      results[i] = (int) (dates[i].toEpochDays());
    }
    return results;
  }

  @Override
  public LocalDate convertFromLong(final long date) {
    return LocalDate.fromEpochDays(date);
  }

  @Override
  public List<LocalDate> convertFromLong(final LongList dateTimes) {
    final List<LocalDate> dates = new ArrayList<LocalDate>(dateTimes.size());
    final LongIterator iterator = dateTimes.iterator();
    while (iterator.hasNext()) {
      dates.add(LocalDate.fromEpochDays(iterator.nextLong()));
    }
    return dates;
  }

  @Override
  public LocalDate[] convertFromLong(final long[] dateTimes) {
    final LocalDate[] dates = new LocalDate[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      dates[i] = LocalDate.fromEpochDays(dateTimes[i]);
    }
    return dates;
  }

  @Override
  public long convertToLong(final LocalDate dateTime) {
    return dateTime.toEpochDays();
  }

  @Override
  public LongList convertToLong(final List<LocalDate> dateTimes) {
    final LongList result = new LongArrayList(dateTimes.size());
    for (final LocalDate date : dateTimes) {
      result.add(date.toEpochDays());
    }
    return result;
  }

  @Override
  public long[] convertToLong(final LocalDate[] dateTimes) {
    final long[] results = new long[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      results[i] = dateTimes[i].toEpochDays();
    }
    return results;
  }

  @Override
  public Pair<LocalDate, Double> makePair(final LocalDate dateTime, final Double value) {
    return new Pair<LocalDate, Double>(dateTime, value);
  }

  @Override
  public DoubleTimeSeries<LocalDate> convertFromLong(final DoubleTimeSeries<LocalDate> templateTS, final FastLongDoubleTimeSeries pldts) {
    final LocalDate[] dateTimes = new LocalDate[pldts.size()];
    final Double[] values = new Double[pldts.size()];
    int i = 0;
    final Iterator<Entry<Long, Double>> iterator = pldts.iterator();
    while (iterator.hasNext()) {
      final Entry<Long, Double> entry = iterator.next();
      dateTimes[i] = LocalDate.fromEpochDays(entry.getKey());
      values[i] = entry.getValue();
      i++;
    }
    return (DoubleTimeSeries<LocalDate>) templateTS.newInstance(dateTimes, values);
  }

  @Override
  public FastLongDoubleTimeSeries convertToLong(final FastLongDoubleTimeSeries templateTS, final DoubleTimeSeries<LocalDate> dts) {
    final Iterator<Entry<LocalDate, Double>> iterator = dts.iterator();
    final long[] dateTimes = new long[dts.size()];
    final double[] values = new double[dts.size()];
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<LocalDate, Double> entry = iterator.next(); 
      dateTimes[i] = entry.getKey().toEpochDays();
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dateTimes, values);
  }

}
