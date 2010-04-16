/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;


public interface MutableTimeSeries<DATE_TYPE, VALUE_TYPE> extends TimeSeries<DATE_TYPE, VALUE_TYPE> {
  public void putDataPoint(DATE_TYPE time, VALUE_TYPE value);

  public void removeDataPoint(DATE_TYPE time);

  public void clear();
}
