/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.sqldate;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
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
 * @author jim
 * 
 */
public class SQLDateEpochDaysConverter implements DateTimeConverter<Date> {
  private static final Logger s_logger = LoggerFactory.getLogger(SQLDateEpochDaysConverter.class);
  // REVIEW kirk 2010-06-11 -- This doesn't really belong here. DateUtils or something?
  private static final long MILLIS_PER_DAY = 1000 * 3600 * 24;
  private ThreadLocal<Calendar> _calendar = new ThreadLocal<Calendar>() {
    @Override
    protected Calendar initialValue() {
      return Calendar.getInstance(_timeZone);
    }
  };
  private final TimeZone _timeZone;

  public SQLDateEpochDaysConverter(final TimeZone timeZone) {
    _timeZone = timeZone;
  }

  public SQLDateEpochDaysConverter() {
    _timeZone = TimeZone.getDefault();
  }
  
  public TimeZone getTimeZone() {
    return _timeZone;
  }
  
  public ZoneId getTimeZone310() {
    return ZoneId.of(_timeZone.getID());
  }

  @Override
  public Date convertFromInt(final int dateTime) {
    final Calendar cal = _calendar.get();
    cal.setTimeInMillis(dateTime * MILLIS_PER_DAY);
    return new Date(cal.getTimeInMillis());
  }

  @Override
  public List<Date> convertFromInt(final IntList dateTimes) {
    final Calendar cal = _calendar.get();
    final List<Date> dates = new ArrayList<Date>(dateTimes.size());
    final IntIterator iterator = dateTimes.iterator();
    while (iterator.hasNext()) {
      cal.setTimeInMillis(iterator.nextInt() * MILLIS_PER_DAY);
      dates.add(new Date(cal.getTimeInMillis()));
    }
    return dates;
  }

  @Override
  public Date[] convertFromInt(final int[] dateTimes) {
    final Calendar cal = _calendar.get();
    final Date[] dates = new Date[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      cal.setTimeInMillis(dateTimes[i] * MILLIS_PER_DAY);
      dates[i] = new Date(cal.getTimeInMillis());
    }
    return dates;
  }

