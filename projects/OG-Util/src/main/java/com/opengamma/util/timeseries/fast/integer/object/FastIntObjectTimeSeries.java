/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer.object;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;

import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.fast.FastObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;

/**
 * 
 * @param <T> The type of the data
 */
public interface FastIntObjectTimeSeries<T> extends FastObjectTimeSeries<Integer, T> {
  int size();

  boolean isEmpty();

  T getValueFast(int time);

  T getValueAtFast(int index);

  int getTimeFast(int index);

  int getLatestTimeFast();

  T getLatestValueFast();

  int getEarliestTimeFast();

  T getEarliestValueFast();

  ObjectIterator<T> valuesIteratorFast();

  ObjectList<T> valuesFast();

  IntIterator timesIteratorFast();

  ObjectIterator<Int2ObjectMap.Entry<T>> iteratorFast();

  IntList timesFast();

  FastIntObjectTimeSeries<T> subSeriesFast(int startTime, int endTime);

  FastIntObjectTimeSeries<T> subSeriesFast(int startTime, boolean includeStart, int endTime, boolean includeEnd);

  FastIntObjectTimeSeries<T> headFast(int items);

  FastIntObjectTimeSeries<T> tailFast(int items);

  T[] valuesArrayFast();

  int[] timesArrayFast();

  FastIntObjectTimeSeries<T> newInstanceFast(int[] times, T[] values);

  FastIntObjectTimeSeries<T> operate(final FastLongObjectTimeSeries<T> other, final BinaryOperator<T> operator);

  FastIntObjectTimeSeries<T> operate(final FastIntObjectTimeSeries<T> other, final BinaryOperator<T> operator);

  FastIntObjectTimeSeries<T> operate(final T other, final BinaryOperator<T> operator);

  FastIntObjectTimeSeries<T> operate(final UnaryOperator<T> operator);

  FastIntObjectTimeSeries<T> unionOperate(final FastLongObjectTimeSeries<T> other, final BinaryOperator<T> operator);

  FastIntObjectTimeSeries<T> unionOperate(final FastIntObjectTimeSeries<T> other, final BinaryOperator<T> operator);

  FastIntObjectTimeSeries<T> lag(final int days);

}
