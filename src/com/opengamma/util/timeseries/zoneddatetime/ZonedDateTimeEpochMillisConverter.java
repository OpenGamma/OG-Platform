/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

import javax.time.Instant;
import javax.time.calendar.Clock;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.Pair;
import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class ZonedDateTimeEpochMillisConverter implements DateTimeConverter<ZonedDateTime> {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(ZonedDateTimeEpochMillisConverter.class);
  public static final long MILLIS_PER_DAY = 1000 * 3600 * 24;

  final TimeZone _timeZone;

  public ZonedDateTimeEpochMillisConverter(final TimeZone timeZone) {
    _timeZone = timeZone;
  }
  
  public ZonedDateTimeEpochMillisConverter(final java.util.TimeZone timeZone) {
    _timeZone = TimeZone.of(timeZone.getID());
  }

  public ZonedDateTimeEpochMillisConverter() {
    _timeZone = TimeZone.UTC;//TimeZone.of(java.util.TimeZone.getDefault().getID()); 
  }

  public java.util.TimeZone getTimeZone() {
    return java.util.TimeZone.getTimeZone(_timeZone.getID());
  }
  
  public TimeZone getTimeZone310() {
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
  public FastIntDoubleTimeSeries convertToInt(final FastIntDoubleTimeSeries emptyMutableTS, final DoubleTimeSeries<ZonedDateTime> dts) {
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
    return ZonedDateTime.fromInstant(Instant.millis(dateTime), _timeZone);
  }

  @Override
  public List<ZonedDateTime> convertFromLong(final LongList dateTimes) {
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>(dateTimes.size());
    final LongIterator iterator = dateTimes.iterator();
    while (iterator.hasNext()) {
      dates.add(ZonedDateTime.fromInstant(Instant.millis(iterator.nextLong()), _timeZone));
    }
    return dates;
  }

  @Override
  public ZonedDateTime[] convertFromLong(final long[] dateTimes) {
    final ZonedDateTime[] dates = new ZonedDateTime[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      dates[i] = ZonedDateTime.fromInstant(Instant.millis(dateTimes[i]), _timeZone);
    }
    return dates;
  }

  @Override
  public long convertToLong(final ZonedDateTime dateTime) {
    return dateTime.toInstant().toEpochMillisLong();
  }

  @Override
  public LongList convertToLong(final List<ZonedDateTime> dateTimes) {
    final LongList result = new LongArrayList(dateTimes.size());
    for (final ZonedDateTime date : dateTimes) {
      result.add(date.toInstant().toEpochMillisLong());
    }
    return result;
  }

  @Override
  public long[] convertToLong(final ZonedDateTime[] dateTimes) {
    final long[] results = new long[dateTimes.length];
    for (int i = 0; i < dateTimes.length; i++) {
      results[i] = dateTimes[i].toInstant().toEpochMillisLong();
    }
    return results;
  }

  @Override
  public Pair<ZonedDateTime, Double> makePair(final ZonedDateTime dateTime, final Double value) {
    return new Pair<ZonedDateTime, Double>(dateTime, value);
  }

  @Override
  public DoubleTimeSeries<ZonedDateTime> convertFromLong(final DoubleTimeSeries<ZonedDateTime> templateTS, final FastLongDoubleTimeSeries pldts) {
    final ZonedDateTime[] dateTimes = new ZonedDateTime[pldts.size()];
    final Double[] values = new Double[pldts.size()];
    int i = 0;
    final Iterator<Entry<Long, Double>> iterator = pldts.iterator();
    while (iterator.hasNext()) {
      final Entry<Long, Double> entry = iterator.next();
      
      final ZonedDateTime date = ZonedDateTime.fromInstant(Instant.millis(entry.getKey()), _timeZone);
      dateTimes[i] = date;
      values[i] = entry.getValue();
      i++;
    }
    return (DoubleTimeSeries<ZonedDateTime>) templateTS.newInstance(dateTimes, values);
  }

  @Override
  public FastLongDoubleTimeSeries convertToLong(final FastLongDoubleTimeSeries templateTS, final DoubleTimeSeries<ZonedDateTime> dts) {
    final long[] dateTimes = new long[dts.size()];
    final double[] values = new double[dts.size()];
    int i = 0;
    final Iterator<Entry<ZonedDateTime, Double>> iterator = dts.iterator();
    while (iterator.hasNext()) {
      final Entry<ZonedDateTime, Double> entry = iterator.next();
      final long epochMillis = entry.getKey().toInstant().toEpochMillisLong();
      dateTimes[i] = epochMillis;
      values[i] = entry.getValue();
      i++;
    }
    return templateTS.newInstanceFast(dateTimes, values);
  }
}
