/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.zoneddatetime;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;
import com.opengamma.lambdava.tuple.Pair;

/**
 * Converter for ZonedDateTime and EpochMillis.
 */
public class ZonedDateTimeEpochMillisConverter implements DateTimeConverter<ZonedDateTime> {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(ZonedDateTimeEpochMillisConverter.class);

  private final ZoneId _timeZone;
  /**
   * Offset from midnight to make all the converted ZonedDateTimes
   */
  private long _timeOffset;
  
  public ZonedDateTimeEpochMillisConverter(final ZoneId timeZone) {
    _timeZone = timeZone;
  }
  
  public ZonedDateTimeEpochMillisConverter(final java.util.TimeZone timeZone) {
    _timeZone = ZoneId.of(timeZone.getID());
  }

  public ZonedDateTimeEpochMillisConverter() {
    _timeZone = ZoneOffset.UTC; //TimeZone.of(java.util.TimeZone.getDefault().getID()) 
  }
  
  /**
   * DO NOT USE THIS METHOD UNLESS YOU KNOW EXACTLY WHAT IT'S FOR.
   * The big problem is that this code assumes that the series within is midnight-aligned, and
   * if you start storing these, things will go south rapidly as the back-end can't tell they're
   * offset.
   * @param timeZone the timeZone to use when constructing ZoneDateTimes 
   * @param time the time offset to use from midnight.  Assumes input data is all midnight aligned.
   */
  public ZonedDateTimeEpochMillisConverter(final ZoneId timeZone, final LocalTime time) {
    _timeZone = timeZone;
    _timeOffset = time.toNanoOfDay() / 1000000L; // TODO: fix with next version of JSR310.
  }

  public TimeZone getTimeZone() {
    return TimeZone.getTimeZone(_timeZone.getId());
  }
  
  public ZoneId getTimeZone310() {
    return _timeZone;
  }

