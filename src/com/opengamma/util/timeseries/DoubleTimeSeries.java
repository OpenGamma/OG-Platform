package com.opengamma.util.timeseries;

import java.util.Date;
import java.util.TimeZone;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.MutableDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.MutableDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.MutableYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.YearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.MutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;


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
  
  public abstract ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries();
  
  public abstract ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone);
  
  public abstract MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries();
  
  public abstract MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone);
  
  public abstract YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate);

  public abstract YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate);

  public abstract MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate);
  
  public abstract MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate);
}
