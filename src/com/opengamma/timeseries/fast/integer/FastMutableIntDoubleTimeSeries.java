package com.opengamma.timeseries.fast.integer;


public interface FastMutableIntDoubleTimeSeries extends FastIntDoubleTimeSeries {
  public void primitivePutDataPoint(int time, double value);

  public void primitiveRemoveDataPoint(int time);

  public void clear();
}
