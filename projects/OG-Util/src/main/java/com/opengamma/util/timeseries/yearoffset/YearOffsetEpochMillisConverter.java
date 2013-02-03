/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.yearoffset;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

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
public class YearOffsetEpochMillisConverter implements DateTimeConverter<Double> {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(YearOffsetEpochMillisConverter.class);
  /** Number of milliseconds in a day */
  public static final long MILLIS_PER_DAY = 1000 * 3600 * 24;
  /** Number of milliseconds in a year (assuming 365.25 days per year) */
  public static final long MILLIS_PER_YEAR = (long) (MILLIS_PER_DAY * 365.25);

  private final ZoneId _timeZone;
  private final long _offset;

  public YearOffsetEpochMillisConverter(final ZonedDateTime zonedDateTime) {
    _timeZone = zonedDateTime.getZone();
    _offset = zonedDateTime.toInstant().toEpochMilli();
  }
  
  public YearOffsetEpochMillisConverter(final java.util.TimeZone timeZone, final Date date) {
    _timeZone = ZoneId.of(timeZone.getID());
    Calendar cal = Calendar.getInstance(timeZone);
    cal.setTime(date);
    _offset = cal.getTimeInMillis(); // does this do anything over date.getTime()?  Specifically does it factor in the timeZone?
  }

  public YearOffsetEpochMillisConverter(Date date) {
    _timeZone = ZoneId.of(java.util.TimeZone.getDefault().getID());
    _offset = date.getTime();
  }

  public java.util.TimeZone getTimeZone() {
    return java.util.TimeZone.getTimeZone(_timeZone.getId());
  }
  
  public ZoneId getTimeZone310() {
    return _timeZone;
  }
  
