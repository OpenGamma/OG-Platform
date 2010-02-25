/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import com.opengamma.timeseries.fast.FastTimeSeries;

/**
 * @author jim
 * 
 */
public interface FastBackedDoubleTimeSeries<DATE_TYPE> extends DoubleTimeSeries<DATE_TYPE> {
  public DateTimeConverter<DATE_TYPE> getConverter();

  public FastTimeSeries<?> getFastSeries();
}
