/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;

/**
 * @author jim
 * 
 */
public abstract class AbstractMutableLongObjectTimeSeries<DATE_TYPE, T> extends AbstractLongObjectTimeSeries<DATE_TYPE, T> implements MutableObjectTimeSeries<DATE_TYPE, T> {

  private final FastMutableLongObjectTimeSeries<T> _timeSeries;

  public AbstractMutableLongObjectTimeSeries(final DateTimeConverter<DATE_TYPE> converter, final FastMutableLongObjectTimeSeries<T> timeSeries) {
    super(converter, null);
    _timeSeries = timeSeries;
  }

  @Override
  public FastMutableLongObjectTimeSeries<T> getFastSeries() {
    return _timeSeries;
  }

  @Override
  public void putDataPoint(final DATE_TYPE time, final T value) {
    getFastSeries().primitivePutDataPoint(_converter.convertToLong(time), value);
  }

  @Override
  public void removeDataPoint(final DATE_TYPE time) {
    getFastSeries().primitiveRemoveDataPoint(_converter.convertToLong(time));
  }

  @Override
  public void clear() {
    getFastSeries().clear();
  }
}
