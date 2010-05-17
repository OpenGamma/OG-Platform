/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
  public abstract int size();

  public abstract boolean isEmpty();

  public abstract T getValueFast(long time);

  public abstract T getValueAtFast(int index);

  public abstract long getTimeFast(int index);

  public abstract long getLatestTimeFast();

  public abstract T getLatestValueFast();

  public abstract long getEarliestTimeFast();

  public abstract T getEarliestValueFast();

  public abstract ObjectIterator<T> valuesIteratorFast();

  public abstract ObjectList<T> valuesFast();

  public abstract LongIterator timesIteratorFast();

  public abstract LongList timesFast();

  public abstract ObjectIterator<Long2ObjectMap.Entry<T>> iteratorFast();

  public abstract FastLongObjectTimeSeries<T> subSeriesFast(long startTime, long endTime);
  
  public abstract FastLongObjectTimeSeries<T> subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd);

  public abstract FastLongObjectTimeSeries<T> headFast(int items);

  public abstract FastLongObjectTimeSeries<T> tailFast(int items);

  public abstract T[] valuesArrayFast();

  public abstract long[] timesArrayFast();

  public abstract FastLongObjectTimeSeries<T> newInstanceFast(long[] times, T[] values);
  
  public abstract FastLongObjectTimeSeries<T> operate(final FastLongObjectTimeSeries<T> other, final BinaryOperator<T> operator);
  
  public abstract FastLongObjectTimeSeries<T> operate(final FastIntObjectTimeSeries<T> other, final BinaryOperator<T> operator);
  
  public abstract FastLongObjectTimeSeries<T> operate(final T other, final BinaryOperator<T> operator);
  
  public abstract FastLongObjectTimeSeries<T> unionOperate(final FastLongObjectTimeSeries<T> other, final BinaryOperator<T> operator);
  
  public abstract FastLongObjectTimeSeries<T> unionOperate(final FastIntObjectTimeSeries<T> other, final BinaryOperator<T> operator);
  
  public abstract FastLongObjectTimeSeries<T> operate(final UnaryOperator<T> operator);
  
  public abstract FastLongObjectTimeSeries<T> lag(final int days);
}
