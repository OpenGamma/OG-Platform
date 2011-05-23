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


/**
 * 
 * @param <DATE_TYPE> The type of the dates
 * @param <T> The type of the data
 */
public interface ObjectTimeSeries<DATE_TYPE, T> extends TimeSeries<DATE_TYPE, T> {

  // Self-explanatory
  ObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(ObjectTimeSeries<?, T> other);
  ObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(FastBackedObjectTimeSeries<?, T> other);
  ObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(FastIntObjectTimeSeries<T> other);
  ObjectTimeSeries<DATE_TYPE, T> intersectionFirstValue(FastLongObjectTimeSeries<T> other);
  ObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(ObjectTimeSeries<?, T> other);
  ObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(FastBackedObjectTimeSeries<?, T> other);
  ObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(FastIntObjectTimeSeries<T> other);
  ObjectTimeSeries<DATE_TYPE, T> intersectionSecondValue(FastLongObjectTimeSeries<T> other);

  ObjectTimeSeries<DATE_TYPE, T> subSeries(DATE_TYPE startTime, boolean inclusiveStart, DATE_TYPE endTime, boolean exclusiveEnd);
  ObjectTimeSeries<DATE_TYPE, T> subSeries(DATE_TYPE startTime, DATE_TYPE endTime);
  ObjectTimeSeries<DATE_TYPE, T> head(int numItems);
  ObjectTimeSeries<DATE_TYPE, T> tail(int numItems);
 
  ObjectTimeSeries<DATE_TYPE, T> lag(final int days);
  
  // conversions
  FastMutableIntObjectTimeSeries<T> toFastMutableIntObjectTimeSeries();
  
  FastIntObjectTimeSeries<T> toFastIntObjectTimeSeries();
  
  FastMutableLongObjectTimeSeries<T> toFastMutableLongObjectTimeSeries();
  
  FastLongObjectTimeSeries<T> toFastLongObjectTimeSeries();

  FastMutableIntObjectTimeSeries<T> toFastMutableIntObjectTimeSeries(DateTimeNumericEncoding encoding);
  
  FastIntObjectTimeSeries<T> toFastIntObjectTimeSeries(DateTimeNumericEncoding encoding);
  
  FastMutableLongObjectTimeSeries<T> toFastMutableLongObjectTimeSeries(DateTimeNumericEncoding encoding);
  
  FastLongObjectTimeSeries<T> toFastLongObjectTimeSeries(DateTimeNumericEncoding encoding);
  
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
