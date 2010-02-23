/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.fast;

import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * @author jim
 * 
 */
public interface FastTimeSeries<T> extends DoubleTimeSeries<T> {
  DateTimeNumericEncoding getEncoding();

  DateTimeResolution getDateTimeResolution();
}
