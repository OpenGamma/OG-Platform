package com.opengamma.timeseries.fast.longint;

import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import com.opengamma.timeseries.DoubleTimeSeriesOperations.BinaryOperator;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;

public interface FastLongDoubleTimeSeries extends FastTimeSeries<Long> {
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

  public abstract double getDataPointFast(long time);

  public abstract ObjectIterator<Long2DoubleMap.Entry> iteratorFast();

  public abstract FastLongDoubleTimeSeries subSeriesFast(long startTime, long endTime);

  public abstract FastLongDoubleTimeSeries headFast(int items);

  public abstract FastLongDoubleTimeSeries tailFast(int items);

  public abstract double[] valuesArrayFast();

  public abstract long[] timesArrayFast();

  public abstract FastLongDoubleTimeSeries newInstanceFast(long[] times, double[] values);
  
  public abstract FastLongDoubleTimeSeries operate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastLongDoubleTimeSeries operate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);
}
