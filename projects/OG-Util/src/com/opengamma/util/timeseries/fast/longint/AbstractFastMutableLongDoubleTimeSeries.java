/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint;

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;


/**
 * @author jim
 *         Contains methods to make Primitive time series work with the normal
 *         non-primitive time series interface (where possible)
 */
public abstract class AbstractFastMutableLongDoubleTimeSeries extends AbstractFastLongDoubleTimeSeries implements FastMutableLongDoubleTimeSeries {

  protected AbstractFastMutableLongDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
  }

  public void putDataPoint(final Long time, final Double value) {
    primitivePutDataPoint(time, value);
  }

  public void removeDataPoint(final Long time) {
    primitiveRemoveDataPoint(time);
  }

}
