/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.util.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.FastObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * @param <DATE_TYPE, T>
 *
 * @author jim
 */
public interface FastBackedObjectTimeSeries<DATE_TYPE, T> extends ObjectTimeSeries<DATE_TYPE, T> {

  public abstract DateTimeConverter<DATE_TYPE> getConverter();

  public abstract FastObjectTimeSeries<?, T> getFastSeries();

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> operate(
      final FastObjectTimeSeries<?, T> other, final BinaryOperator<T> operator);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> operate(
      final FastBackedObjectTimeSeries<?, T> other, final BinaryOperator<T> operator);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> operate(
      final T other, final BinaryOperator<T> operator);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> operate(
      final UnaryOperator<T> operator);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionOperate(
      final FastObjectTimeSeries<?, T> other, final BinaryOperator<T> operator);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> unionOperate(
      final FastBackedObjectTimeSeries<?, T> other, final BinaryOperator<T> operator);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(
      FastBackedObjectTimeSeries<?, T> other);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(
      FastIntObjectTimeSeries<T> other);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(
      FastLongObjectTimeSeries<T> other);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(
      FastBackedObjectTimeSeries<?, T> other);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(
      FastIntObjectTimeSeries<T> other);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(
      FastLongObjectTimeSeries<T> other);

  public abstract FastBackedObjectTimeSeries<DATE_TYPE, T> lag(final int days);

  public abstract FastIntObjectTimeSeries<T> toFastIntObjectTimeSeries();

  public abstract FastIntObjectTimeSeries<T> toFastIntObjectTimeSeries(
      DateTimeNumericEncoding encoding);

  public abstract FastLongObjectTimeSeries<T> toFastLongObjectTimeSeries();

  public abstract FastLongObjectTimeSeries<T> toFastLongObjectTimeSeries(DateTimeNumericEncoding encoding);

  public abstract FastMutableIntObjectTimeSeries<T> toFastMutableIntObjectTimeSeries();

  public abstract FastMutableIntObjectTimeSeries<T> toFastMutableIntObjectTimeSeries(
      DateTimeNumericEncoding encoding);

  public abstract FastMutableLongObjectTimeSeries<T> toFastMutableLongObjectTimeSeries();

  public abstract FastMutableLongObjectTimeSeries<T> toFastMutableLongObjectTimeSeries(
      DateTimeNumericEncoding encoding);
  
}
