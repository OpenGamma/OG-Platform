/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * @param <DATE_TYPE> the type of object used to hold Dates/DateTimes in the wrapper
 */
public abstract class AbstractMutableLongDoubleTimeSeries<DATE_TYPE> extends AbstractLongDoubleTimeSeries<DATE_TYPE> implements MutableDoubleTimeSeries<DATE_TYPE> {

  private final FastMutableLongDoubleTimeSeries _timeSeries;

  public AbstractMutableLongDoubleTimeSeries(final DateTimeConverter<DATE_TYPE> converter, final FastMutableLongDoubleTimeSeries timeSeries) {
    super(converter, null);
    _timeSeries = timeSeries;
  }

  @Override
  public FastMutableLongDoubleTimeSeries getFastSeries() {
    return _timeSeries;
  }

  @Override
  public void putDataPoint(final DATE_TYPE time, final Double value) {
    getFastSeries().primitivePutDataPoint(getConverter().convertToLong(time), value);
  }

  @Override
  public void removeDataPoint(final DATE_TYPE time) {
    getFastSeries().primitiveRemoveDataPoint(getConverter().convertToLong(time));
  }

  @Override
  public void clear() {
    getFastSeries().clear();
  }
}
