/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;

/**
 * @param <DATE_TYPE> the type of object used to hold Dates/DateTimes in the wrapper
 */
public abstract class AbstractMutableIntDoubleTimeSeries<DATE_TYPE> extends AbstractIntDoubleTimeSeries<DATE_TYPE> implements MutableDoubleTimeSeries<DATE_TYPE> {

  private final FastMutableIntDoubleTimeSeries _timeSeries;

  public AbstractMutableIntDoubleTimeSeries(final DateTimeConverter<DATE_TYPE> converter, final FastMutableIntDoubleTimeSeries timeSeries) {
    super(converter, null);
    _timeSeries = timeSeries;
  }

  @Override
  public FastMutableIntDoubleTimeSeries getFastSeries() {
    return _timeSeries;
  }

  @Override
  public void putDataPoint(final DATE_TYPE time, final Double value) {
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
