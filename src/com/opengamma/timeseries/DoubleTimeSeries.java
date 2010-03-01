package com.opengamma.timeseries;

import java.util.TimeZone;

import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.MutableDateDoubleTimeSeries;
import com.opengamma.timeseries.date.time.DateTimeDoubleTimeSeries;
import com.opengamma.timeseries.date.time.MutableDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;


public interface DoubleTimeSeries<DATE_TYPE> extends TimeSeries<DATE_TYPE, Double> {
  
  public abstract FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries();
  
  public abstract FastIntDoubleTimeSeries toFastIntDoubleTimeSeries();
  
  public abstract FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries();
  
  public abstract FastLongDoubleTimeSeries toFastLongDoubleTimeSeries();

  public abstract FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding encoding);
  
  public abstract FastIntDoubleTimeSeries toFastIntDoubleTimeSeries(DateTimeNumericEncoding encoding);
  
  public abstract FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding encoding);
  
  public abstract FastLongDoubleTimeSeries toFastLongDoubleTimeSeries(DateTimeNumericEncoding encoding);
  
  public abstract MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries();
  
  public abstract MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(TimeZone timeZone);
  
  public abstract DateDoubleTimeSeries toDateDoubleTimeSeries();
  
  public abstract DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone);
  
  public abstract MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries();
  
  public abstract MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries(TimeZone timeZone);
  
  public abstract DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries();
  
  public abstract DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone);  
}
