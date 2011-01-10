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

public interface FastLongDoubleTimeSeries extends FastTimeSeries<Long>, Iterable<Entry<Long, Double>> {
  public abstract int size();

  public abstract boolean isEmpty();

  public abstract double getValueFast(long time);

  public abstract double getValueAtFast(int index);

  public abstract long getTimeFast(int index);

  public abstract long getLatestTimeFast();

  public abstract double getLatestValueFast();

  public abstract long getEarliestTimeFast();

  public abstract double getEarliestValueFast();

  public abstract DoubleIterator valuesIteratorFast();

  public abstract DoubleList valuesFast();

  public abstract LongIterator timesIteratorFast();

  public abstract LongList timesFast();

  public abstract ObjectIterator<Long2DoubleMap.Entry> iteratorFast();

  public abstract FastLongDoubleTimeSeries subSeriesFast(long startTime, long endTime);
  
  public abstract FastLongDoubleTimeSeries subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd);

  public abstract FastLongDoubleTimeSeries headFast(int items);

  public abstract FastLongDoubleTimeSeries tailFast(int items);

  public abstract double[] valuesArrayFast();

  public abstract long[] timesArrayFast();

  public abstract FastLongDoubleTimeSeries newInstanceFast(long[] times, double[] values);
  
  public abstract FastLongDoubleTimeSeries operate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastLongDoubleTimeSeries operate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastLongDoubleTimeSeries operate(final double other, final BinaryOperator operator);
  
  public abstract FastLongDoubleTimeSeries unionOperate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastLongDoubleTimeSeries unionOperate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastLongDoubleTimeSeries operate(final UnaryOperator operator);
  
  public abstract FastLongDoubleTimeSeries lag(final int days);
}