  @Override
  public DoubleTimeSeries<Date> convertFromInt(final DoubleTimeSeries<Date> templateTS, final FastIntDoubleTimeSeries pidts) {
    final Calendar cal = _calendar.get();
    final Date[] dates = new Date[pidts.size()];
    final Double[] values = new Double[pidts.size()];
    final Iterator<Entry<Integer, Double>> iterator = pidts.iterator();
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<Integer, Double> entry = iterator.next();
      cal.setTimeInMillis(entry.getKey() * MILLIS_PER_DAY);
      final Date date = new Date(cal.getTimeInMillis());
      dates[i] = date;
      values[i] = entry.getValue();
      i++;
    }
    return (DoubleTimeSeries<Date>) templateTS.newInstance(dates, values);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> ObjectTimeSeries<Date, T> convertFromInt(
      ObjectTimeSeries<Date, T> templateTS, FastIntObjectTimeSeries<T> pidts) {
    final Calendar cal = _calendar.get();
    final Date[] dates = new Date[pidts.size()];
    final T[] values = (T[]) new Object[pidts.size()];
    final Iterator<Entry<Integer, T>> iterator = pidts.iterator();
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<Integer, T> entry = iterator.next();
      cal.setTimeInMillis(entry.getKey() * MILLIS_PER_DAY);
      final Date date = new Date(cal.getTimeInMillis());
      dates[i] = date;
      values[i] = entry.getValue();
      i++;
    }
    return (ObjectTimeSeries<Date, T>) templateTS.newInstance(dates, values);
  }

  @Override
  public FastIntDoubleTimeSeries convertToInt(final FastIntDoubleTimeSeries templateTS, final DoubleTimeSeries<Date> dts) {
    final Iterator<Entry<Date, Double>> iterator = dts.iterator();
    final int[] dates = new int[dts.size()];
    final double[] values = new double[dts.size()];
    int i = 0;
    boolean alreadyWarnedDeveloper = false;
    while (iterator.hasNext()) {
      final Entry<Date, Double> entry = iterator.next();
      final int epochDays = (int) (entry.getKey().getTime() / MILLIS_PER_DAY);
      if (s_logger.isDebugEnabled()) {
        if (!alreadyWarnedDeveloper && (entry.getKey().getTime() % MILLIS_PER_DAY != 0)) {
          s_logger.warn("losing precision on conversion of dates to ints (epoch days)");
          alreadyWarnedDeveloper = true;
        }
      }
      dates[i] = epochDays;
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dates, values);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> FastIntObjectTimeSeries<T> convertToInt(
      FastIntObjectTimeSeries<T> templateTS, ObjectTimeSeries<Date, T> dts) {
    final Iterator<Entry<Date, T>> iterator = dts.iterator();
    final int[] dates = new int[dts.size()];
    final T[] values = (T[]) new Object[dts.size()];
    int i = 0;
    boolean alreadyWarnedDeveloper = false;
    while (iterator.hasNext()) {
      final Entry<Date, T> entry = iterator.next();
      final int epochDays = (int) (entry.getKey().getTime() / MILLIS_PER_DAY);
      if (s_logger.isDebugEnabled()) {
        if (!alreadyWarnedDeveloper && (entry.getKey().getTime() % MILLIS_PER_DAY != 0)) {
          s_logger.warn("losing precision on conversion of dates to ints (epoch days)");
          alreadyWarnedDeveloper = true;
        }
      }
      dates[i] = epochDays;
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dates, values);
  }

  @Override
  public int convertToInt(final Date dateTime) {
    if (s_logger.isDebugEnabled()) {
      if (dateTime.getTime() % MILLIS_PER_DAY != 0) {
        s_logger.warn("losing precision on conversion of date to int (epoch days)");
      }
    }
    return (int) (dateTime.getTime() / MILLIS_PER_DAY);
  }

  @Override
  public IntList convertToInt(final List<Date> dateTimes) {
    final IntList result = new IntArrayList(dateTimes.size());
    boolean alreadyWarnedDeveloper = false;
    for (final Date date : dateTimes) {
      if (s_logger.isDebugEnabled()) {
        if (!alreadyWarnedDeveloper && (date.getTime() % MILLIS_PER_DAY != 0)) {
          s_logger.warn("losing precision on conversion of dates to ints (epoch days)");
          alreadyWarnedDeveloper = true;
        }
      }
      result.add((int) (date.getTime() / MILLIS_PER_DAY));
    }
    return result;
  }

  @Override
  public int[] convertToInt(final Date[] dateTimes) {
    final int[] results = new int[dateTimes.length];
    boolean alreadyWarnedDeveloper = false;
    for (int i = 0; i < dateTimes.length; i++) {
      if (s_logger.isDebugEnabled()) {
        if (!alreadyWarnedDeveloper && (dateTimes[i].getTime() % MILLIS_PER_DAY != 0)) {
          s_logger.warn("losing precision on conversion of dates to ints (epoch days)");
        }
      }
      results[i] = (int) (dateTimes[i].getTime() / MILLIS_PER_DAY);
    }
    return results;
  }

  @Override
  public Date convertFromLong(final long dateTime) {
    final Calendar cal = _calendar.get();
    cal.setTimeInMillis(dateTime * MILLIS_PER_DAY);
    return new Date(cal.getTimeInMillis());
  }

  @Override
  public List<Date> convertFromLong(final LongList dateTimes) {
    final Calendar cal = _calendar.get();
    final List<Date> dates = new ArrayList<Date>(dateTimes.size());
    final LongIterator iterator = dateTimes.iterator();
    while (iterator.hasNext()) {
      cal.setTimeInMillis(iterator.nextLong() * MILLIS_PER_DAY);
      dates.add(new Date(cal.getTimeInMillis()));
    }
    return dates;
  }

  @Override
  public Date[] convertFromLong(final long[] dateTimes) {
    final Calendar cal = _calendar.get();
    final Date[] dates = new Date[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      cal.setTimeInMillis(dateTimes[i] * MILLIS_PER_DAY);
      dates[i] = new Date(cal.getTimeInMillis());
    }
    return dates;
  }

  @Override
  public long convertToLong(final Date dateTime) {
    if (s_logger.isDebugEnabled()) {
      if (dateTime.getTime() % MILLIS_PER_DAY != 0) {
        s_logger.warn("losing precision on conversion of date to int (epoch days)");
      }
    }
    return (dateTime.getTime() / MILLIS_PER_DAY);
  }

  @Override
  public LongList convertToLong(final List<Date> dateTimes) {
    final LongList result = new LongArrayList(dateTimes.size());
    boolean alreadyWarnedDeveloper = false;
    for (final Date date : dateTimes) {
      if (s_logger.isDebugEnabled()) {
        if (!alreadyWarnedDeveloper && (date.getTime() % MILLIS_PER_DAY != 0)) {
          s_logger.warn("losing precision on conversion of dates to ints (epoch days)");
          alreadyWarnedDeveloper = true;
        }
      }
      result.add(date.getTime() / MILLIS_PER_DAY);
    }
    return result;
  }

  @Override
  public long[] convertToLong(final Date[] dateTimes) {
    final long[] results = new long[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      if (s_logger.isDebugEnabled()) {
        if (dateTimes[i].getTime() % MILLIS_PER_DAY != 0) {
          s_logger.warn("losing precision on conversion of dates to ints (epoch days)");
        }
      }
      results[i] = dateTimes[i].getTime() / MILLIS_PER_DAY;
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
      cal.setTimeInMillis(entry.getKey() * MILLIS_PER_DAY);
      final Date date = new Date(cal.getTimeInMillis());
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
      cal.setTimeInMillis(entry.getKey() * MILLIS_PER_DAY);
      final Date date = new Date(cal.getTimeInMillis());
      dateTimes[i] = date;
      values[i] = entry.getValue();
      i++;
    }
    return (ObjectTimeSeries<Date, T>) templateTS.newInstance(dateTimes, values);
  }

  @Override
  public FastLongDoubleTimeSeries convertToLong(final FastLongDoubleTimeSeries templateTS, final DoubleTimeSeries<Date> dts) {
    final Iterator<Entry<Date, Double>> iterator = dts.iterator();
    final long[] dateTimes = new long[dts.size()];
    final double[] values = new double[dts.size()];
    int i = 0;
    boolean alreadyWarnedDeveloper = false;
    while (iterator.hasNext()) {
      final Entry<Date, Double> entry = iterator.next();
      final long epochDays = entry.getKey().getTime() / MILLIS_PER_DAY;
      if (s_logger.isDebugEnabled()) {
        if (!alreadyWarnedDeveloper && (entry.getKey().getTime() % MILLIS_PER_DAY != 0)) {
          s_logger.warn("losing precision on conversion of dates to ints (epoch days)");
          alreadyWarnedDeveloper = true;
        }
      }
      dateTimes[i] = epochDays;
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dateTimes, values);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> FastLongObjectTimeSeries<T> convertToLong(FastLongObjectTimeSeries<T> templateTS, ObjectTimeSeries<Date, T> dts) {
    final Iterator<Entry<Date, T>> iterator = dts.iterator();
    final long[] dateTimes = new long[dts.size()];
    final T[] values = (T[]) new Object[dts.size()];
    int i = 0;
    boolean alreadyWarnedDeveloper = false;
    while (iterator.hasNext()) {
      final Entry<Date, T> entry = iterator.next();
      final long epochDays = entry.getKey().getTime() / MILLIS_PER_DAY;
      if (s_logger.isDebugEnabled()) {
        if (!alreadyWarnedDeveloper && (entry.getKey().getTime() % MILLIS_PER_DAY != 0)) {
          s_logger.warn("losing precision on conversion of dates to ints (epoch days)");
          alreadyWarnedDeveloper = true;
        }
      }
      dateTimes[i] = epochDays;
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
