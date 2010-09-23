/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint.object;

public interface FastMutableLongObjectTimeSeries<T> extends FastLongObjectTimeSeries<T> {
  void primitivePutDataPoint(long time, T value);

  void primitiveRemoveDataPoint(long time);

  void clear();
}