  @Override
  public ZonedDateTime convertFromInt(final int dateTime) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public List<ZonedDateTime> convertFromInt(final IntList dateTimes) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public ZonedDateTime[] convertFromInt(final int[] dateTimes) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public DoubleTimeSeries<ZonedDateTime> convertFromInt(final DoubleTimeSeries<ZonedDateTime> emptyMutableTS, final FastIntDoubleTimeSeries pidts) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }
  
  @Override
  public <T> ObjectTimeSeries<ZonedDateTime, T> convertFromInt(ObjectTimeSeries<ZonedDateTime, T> templateTS, FastIntObjectTimeSeries<T> pidts) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public FastIntDoubleTimeSeries convertToInt(final FastIntDoubleTimeSeries emptyMutableTS, final DoubleTimeSeries<ZonedDateTime> dts) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }
  
  @Override
  public <T> FastIntObjectTimeSeries<T> convertToInt(FastIntObjectTimeSeries<T> templateTS, ObjectTimeSeries<ZonedDateTime, T> dts) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public int convertToInt(final ZonedDateTime dateTime) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public IntList convertToInt(final List<ZonedDateTime> dateTimes) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public int[] convertToInt(final ZonedDateTime[] dateTimes) {
    throw new UnsupportedOperationException("Can't reduce epoch milliseconds into an integer field");
  }

  @Override
  public ZonedDateTime convertFromLong(final long dateTime) {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateTime + _timeOffset), _timeZone);
  }

  @Override
  public List<ZonedDateTime> convertFromLong(final LongList dateTimes) {
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>(dateTimes.size());
    final LongIterator iterator = dateTimes.iterator();
    while (iterator.hasNext()) {
      dates.add(ZonedDateTime.ofInstant(Instant.ofEpochMilli(iterator.nextLong() + _timeOffset), _timeZone));
    }
    return dates;
  }

  @Override
  public ZonedDateTime[] convertFromLong(final long[] dateTimes) {
    final ZonedDateTime[] dates = new ZonedDateTime[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      dates[i] = ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateTimes[i] + _timeOffset), _timeZone);
    }
    return dates;
  }

  @Override
  public long convertToLong(final ZonedDateTime dateTime) {
    return dateTime.toInstant().toEpochMilli() - _timeOffset;
  }

  @Override
  public LongList convertToLong(final List<ZonedDateTime> dateTimes) {
    final LongList result = new LongArrayList(dateTimes.size());
    for (final ZonedDateTime date : dateTimes) {
      result.add(date.toInstant().toEpochMilli() - _timeOffset);
    }
    return result;
  }

  @Override
  public long[] convertToLong(final ZonedDateTime[] dateTimes) {
    final long[] results = new long[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      results[i] = dateTimes[i].toInstant().toEpochMilli() - _timeOffset;
    }
    return results;
  }

  @Override
  public DoubleTimeSeries<ZonedDateTime> convertFromLong(final DoubleTimeSeries<ZonedDateTime> templateTS, final FastLongDoubleTimeSeries pldts) {
    final ZonedDateTime[] dateTimes = new ZonedDateTime[pldts.size()];
    final Double[] values = new Double[pldts.size()];
    int i = 0;
    final Iterator<Entry<Long, Double>> iterator = pldts.iterator();
    while (iterator.hasNext()) {
      final Entry<Long, Double> entry = iterator.next();
      
      final ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(entry.getKey() + _timeOffset), _timeZone);
      dateTimes[i] = date;
      values[i] = entry.getValue();
      i++;
    }
    return (DoubleTimeSeries<ZonedDateTime>) templateTS.newInstance(dateTimes, values);
  }
  
  @Override
  public <T> ObjectTimeSeries<ZonedDateTime, T> convertFromLong(
      ObjectTimeSeries<ZonedDateTime, T> templateTS,
      FastLongObjectTimeSeries<T> pldts) {
    final ZonedDateTime[] dateTimes = new ZonedDateTime[pldts.size()];
    @SuppressWarnings("unchecked")
    final T[] values = (T[]) new Object[pldts.size()];
    int i = 0;
    final Iterator<Entry<Long, T>> iterator = pldts.iterator();
    while (iterator.hasNext()) {
      final Entry<Long, T> entry = iterator.next();
      
      final ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(entry.getKey() + _timeOffset), _timeZone);
      dateTimes[i] = date;
      values[i] = entry.getValue();
      i++;
    }
    return (ObjectTimeSeries<ZonedDateTime, T>) templateTS.newInstance(dateTimes, values);
  }

  @Override
  public FastLongDoubleTimeSeries convertToLong(final FastLongDoubleTimeSeries templateTS, final DoubleTimeSeries<ZonedDateTime> dts) {
    final long[] dateTimes = new long[dts.size()];
    final double[] values = new double[dts.size()];
    int i = 0;
    final Iterator<Entry<ZonedDateTime, Double>> iterator = dts.iterator();
    while (iterator.hasNext()) {
      final Entry<ZonedDateTime, Double> entry = iterator.next();
      final long epochMillis = entry.getKey().toInstant().toEpochMilli() - _timeOffset;
      dateTimes[i] = epochMillis;
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dateTimes, values);
  }

  @Override
  public <T> FastLongObjectTimeSeries<T> convertToLong(FastLongObjectTimeSeries<T> templateTS, ObjectTimeSeries<ZonedDateTime, T> dts) {
    final long[] dateTimes = new long[dts.size()];
    @SuppressWarnings("unchecked")
    final T[] values = (T[]) new Object[dts.size()];
    int i = 0;
    final Iterator<Entry<ZonedDateTime, T>> iterator = dts.iterator();
    while (iterator.hasNext()) {
      final Entry<ZonedDateTime, T> entry = iterator.next();
      final long epochMillis = entry.getKey().toInstant().toEpochMilli() - _timeOffset;
      dateTimes[i] = epochMillis;
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dateTimes, values);
  }

  @Override
  public <T> Pair<ZonedDateTime, T> makePair(ZonedDateTime dateTime, T value) {
    return Pair.of(dateTime, value);
  }

}
