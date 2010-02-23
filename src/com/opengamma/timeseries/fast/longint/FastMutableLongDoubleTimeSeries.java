package com.opengamma.timeseries.fast.longint;


public interface FastMutableLongDoubleTimeSeries extends FastLongDoubleTimeSeries {
  public void primitivePutDataPoint(long time, double value);

  public void primitiveRemoveDataPoint(long time);

  public void clear();
}
