/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;


public interface MutableTimeSeries<DATE_TYPE, VALUE_TYPE> extends TimeSeries<DATE_TYPE, VALUE_TYPE> {
  void putDataPoint(DATE_TYPE time, VALUE_TYPE value);

  void removeDataPoint(DATE_TYPE time);

  void clear();
}
