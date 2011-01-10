/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint.object;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Map.Entry;

import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.fast.FastObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;

public interface FastLongObjectTimeSeries<T> extends FastObjectTimeSeries<Long, T>, Iterable<Entry<Long, T>> {
  int size();

  boolean isEmpty();

  T getValueFast(long time);

  T getValueAtFast(int index);

  long getTimeFast(int index);

  long getLatestTimeFast();

  T getLatestValueFast();

  long getEarliestTimeFast();

  T getEarliestValueFast();

  ObjectIterator<T> valuesIteratorFast();

  ObjectList<T> valuesFast();

  LongIterator timesIteratorFast();

  LongList timesFast();

  ObjectIterator<Long2ObjectMap.Entry<T>> iteratorFast();

  FastLongObjectTimeSeries<T> subSeriesFast(long startTime, long endTime);
  
  FastLongObjectTimeSeries<T> subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd);

  FastLongObjectTimeSeries<T> headFast(int items);

  FastLongObjectTimeSeries<T> tailFast(int items);

  T[] valuesArrayFast();

  long[] timesArrayFast();

  FastLongObjectTimeSeries<T> newInstanceFast(long[] times, T[] values);
  
  FastLongObjectTimeSeries<T> operate(final FastLongObjectTimeSeries<T> other, final BinaryOperator<T> operator);
  
  FastLongObjectTimeSeries<T> operate(final FastIntObjectTimeSeries<T> other, final BinaryOperator<T> operator);
  
  FastLongObjectTimeSeries<T> operate(final T other, final BinaryOperator<T> operator);
  
  FastLongObjectTimeSeries<T> unionOperate(final FastLongObjectTimeSeries<T> other, final BinaryOperator<T> operator);
  
  FastLongObjectTimeSeries<T> unionOperate(final FastIntObjectTimeSeries<T> other, final BinaryOperator<T> operator);
  
  FastLongObjectTimeSeries<T> operate(final UnaryOperator<T> operator);
  
  FastLongObjectTimeSeries<T> lag(final int days);
}
