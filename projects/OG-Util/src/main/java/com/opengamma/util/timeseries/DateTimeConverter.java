/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.List;
import java.util.TimeZone;

import org.threeten.bp.ZoneId;

import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * @param <DATE_TYPE> The type of dates 
 */
public interface DateTimeConverter<DATE_TYPE> {

  // TimeZone methods.
  TimeZone getTimeZone();
  
  ZoneId getTimeZone310();
  // Long methods
  
  long convertToLong(DATE_TYPE dateTime);

  FastLongDoubleTimeSeries convertToLong(FastLongDoubleTimeSeries templateTS, DoubleTimeSeries<DATE_TYPE> dts);
  
  <T> FastLongObjectTimeSeries<T> convertToLong(FastLongObjectTimeSeries<T> templateTS, ObjectTimeSeries<DATE_TYPE, T> dts);

  DATE_TYPE convertFromLong(long dateTime);

  DoubleTimeSeries<DATE_TYPE> convertFromLong(DoubleTimeSeries<DATE_TYPE> templateTS, FastLongDoubleTimeSeries pldts);
  
  <T> ObjectTimeSeries<DATE_TYPE, T> convertFromLong(ObjectTimeSeries<DATE_TYPE, T> templateTS, FastLongObjectTimeSeries<T> pldts);

  List<DATE_TYPE> convertFromLong(LongList dateTimes);

  LongList convertToLong(List<DATE_TYPE> dateTimes);

  DATE_TYPE[] convertFromLong(long[] dateTimes);

  long[] convertToLong(DATE_TYPE[] dateTimes);

  // Integer methods

  int convertToInt(DATE_TYPE dateTime);

  FastIntDoubleTimeSeries convertToInt(FastIntDoubleTimeSeries templateTS, DoubleTimeSeries<DATE_TYPE> dts);
  
  <T> FastIntObjectTimeSeries<T> convertToInt(FastIntObjectTimeSeries<T> templateTS, ObjectTimeSeries<DATE_TYPE, T> dts);

  DATE_TYPE convertFromInt(int dateTime);

  DoubleTimeSeries<DATE_TYPE> convertFromInt(DoubleTimeSeries<DATE_TYPE> templateTS, FastIntDoubleTimeSeries pidts);
  
  <T> ObjectTimeSeries<DATE_TYPE, T> convertFromInt(ObjectTimeSeries<DATE_TYPE, T> templateTS, FastIntObjectTimeSeries<T> pidts);

  List<DATE_TYPE> convertFromInt(IntList dateTimes);

  IntList convertToInt(List<DATE_TYPE> dateTimes);

  DATE_TYPE[] convertFromInt(int[] dateTimes);

  int[] convertToInt(DATE_TYPE[] dateTimes);

  // Other

  <T> Pair<DATE_TYPE, T> makePair(DATE_TYPE dateTime, T value);
}