  public ZonedDateTime getZonedOffset() {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(_offset), _timeZone);
  }

  @Override
  public Double convertFromInt(final int dateTime) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public List<Double> convertFromInt(final IntList dateTimes) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public Double[] convertFromInt(final int[] dateTimes) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public DoubleTimeSeries<Double> convertFromInt(final DoubleTimeSeries<Double> emptyMutableTS, final FastIntDoubleTimeSeries pidts) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }
  
  @Override
  public <T> ObjectTimeSeries<Double, T> convertFromInt(ObjectTimeSeries<Double, T> templateTS, FastIntObjectTimeSeries<T> pidts) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public FastIntDoubleTimeSeries convertToInt(final FastIntDoubleTimeSeries emptyMutableTS, final DoubleTimeSeries<Double> dts) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }
  
  @Override
  public <T> FastIntObjectTimeSeries<T> convertToInt(FastIntObjectTimeSeries<T> templateTS, ObjectTimeSeries<Double, T> dts) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }


  @Override
  public int convertToInt(final Double dateTime) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public IntList convertToInt(final List<Double> dateTimes) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public int[] convertToInt(final Double[] dateTimes) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public Double convertFromLong(final long dateTime) {
    return (double) (dateTime - _offset) / MILLIS_PER_YEAR;
  }

  @Override
  public List<Double> convertFromLong(final LongList dateTimes) {
    final List<Double> dates = new ArrayList<Double>(dateTimes.size());
    final LongIterator iterator = dateTimes.iterator();
    while (iterator.hasNext()) {
      dates.add((double) (iterator.nextLong() - _offset) / MILLIS_PER_YEAR);
    }
    return dates;
  }

  @Override
  public Double[] convertFromLong(final long[] dateTimes) {
    final Double[] dates = new Double[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      dates[i] = (double) ((dateTimes[i] - _offset) / MILLIS_PER_YEAR);
    }
    return dates;
  }

  /** 
   * {@inheritDoc}
   * IMPORTANT: this conversion may result in a loss of accuracy
   */
  @Override
  public long convertToLong(final Double dateTime) {
    return (long) ((dateTime * MILLIS_PER_YEAR) + _offset);
  }

  /** 
   * {@inheritDoc}
   * IMPORTANT: this conversion may result in a loss of accuracy
   */
  @Override
  public LongList convertToLong(final List<Double> dateTimes) {
    final LongList result = new LongArrayList(dateTimes.size());
    for (final Double date : dateTimes) {
      result.add((long) ((date * MILLIS_PER_YEAR) + _offset));
    }
    return result;
  }

  /** 
   * {@inheritDoc}
   * IMPORTANT: this conversion may result in a loss of accuracy
   */
  @Override
  public long[] convertToLong(final Double[] dateTimes) {
    final long[] results = new long[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      results[i] = ((long) ((dateTimes[i] * MILLIS_PER_YEAR) + _offset));
    }
    return results;
  }

  @Override
  public DoubleTimeSeries<Double> convertFromLong(final DoubleTimeSeries<Double> templateTS, final FastLongDoubleTimeSeries pldts) {
    final Double[] dateTimes = new Double[pldts.size()];
    final Double[] values = new Double[pldts.size()];
    int i = 0;
    final Iterator<Entry<Long, Double>> iterator = pldts.iterator();
    while (iterator.hasNext()) {
      final Entry<Long, Double> entry = iterator.next();
      
      final Double date = (double) ((entry.getKey() - _offset) / MILLIS_PER_YEAR);
      dateTimes[i] = date;
      values[i] = entry.getValue();
      i++;
    }
    return (DoubleTimeSeries<Double>) templateTS.newInstance(dateTimes, values);
  }
  
  @Override
  public <T> ObjectTimeSeries<Double, T> convertFromLong(ObjectTimeSeries<Double, T> templateTS, FastLongObjectTimeSeries<T> pldts) {
    final Double[] dateTimes = new Double[pldts.size()];
    @SuppressWarnings("unchecked")
    final T[] values = (T[]) new Object[pldts.size()];
    int i = 0;
    final Iterator<Entry<Long, T>> iterator = pldts.iterator();
    while (iterator.hasNext()) {
      final Entry<Long, T> entry = iterator.next();
      
      final Double date = (double) ((entry.getKey() - _offset) / MILLIS_PER_YEAR);
      dateTimes[i] = date;
      values[i] = entry.getValue();
      i++;
    }
    return (ObjectTimeSeries<Double, T>) templateTS.newInstance(dateTimes, values);
  }

  /**
   * {@inheritDoc}
   * IMPORTANT: this may result in a loss of accuracy.
   */
  @Override
  public FastLongDoubleTimeSeries convertToLong(final FastLongDoubleTimeSeries templateTS, final DoubleTimeSeries<Double> dts) {
    final long[] dateTimes = new long[dts.size()];
    final double[] values = new double[dts.size()];
    int i = 0;
    final Iterator<Entry<Double, Double>> iterator = dts.iterator();
    while (iterator.hasNext()) {
      final Entry<Double, Double> entry = iterator.next();
      final long epochMillis = (long) ((entry.getKey() * MILLIS_PER_YEAR) + _offset);
      dateTimes[i] = epochMillis;
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dateTimes, values);
  }


  
  @Override
  public <T> FastLongObjectTimeSeries<T> convertToLong(FastLongObjectTimeSeries<T> templateTS, ObjectTimeSeries<Double, T> dts) {
    final long[] dateTimes = new long[dts.size()];
    @SuppressWarnings("unchecked")
    final T[] values = (T[]) new Object[dts.size()];
    int i = 0;
    final Iterator<Entry<Double, T>> iterator = dts.iterator();
    while (iterator.hasNext()) {
      final Entry<Double, T> entry = iterator.next();
      final long epochMillis = (long) ((entry.getKey() * MILLIS_PER_YEAR) + _offset);
      dateTimes[i] = epochMillis;
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dateTimes, values);
  }

  @Override
  public <T> Pair<Double, T> makePair(Double dateTime, T value) {
    return Pair.of(dateTime, value);
  }

}
