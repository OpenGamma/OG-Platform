/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer.object;

/**
 * 
 * @param <T> The type of the data
 */
public interface FastMutableIntObjectTimeSeries<T> extends FastIntObjectTimeSeries<T> {
  void primitivePutDataPoint(int time, T value);

  void primitiveRemoveDataPoint(int time);

  void clear();
}
