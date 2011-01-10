/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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

public interface FastIntObjectTimeSeries<T> extends FastObjectTimeSeries<Integer, T> {
  public abstract int size();

  public abstract boolean isEmpty();

  public abstract T getValueFast(int time);

  public abstract T getValueAtFast(int index);

  public abstract int getTimeFast(int index);

  public abstract int getLatestTimeFast();

  public abstract T getLatestValueFast();

  public abstract int getEarliestTimeFast();

  public abstract T getEarliestValueFast();

  public abstract ObjectIterator<T> valuesIteratorFast();

  public abstract ObjectList<T> valuesFast();

  public abstract IntIterator timesIteratorFast();

  public abstract ObjectIterator<Int2ObjectMap.Entry<T>> iteratorFast();

  public abstract IntList timesFast();

  public abstract FastIntObjectTimeSeries<T> subSeriesFast(int startTime, int endTime);
  
  public abstract FastIntObjectTimeSeries<T> subSeriesFast(int startTime, boolean includeStart, int endTime, boolean includeEnd);

  public abstract FastIntObjectTimeSeries<T> headFast(int items);

  public abstract FastIntObjectTimeSeries<T> tailFast(int items);

  public abstract T[] valuesArrayFast();

  public abstract int[] timesArrayFast();

  public abstract FastIntObjectTimeSeries<T> newInstanceFast(int[] times, T[] values);

  public abstract FastIntObjectTimeSeries<T> operate(final FastLongObjectTimeSeries<T> other, final BinaryOperator<T> operator);
  
  public abstract FastIntObjectTimeSeries<T> operate(final FastIntObjectTimeSeries<T> other, final BinaryOperator<T> operator);
  
  public abstract FastIntObjectTimeSeries<T> operate(final T other, final BinaryOperator<T> operator);
  
  public abstract FastIntObjectTimeSeries<T> operate(final UnaryOperator<T> operator);
  
  public abstract FastIntObjectTimeSeries<T> unionOperate(final FastLongObjectTimeSeries<T> other, final BinaryOperator<T> operator);
  
  public abstract FastIntObjectTimeSeries<T> unionOperate(final FastIntObjectTimeSeries<T> other, final BinaryOperator<T> operator);
  
  public abstract FastIntObjectTimeSeries<T> lag(final int days);

}
