/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.date.time;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZoneId;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class DateEpochMillisConverter implements DateTimeConverter<Date> {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(DateEpochMillisConverter.class);
  /** Number of milliseconds in a day */
  public static final long MILLIS_PER_DAY = 1000 * 3600 * 24;
  private ThreadLocal<Calendar> _calendar = new ThreadLocal<Calendar>() {
    @Override
    protected Calendar initialValue() {
      return Calendar.getInstance(_timeZone);
    }
  };
  private final TimeZone _timeZone;

  public DateEpochMillisConverter(final TimeZone timeZone) {
    _timeZone = timeZone;
  }

  public DateEpochMillisConverter() {
    _timeZone = TimeZone.getDefault();
  }

  public TimeZone getTimeZone() {
    return _timeZone;
  }
  
  public ThreadLocal<Calendar> getCalendar() {
    return _calendar;
  }
  
  public ZoneId getTimeZone310() {
    return ZoneId.of(_timeZone.getID());
  }

  @Override
  public Date convertFromInt(final int dateTime) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public List<Date> convertFromInt(final IntList dateTimes) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }
  
  @Override
  public <T> ObjectTimeSeries<Date, T> convertFromInt(
      ObjectTimeSeries<Date, T> templateTS, FastIntObjectTimeSeries<T> pidts) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public Date[] convertFromInt(final int[] dateTimes) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public DoubleTimeSeries<Date> convertFromInt(final DoubleTimeSeries<Date> emptyMutableTS, final FastIntDoubleTimeSeries pidts) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public FastIntDoubleTimeSeries convertToInt(final FastIntDoubleTimeSeries emptyMutableTS, final DoubleTimeSeries<Date> dts) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }
  
  @Override
  public <T> FastIntObjectTimeSeries<T> convertToInt(FastIntObjectTimeSeries<T> templateTS, ObjectTimeSeries<Date, T> dts) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public int convertToInt(final Date dateTime) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public IntList convertToInt(final List<Date> dateTimes) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public int[] convertToInt(final Date[] dateTimes) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public Date convertFromLong(final long dateTime) {
    final Calendar cal = _calendar.get();
    cal.setTimeInMillis(dateTime);
    return cal.getTime();
  }

  @Override
  public List<Date> convertFromLong(final LongList dateTimes) {
    final Calendar cal = _calendar.get();
    final List<Date> dates = new ArrayList<Date>(dateTimes.size());
    final LongIterator iterator = dateTimes.iterator();
    while (iterator.hasNext()) {
      cal.setTimeInMillis(iterator.nextLong());
      dates.add(cal.getTime());
    }
    return dates;
  }

  @Override
  public Date[] convertFromLong(final long[] dateTimes) {
    final Calendar cal = _calendar.get();
    final Date[] dates = new Date[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      cal.setTimeInMillis(dateTimes[i]);
      dates[i] = cal.getTime();
    }
    return dates;
  }

  @Override
  public long convertToLong(final Date dateTime) {
    return dateTime.getTime();
  }

  @Override
  public LongList convertToLong(final List<Date> dateTimes) {
    final LongList result = new LongArrayList(dateTimes.size());
    for (final Date date : dateTimes) {
      result.add(date.getTime());
    }
    return result;
  }

  @Override
  public long[] convertToLong(final Date[] dateTimes) {
    final long[] results = new long[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      results[i] = dateTimes[i].getTime();
    }
    return results;
  }

  @Override
  public DoubleTimeSeries<Date> convertFromLong(final DoubleTimeSeries<Date> templateTS, final FastLongDoubleTimeSeries pldts) {
    final Calendar cal = _calendar.get();
    final Date[] dateTimes = new Date[pldts.size()];
    final Double[] values = new Double[pldts.size()];
    int i = 0;
    final Iterator<Entry<Long, Double>> iterator = pldts.iterator();
    while (iterator.hasNext()) {
      final Entry<Long, Double> entry = iterator.next();
      cal.setTimeInMillis(entry.getKey());
      final Date date = cal.getTime();
      dateTimes[i] = date;
      values[i] = entry.getValue();
      i++;
    }
    return (DoubleTimeSeries<Date>) templateTS.newInstance(dateTimes, values);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> ObjectTimeSeries<Date, T> convertFromLong(
      ObjectTimeSeries<Date, T> templateTS, FastLongObjectTimeSeries<T> pldts) {
    final Calendar cal = _calendar.get();
    final Date[] dateTimes = new Date[pldts.size()];
    final T[] values = (T[]) new Object[pldts.size()];
    int i = 0;
    final Iterator<Entry<Long, T>> iterator = pldts.iterator();
    while (iterator.hasNext()) {
      final Entry<Long, T> entry = iterator.next();
      cal.setTimeInMillis(entry.getKey());
      final Date date = cal.getTime();
      dateTimes[i] = date;
      values[i] = entry.getValue();
      i++;
    }
    return (ObjectTimeSeries<Date, T>) templateTS.newInstance(dateTimes, values);
  }

  @Override
  public FastLongDoubleTimeSeries convertToLong(final FastLongDoubleTimeSeries templateTS, final DoubleTimeSeries<Date> dts) {
    final long[] dateTimes = new long[dts.size()];
    final double[] values = new double[dts.size()];
    int i = 0;
    final Iterator<Entry<Date, Double>> iterator = dts.iterator();
    while (iterator.hasNext()) {
      final Entry<Date, Double> entry = iterator.next();
      final long epochMillis = entry.getKey().getTime();
      dateTimes[i] = epochMillis;
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dateTimes, values);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> FastLongObjectTimeSeries<T> convertToLong(FastLongObjectTimeSeries<T> templateTS, ObjectTimeSeries<Date, T> dts) {
    final long[] dateTimes = new long[dts.size()];
    final T[] values = (T[]) new Object[dts.size()];
    int i = 0;
    final Iterator<Entry<Date, T>> iterator = dts.iterator();
    while (iterator.hasNext()) {
      final Entry<Date, T> entry = iterator.next();
      final long epochMillis = entry.getKey().getTime();
      dateTimes[i] = epochMillis;
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dateTimes, values);
  }

  @Override
  public <T> Pair<Date, T> makePair(Date dateTime, T value) {
    return Pair.of(dateTime, value);
  }

}
