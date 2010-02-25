/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperations.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperations.UnaryOperator;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public interface FastTimeSeries<T> extends DoubleTimeSeries<T> {
  DateTimeNumericEncoding getEncoding();

  DateTimeResolution getDateTimeResolution();
  
  public abstract FastTimeSeries<T> operate(final UnaryOperator operator);

  public abstract FastTimeSeries<T> operate(final double other, final BinaryOperator operator);
  
  public abstract FastTimeSeries<T> operate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastTimeSeries<T> operate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);

  public abstract FastTimeSeries<T> unionOperate(final FastLongDoubleTimeSeries other, final BinaryOperator operator);
  
  public abstract FastTimeSeries<T> unionOperate(final FastIntDoubleTimeSeries other, final BinaryOperator operator);  
}
