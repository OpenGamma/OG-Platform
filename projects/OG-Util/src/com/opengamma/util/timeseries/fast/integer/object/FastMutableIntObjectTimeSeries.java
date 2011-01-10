/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer.object;

public interface FastMutableIntObjectTimeSeries<T> extends FastIntObjectTimeSeries<T> {
  public void primitivePutDataPoint(int time, T value);

  public void primitiveRemoveDataPoint(int time);

  public void clear();
}
