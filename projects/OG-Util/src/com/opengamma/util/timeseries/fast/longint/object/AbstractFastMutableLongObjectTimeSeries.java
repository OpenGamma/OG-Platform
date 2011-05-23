/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint.object;

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;

/**
 *         Contains methods to make Primitive time series work with the normal
 *         non-primitive time series interface (where possible)
 *         @param <T> The type of the data
 */
public abstract class AbstractFastMutableLongObjectTimeSeries<T> extends AbstractFastLongObjectTimeSeries<T> implements
    FastMutableLongObjectTimeSeries<T> {

  protected AbstractFastMutableLongObjectTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
  }

  public void putDataPoint(final Long time, final T value) {
    primitivePutDataPoint(time, value);
  }

  public void removeDataPoint(final Long time) {
    primitiveRemoveDataPoint(time);
  }

}
