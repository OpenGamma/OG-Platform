/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint.object;

public interface FastMutableLongObjectTimeSeries<T> extends FastLongObjectTimeSeries<T> {
  public void primitivePutDataPoint(long time, T value);

  public void primitiveRemoveDataPoint(long time);

  public void clear();
}
