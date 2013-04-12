/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise;

import java.util.NoSuchElementException;

import com.opengamma.timeseries.TimeSeries;

/**
 * A time series that stores data values against instants.
 * <p>
 * The "time" key to the time-series is an instant.
 * The instant class is defined by the implementation, allowing flexibility of instant library.
 * <p>
 * This interface also allows the "time" to be viewed and manipulate as a {@code long}.
 * An implementation may store either the instant object or a {@code long}.
 * <p>
 * The {@code long} must use this encoding:
 * Any far future or maximum instant must be converted to {@code Long.MAX_VALUE}.
 * Any far past or minimum instant must be converted to {@code Long.MIN_VALUE}.
 * Other values are encoded as the number of nanoseconds from 1970-01-01, with
 * a range of +-292 years.
 * 
 * @param <T>  the instant type
 * @param <V>  the value being viewed over time
 */
public interface PreciseTimeSeries<T, V>
    extends TimeSeries<T, V> {

  /**
   * Checks if the series contains a value at the {@code long} instant specified.
   * <p>
   * This method provides {@code Map} style {@code containsKey()} behavior.
   * 
   * @param instant  the nanosecond instant to retrieve, not null
   * @return true if the series contains the specified instant, false if not
   */
  boolean containsTime(long instant);

  /**
   * Gets the value associated with the instant, specifying the primitive {@code long} instant.
   * 
   * @param instant  the nanosecond instant
   * @return the matching value, null if there is no value for the instant
   */
  V getValue(long instant);

  //-------------------------------------------------------------------------
  /**
   * Gets the value at the specified index.
   * 
   * @param index  the index to retrieve
   * @return the instant at the index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  long getTimeAtIndexFast(int index);

  //-------------------------------------------------------------------------
  /**
   * Gets the earliest instant for which there is a data point.
   * 
   * @return the earliest nanosecond instant
   * @throws NoSuchElementException if empty
   */
  long getEarliestTimeFast();

  /**
   * Gets the latest instant for which there is a data point.
   * 
   * @return the latest nanosecond instant
   * @throws NoSuchElementException if empty
   */
  long getLatestTimeFast();

  //-------------------------------------------------------------------------
  /**
   * Gets an iterator over the instant-value pairs.
   * <p>
   * Although the pairs are expressed as instances of {@code Map.Entry},
   * it is recommended to use the primitive methods on {@code PreciseObjectEntryIterator}.
   * 
   * @return the iterator, not null
   */
  PreciseEntryIterator<T, V> iterator();

  //-------------------------------------------------------------------------
  /**
   * Gets an array of all {@code long} nanosecond times in this series.
   * <p>
   * The index of each entry will match that used by the index lookup methods.
   * As such, the values will be in instant order.
   * 
   * @return an array of all the nanosecond instants in order from earliest to latest, not null
   */
  long[] timesArrayFast();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseTimeSeries<T, V> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);

  @Override  // override for covariant return type
  PreciseTimeSeries<T, V> subSeries(T startTime, T endTime);

  @Override  // override for covariant return type
  PreciseTimeSeries<T, V> head(int numItems);

  @Override  // override for covariant return type
  PreciseTimeSeries<T, V> tail(int numItems);

  @Override  // override for covariant return type
  PreciseTimeSeries<T, V> lag(int lagCount);

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
