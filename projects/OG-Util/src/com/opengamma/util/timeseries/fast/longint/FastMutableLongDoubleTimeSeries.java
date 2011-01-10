/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint;

public interface FastMutableLongDoubleTimeSeries extends FastLongDoubleTimeSeries {
  public void primitivePutDataPoint(long time, double value);

  public void primitiveRemoveDataPoint(long time);

  public void clear();
}
