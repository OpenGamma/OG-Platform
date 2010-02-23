/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * @author jim
 * 
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
