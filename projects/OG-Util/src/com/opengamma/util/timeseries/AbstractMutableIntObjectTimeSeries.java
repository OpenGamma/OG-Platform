/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;

/**
 * @param <DATE_TYPE> the type of object used to hold Dates/DateTimes in the wrapper
 * @param <T> the type of the objects being stored in the time series
 */
public abstract class AbstractMutableIntObjectTimeSeries<DATE_TYPE, T> extends AbstractIntObjectTimeSeries<DATE_TYPE, T> implements MutableObjectTimeSeries<DATE_TYPE, T> {

  private final FastMutableIntObjectTimeSeries<T> _timeSeries;

  public AbstractMutableIntObjectTimeSeries(final DateTimeConverter<DATE_TYPE> converter, final FastMutableIntObjectTimeSeries<T> timeSeries) {
    super(converter, null);
    _timeSeries = timeSeries;
  }

  @Override
  public FastMutableIntObjectTimeSeries<T> getFastSeries() {
    return _timeSeries;
  }

  @Override
  public void putDataPoint(final DATE_TYPE time, final T value) {
    getFastSeries().primitivePutDataPoint(getConverter().convertToInt(time), value);
  }

  @Override
  public void removeDataPoint(final DATE_TYPE time) {
    getFastSeries().primitiveRemoveDataPoint(getConverter().convertToInt(time));
  }

  @Override
  public void clear() {
    getFastSeries().clear();
  }
}
