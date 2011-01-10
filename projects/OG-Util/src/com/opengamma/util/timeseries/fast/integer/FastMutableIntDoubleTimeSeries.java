/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer;

public interface FastMutableIntDoubleTimeSeries extends FastIntDoubleTimeSeries {
  public void primitivePutDataPoint(int time, double value);

  public void primitiveRemoveDataPoint(int time);

  public void clear();
}
