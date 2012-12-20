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
 * A time-series, which represents the changes in a value over time.
 * <p>
 * This interface is similar to both a {@code SortedMap} of value keyed by date-time
 * and a {@code List} of date-time to value pairs.
 * As such, the date/times do not have to be evenly spread over time within the series.
 * 
 * @param <T> the date-time type, such as {@code Instant} or {@code LocalDate}
 * @param <V> the value being viewed over time, such as {@code Double}
 */
public interface ObjectTimeSeries<T, V> extends TimeSeries<T, V> {

  @Override
  ObjectTimeSeries<T, V> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);

  @Override
  ObjectTimeSeries<T, V> subSeries(T startTimeInclusive, T endTimeExclusive);

  @Override
  ObjectTimeSeries<T, V> head(int numItems);

  @Override
  ObjectTimeSeries<T, V> tail(int numItems);

  @Override
  ObjectTimeSeries<T, V> lag(final int lagCount);

  @Override
  ObjectTimeSeries<T, V> newInstance(T[] dateTimes, V[] values);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from this series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  ObjectTimeSeries<T, V> intersectionFirstValue(ObjectTimeSeries<?, V> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from this series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  ObjectTimeSeries<T, V> intersectionFirstValue(FastBackedObjectTimeSeries<?, V> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from this series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  ObjectTimeSeries<T, V> intersectionFirstValue(FastIntObjectTimeSeries<V> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from this series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  ObjectTimeSeries<T, V> intersectionFirstValue(FastLongObjectTimeSeries<V> other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  ObjectTimeSeries<T, V> intersectionSecondValue(ObjectTimeSeries<?, V> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  ObjectTimeSeries<T, V> intersectionSecondValue(FastBackedObjectTimeSeries<?, V> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  ObjectTimeSeries<T, V> intersectionSecondValue(FastIntObjectTimeSeries<V> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  ObjectTimeSeries<T, V> intersectionSecondValue(FastLongObjectTimeSeries<V> other);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code FastIntObjectTimeSeries}.
   * 
   * @return the time-series, not null
   */
  FastIntObjectTimeSeries<V> toFastIntObjectTimeSeries();

  /**
   * Converts this time-series to a {@code FastMutableIntObjectTimeSeries}.
   * 
   * @return the time-series, not null
   */
  FastMutableIntObjectTimeSeries<V> toFastMutableIntObjectTimeSeries();

  /**
   * Converts this time-series to a {@code FastLongObjectTimeSeries}.
   * 
   * @return the time-series, not null
   */
  FastLongObjectTimeSeries<V> toFastLongObjectTimeSeries();

  /**
   * Converts this time-series to a {@code FastMutableLongObjectTimeSeries}.
   * 
   * @return the time-series, not null
   */
  FastMutableLongObjectTimeSeries<V> toFastMutableLongObjectTimeSeries();

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code FastIntObjectTimeSeries} using
   * a specific date-time encoding.
   * 
   * @param encoding  the date-time encoding, not null
   * @return the time-series, not null
   */
  FastIntObjectTimeSeries<V> toFastIntObjectTimeSeries(DateTimeNumericEncoding encoding);

  /**
   * Converts this time-series to a {@code FastMutableIntObjectTimeSeries} using
   * a specific date-time encoding.
   * 
   * @param encoding  the date-time encoding, not null
   * @return the time-series, not null
   */
  FastMutableIntObjectTimeSeries<V> toFastMutableIntObjectTimeSeries(DateTimeNumericEncoding encoding);

  /**
   * Converts this time-series to a {@code FastLongObjectTimeSeries} using
   * a specific date-time encoding.
   * 
   * @param encoding  the date-time encoding, not null
   * @return the time-series, not null
   */
  FastLongObjectTimeSeries<V> toFastLongObjectTimeSeries(DateTimeNumericEncoding encoding);

  /**
   * Converts this time-series to a {@code FastMutableLongObjectTimeSeries} using
   * a specific date-time encoding.
   * 
   * @param encoding  the date-time encoding, not null
   * @return the time-series, not null
   */
  FastMutableLongObjectTimeSeries<V> toFastMutableLongObjectTimeSeries(DateTimeNumericEncoding encoding);

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
}
