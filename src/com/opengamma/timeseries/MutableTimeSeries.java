package com.opengamma.timeseries;


public interface MutableTimeSeries<DATE_TYPE, VALUE_TYPE> extends TimeSeries<DATE_TYPE, VALUE_TYPE> {
  public void putDataPoint(DATE_TYPE time, VALUE_TYPE value);

  public void removeDataPoint(DATE_TYPE time);

  public void clear();
}
