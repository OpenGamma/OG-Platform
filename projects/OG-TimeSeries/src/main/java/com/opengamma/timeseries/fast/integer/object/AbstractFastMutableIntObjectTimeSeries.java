/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.integer.object;

import com.opengamma.timeseries.fast.DateTimeNumericEncoding;

/**
 * Contains methods to make Primitive time series work with the normal
 * non-primitive time series interface (where possible)
 * @param <T>  the type
 */
public abstract class AbstractFastMutableIntObjectTimeSeries<T>
    extends AbstractFastIntObjectTimeSeries<T>
    implements FastMutableIntObjectTimeSeries<T> {

  /** Serialization version. */
  private static final long serialVersionUID = -2160158198123994057L;

  protected AbstractFastMutableIntObjectTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
  }

  //-------------------------------------------------------------------------
  public void putDataPoint(final Integer time, final T value) {
    primitivePutDataPoint(time, value);
  }

  public void removeDataPoint(final Integer time) {
    primitiveRemoveDataPoint(time);
  }

}
