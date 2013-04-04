/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.integer;

import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * 
 */
public interface FastIntDoubleTimeSeries extends FastTimeSeries<Integer> {
  int size();

  boolean isEmpty();

  double getValueFast(int time);

  double getValueAtIndexFast(int index);

  int getTimeAtIndexFast(int index);

  int getLatestTimeFast();

  double getLatestValueFast();

  int getEarliestTimeFast();

  double getEarliestValueFast();

  DoubleIterator valuesIteratorFast();

  DoubleList valuesFast();

  IntIterator timesIteratorFast();

  ObjectIterator<Int2DoubleMap.Entry> iteratorFast();

  IntList timesFast();

  FastIntDoubleTimeSeries subSeriesFast(int startTime, int endTime);

  FastIntDoubleTimeSeries subSeriesFast(int startTime, boolean includeStart, int endTime, boolean includeEnd);

  FastIntDoubleTimeSeries headFast(int items);

  FastIntDoubleTimeSeries tailFast(int items);

  int[] timesArrayFast();

  FastIntDoubleTimeSeries newInstanceFast(int[] times, double[] values);

  FastIntDoubleTimeSeries operate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);

  FastIntDoubleTimeSeries operate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);

  FastIntDoubleTimeSeries operate(final double other, final BinaryOperator operator);

  FastIntDoubleTimeSeries operate(final UnaryOperator operator);

  FastIntDoubleTimeSeries unionOperate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);

  FastIntDoubleTimeSeries unionOperate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);

  FastIntDoubleTimeSeries lag(final int days);

}
