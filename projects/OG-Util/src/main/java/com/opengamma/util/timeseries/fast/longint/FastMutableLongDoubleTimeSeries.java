/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint;

/**
 * 
 */
public interface FastMutableLongDoubleTimeSeries extends FastLongDoubleTimeSeries {
  void primitivePutDataPoint(long time, double value);

  void primitiveRemoveDataPoint(long time);

  void clear();
}
