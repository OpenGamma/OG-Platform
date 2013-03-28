/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.longint;

import com.opengamma.timeseries.fast.DateTimeNumericEncoding;

/**
 * Contains methods to make Primitive time series work with the normal
 * non-primitive time series interface (where possible)
 */
public abstract class AbstractFastMutableLongDoubleTimeSeries
    extends AbstractFastLongDoubleTimeSeries
    implements FastMutableLongDoubleTimeSeries {

  /** Serialization version. */
  private static final long serialVersionUID = 7826820209544890667L;

  protected AbstractFastMutableLongDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
  }

  //-------------------------------------------------------------------------
  public void putDataPoint(final Long time, final Double value) {
    primitivePutDataPoint(time, value);
  }

  public void removeDataPoint(final Long time) {
    primitiveRemoveDataPoint(time);
  }

}
