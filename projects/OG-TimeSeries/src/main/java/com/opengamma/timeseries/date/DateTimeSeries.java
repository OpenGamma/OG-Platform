/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import java.util.NoSuchElementException;

import com.opengamma.timeseries.TimeSeries;

/**
 * A time series that stores data values against dates.
 * <p>
 * The "time" key to the time-series is a date.
 * The date class is defined by the implementation, allowing flexibility of date library.
 * <p>
 * This interface also allows the "time" to be viewed and manipulate as an {@code int}.
 * An implementation may store either the date object or an {@code int}.
 * <p>
 * The {@code int} must use this encoding:
 * Any far future or maximum date must be converted to {@code Integer.MAX_VALUE}.
 * Any far past or minimum date must be converted to {@code Integer.MIN_VALUE}.
 * Other values are encoded by multiplying the year by 10,000 and the month by 100.
 * Thus the date 2012-06-30 will be converted to the number 20,120,630.
 * Any date with a year outside the range 0000 to 9999 throws an exception.
 * 
 * @param <T>  the date type
 * @param <V>  the value being viewed over time
 */
public interface DateTimeSeries<T, V>
    extends TimeSeries<T, V> {

  /**
   * Checks if the series contains a value at the {@code int} date specified.
   * <p>
   * This method provides {@code Map} style {@code containsKey()} behavior.
   * 
   * @param date  the date to retrieve, not null
   * @return true if the series contains the specified date, false if not
   */
  boolean containsTime(int date);

  /**
   * Gets the value associated with the date, specifying the primitive {@code int} date.
   * 
   * @param date  the {@code int} date
   * @return the matching value, null if there is no value for the date
   */
  V getValue(int date);

  //-------------------------------------------------------------------------
  /**
   * Gets the value at the specified index.
   * 
   * @param index  the index to retrieve
   * @return the date at the index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  int getTimeAtIndexFast(int index);

  //-------------------------------------------------------------------------
  /**
   * Gets the earliest date for which there is a data point.
   * 
   * @return the earliest date
   * @throws NoSuchElementException if empty
   */
  int getEarliestTimeFast();

  /**
   * Gets the latest date for which there is a data point.
   * 
   * @return the latest date
   * @throws NoSuchElementException if empty
   */
  int getLatestTimeFast();

  //-------------------------------------------------------------------------
  /**
   * Gets an iterator over the date-value pairs.
   * <p>
   * Although the pairs are expressed as instances of {@code Map.Entry},
   * it is recommended to use the primitive methods on {@code DateObjectEntryIterator}.
   * 
   * @return the iterator, not null
   */
  DateEntryIterator<T, V> iterator();

  //-------------------------------------------------------------------------
  /**
   * Gets an array of all {@code int} times in this series.
   * <p>
   * The index of each entry will match that used by the index lookup methods.
   * As such, the values will be in date order.
   * 
   * @return an array of all the values in order from earliest to latest, not null
   */
  int[] timesArrayFast();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  DateTimeSeries<T, V> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);

  @Override  // override for covariant return type
  DateTimeSeries<T, V> subSeries(T startTime, T endTime);

  @Override  // override for covariant return type
  DateTimeSeries<T, V> head(int numItems);

  @Override  // override for covariant return type
  DateTimeSeries<T, V> tail(int numItems);

  @Override  // override for covariant return type
  DateTimeSeries<T, V> lag(int lagCount);

  //-------------------------------------------------------------------------
  /**
   * Checks if this time-series equals the specified time-series.
   * 
   * @param obj  the other time-series, null returns false
   * @return true if equal
   */
  @Override
  boolean equals(Object obj);

  /**
   * A suitable hash code.
   * 
   * @return the hash code
   */
  @Override
  int hashCode();

}
