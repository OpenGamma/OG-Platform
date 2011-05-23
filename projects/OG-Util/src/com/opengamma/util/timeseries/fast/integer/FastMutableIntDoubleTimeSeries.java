/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer;

/**
 * 
 */
public interface FastMutableIntDoubleTimeSeries extends FastIntDoubleTimeSeries {
  void primitivePutDataPoint(int time, double value);

  void primitiveRemoveDataPoint(int time);

  void clear();
}
