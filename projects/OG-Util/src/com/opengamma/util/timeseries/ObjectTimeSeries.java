/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.integer.object.FastMutableIntObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;
import com.opengamma.util.timeseries.fast.longint.object.FastMutableLongObjectTimeSeries;




public interface ObjectTimeSeries<DATE_TYPE, T> extends TimeSeries<DATE_TYPE, T> {

  // CSOFF: Self-explainatory
  public ObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(ObjectTimeSeries<?, T> other);
  public ObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(FastBackedObjectTimeSeries<?, T> other);
  public ObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(FastIntObjectTimeSeries<T> other);
  public ObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(FastLongObjectTimeSeries<T> other);
  public ObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(ObjectTimeSeries<?, T> other);
  public ObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(FastBackedObjectTimeSeries<?, T> other);
  public ObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(FastIntObjectTimeSeries<T> other);
  public ObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(FastLongObjectTimeSeries<T> other);

  public ObjectTimeSeries<DATE_TYPE, T> subSeries(DATE_TYPE startTime, boolean inclusiveStart, DATE_TYPE endTime, boolean exclusiveEnd);
  public ObjectTimeSeries<DATE_TYPE, T> subSeries(DATE_TYPE startTime, DATE_TYPE endTime);
  public ObjectTimeSeries<DATE_TYPE, T> head(int numItems);
  public ObjectTimeSeries<DATE_TYPE, T> tail(int numItems);
 
  public ObjectTimeSeries<DATE_TYPE, T> lag(final int days);
  
  // conversions
  public abstract FastMutableIntObjectTimeSeries<T> toFastMutableIntObjectTimeSeries();
  
  public abstract FastIntObjectTimeSeries<T> toFastIntObjectTimeSeries();
  
  public abstract FastMutableLongObjectTimeSeries<T> toFastMutableLongObjectTimeSeries();
  
  public abstract FastLongObjectTimeSeries<T> toFastLongObjectTimeSeries();

  public abstract FastMutableIntObjectTimeSeries<T> toFastMutableIntObjectTimeSeries(DateTimeNumericEncoding encoding);
  
  public abstract FastIntObjectTimeSeries<T> toFastIntObjectTimeSeries(DateTimeNumericEncoding encoding);
  
  public abstract FastMutableLongObjectTimeSeries<T> toFastMutableLongObjectTimeSeries(DateTimeNumericEncoding encoding);
  
  public abstract FastLongObjectTimeSeries<T> toFastLongObjectTimeSeries(DateTimeNumericEncoding encoding);
  
//  public abstract MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries();
//  
//  public abstract MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(TimeZone timeZone);
//  
//  public abstract DateDoubleTimeSeries toDateDoubleTimeSeries();
//  
//  public abstract DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone timeZone);
//  
//  public abstract MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries();
//  
//  public abstract MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries(TimeZone timeZone);
//  
//  public abstract SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries();
//  
//  public abstract SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries(TimeZone timeZone);
//  
//  public abstract MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries();
//  
//  public abstract MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries(TimeZone timeZone);
//  
//  public abstract DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries();
//  
//  public abstract DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone timeZone);  
//  
//  public abstract ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries();
//  
//  public abstract ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone);
//  
//  public abstract MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries();
//  
//  public abstract MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone timeZone);
//
//  public abstract LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries();
//  
//  public abstract LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone timeZone);
//  
//  public abstract MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries();
//  
//  public abstract MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone timeZone);
//  
//  public abstract YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate);
//
//  public abstract YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate);
//
//  public abstract MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate);
//  
//  public abstract MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(java.util.TimeZone timeZone, Date zeroDate);

  // CSON: Self-explainatory
}
