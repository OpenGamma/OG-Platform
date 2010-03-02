/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.List;

import com.opengamma.util.Pair;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public interface DateTimeConverter<DATE_TYPE> {

  // Long methods

  long convertToLong(DATE_TYPE dateTime);

  FastLongDoubleTimeSeries convertToLong(FastLongDoubleTimeSeries templateTS, DoubleTimeSeries<DATE_TYPE> dts);

  DATE_TYPE convertFromLong(long dateTime);

  DoubleTimeSeries<DATE_TYPE> convertFromLong(DoubleTimeSeries<DATE_TYPE> templateTS, FastLongDoubleTimeSeries pldts);

  List<DATE_TYPE> convertFromLong(LongList dateTimes);

  LongList convertToLong(List<DATE_TYPE> dateTimes);

  DATE_TYPE[] convertFromLong(long[] dateTimes);

  long[] convertToLong(DATE_TYPE[] dateTimes);

  // Integer methods

  int convertToInt(DATE_TYPE dateTime);

  FastIntDoubleTimeSeries convertToInt(FastIntDoubleTimeSeries templateTS, DoubleTimeSeries<DATE_TYPE> dts);

  DATE_TYPE convertFromInt(int dateTime);

  DoubleTimeSeries<DATE_TYPE> convertFromInt(DoubleTimeSeries<DATE_TYPE> templateTS, FastIntDoubleTimeSeries pidts);

  List<DATE_TYPE> convertFromInt(IntList dateTimes);

  IntList convertToInt(List<DATE_TYPE> dateTimes);

  DATE_TYPE[] convertFromInt(int[] dateTimes);

  int[] convertToInt(DATE_TYPE[] dateTimes);

  // Other

  Pair<DATE_TYPE, Double> makePair(DATE_TYPE dateTime, Double value);
}
