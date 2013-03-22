/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint;

import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.Map.Entry;

import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;

/**
 * 
 */
public interface FastLongDoubleTimeSeries extends FastTimeSeries<Long>, Iterable<Entry<Long, Double>> {
  int size();

  boolean isEmpty();

  double getValueFast(long time);

  double getValueAtFast(int index);

  long getTimeFast(int index);

  long getLatestTimeFast();

  double getLatestValueFast();

  long getEarliestTimeFast();

  double getEarliestValueFast();

  DoubleIterator valuesIteratorFast();

  DoubleList valuesFast();

  LongIterator timesIteratorFast();

  LongList timesFast();

  ObjectIterator<Long2DoubleMap.Entry> iteratorFast();

  FastLongDoubleTimeSeries subSeriesFast(long startTime, long endTime);

  FastLongDoubleTimeSeries subSeriesFast(long startTime, boolean includeStart, long endTime,
      boolean includeEnd);

  FastLongDoubleTimeSeries headFast(int items);

  FastLongDoubleTimeSeries tailFast(int items);

  long[] timesArrayFast();

  FastLongDoubleTimeSeries newInstanceFast(long[] times, double[] values);

  FastLongDoubleTimeSeries operate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);

  FastLongDoubleTimeSeries operate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);

  FastLongDoubleTimeSeries operate(final double other, final BinaryOperator operator);

  FastLongDoubleTimeSeries unionOperate(final FastLongDoubleTimeSeries other,
      final BinaryOperator operator);

  FastLongDoubleTimeSeries unionOperate(final FastIntDoubleTimeSeries other,
      final BinaryOperator operator);

  FastLongDoubleTimeSeries operate(final UnaryOperator operator);

  FastLongDoubleTimeSeries lag(final int days);
}
