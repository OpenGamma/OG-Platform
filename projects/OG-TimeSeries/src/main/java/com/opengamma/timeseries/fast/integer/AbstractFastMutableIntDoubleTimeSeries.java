/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast.integer;

import com.opengamma.timeseries.fast.DateTimeNumericEncoding;

/**
 * Contains methods to make Primitive time series work with the normal
 * non-primitive time series interface (where possible)
 */
public abstract class AbstractFastMutableIntDoubleTimeSeries
    extends AbstractFastIntDoubleTimeSeries
    implements FastMutableIntDoubleTimeSeries {

  /** Serialization version. */
  private static final long serialVersionUID = -7788807755748622617L;

  protected AbstractFastMutableIntDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
  }

  //-------------------------------------------------------------------------
  public void putDataPoint(final Integer time, final Double value) {
    primitivePutDataPoint(time, value);
  }

  public void removeDataPoint(final Integer time) {
    primitiveRemoveDataPoint(time);
  }

}
