/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer;

import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

public interface FastIntDoubleTimeSeries extends FastTimeSeries<Integer> {
  public abstract int size();

  public abstract boolean isEmpty();

  public abstract double getValueFast(int time);

  public abstract double getValueAtFast(int index);

  public abstract int getTimeFast(int index);

  public abstract int getLatestTimeFast();

  public abstract double getLatestValueFast();

  public abstract int getEarliestTimeFast();

  public abstract double getEarliestValueFast();

  public abstract DoubleIterator valuesIteratorFast();

  public abstract DoubleList valuesFast();

  public abstract IntIterator timesIteratorFast();

  public abstract ObjectIterator<Int2DoubleMap.Entry> iteratorFast();

  public abstract IntList timesFast();

  public abstract FastIntDoubleTimeSeries subSeriesFast(int startTime, int endTime);
  
  public abstract FastIntDoubleTimeSeries subSeriesFast(int startTime, boolean includeStart, int endTime, boolean includeEnd);

  public abstract FastIntDoubleTimeSeries headFast(int items);

  public abstract FastIntDoubleTimeSeries tailFast(int items);

  public abstract double[] valuesArrayFast();

  public abstract int[] timesArrayFast();

  public abstract FastIntDoubleTimeSeries newInstanceFast(int[] times, double[] values);

  public abstract FastIntDoubleTimeSeries operate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastIntDoubleTimeSeries operate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastIntDoubleTimeSeries operate(final double other, final BinaryOperator operator);
  
  public abstract FastIntDoubleTimeSeries operate(final UnaryOperator operator);
  
  public abstract FastIntDoubleTimeSeries unionOperate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastIntDoubleTimeSeries unionOperate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastIntDoubleTimeSeries lag(final int days);

}
