/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public interface TimeSeries<T, V> extends Iterable<Map.Entry<T, V>> {
  // tailSeries/headSeries by time

  /**
   * Gets the size of the time-series.
   * <p>
   * This is the number of data points in the series.
   * 
   * @return the size, zero or greater
   */
  int size();

  /**
   * Checks if the series is empty.
   * <p>
   * This checks if there are no data points.
   * 
   * @return true if the time-series is empty
   */
  boolean isEmpty();

  //-------------------------------------------------------------------------
  /**
   * Checks if the series contains a value at the date-time specified.
   * <p>
   * This method provides {@code Map} style {@code containsKey()} behavior.
   * The date/time is matched exactly, thus care must be taken with precision in times.
   * 
   * @param dateTime  the date-time to retrieve, not null
   * @return true if the series contains the specified time, false if not
   */
  boolean containsTime(T dateTime);

  /**
   * Gets the value at the date-time specified.
   * <p>
   * This method provides {@code Map} style lookup of values.
   * The date/time is matched exactly, thus care must be taken with precision in times.
   * If there is no entry at the date-time, then null is returned.
   * 
   * @param dateTime  the date-time to retrieve, not null
   * @return the value at the date-time, null if date-time not present or
   *  if the implementation permits nulls
   */
  V getValue(T dateTime);

  /**
   * Gets the date-time at the index specified.
   * <p>
   * This method provides {@code List} style lookup of date-times.
   * It is not guaranteed that the lookup is O(1), thus it should be avoided in loops.
   * 
   * @param index  the zero-based index to retrieve, not null
   * @return the date-time at the index, null if the implementation permits nulls
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  T getTimeAtIndex(int index);

  /**
   * Gets the value at the index specified.
   * <p>
   * This method provides {@code List} style lookup of values.
   * It is not guaranteed that the lookup is O(1), thus it should be avoided in loops.
   * 
   * @param index  the zero-based index to retrieve, not null
   * @return the value at the index, null if the implementation permits nulls
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  V getValueAtIndex(int index);

  //-------------------------------------------------------------------------
  /**
   * Gets the earliest date-time for which there is a data point.
   * 
   * @return the earliest date-time, not null
   * @throws java.util.NoSuchElementException if empty
   */
  T getEarliestTime();

  /**
   * Gets the value at the earliest date-time in the series.
   * 
   * @return the value at the earliest date-time, not null
   * @throws java.util.NoSuchElementException if empty
   */
  V getEarliestValue();

  /**
   * Gets the latest date-time for which there is a data point.
   * 
   * @return the latest date-time, not null
   * @throws java.util.NoSuchElementException if empty
   */
  T getLatestTime();

  /**
   * Gets the value at the latest date-time in the series.
   * 
   * @return the value at the latest date-time, not null
   * @throws java.util.NoSuchElementException if empty
   */
  V getLatestValue();

  //-------------------------------------------------------------------------
  /**
   * Gets an iterator over the pairs of date-times and values.
   * <p>
   * The pairs are expressed as instances of {@code Map.Entry}.
   * The iterator is in date-time order.
   * 
   * @return the pair iterator, not null
   */
  Iterator<Map.Entry<T, V>> iterator();

  /**
   * Gets an iterator over the date-times in the time-series from earliest to latest.
   * <p>
   * The iterator is in date-time order.
   * 
   * @return the date-times iterator, not null
   */
  Iterator<T> timesIterator();

  /**
   * Gets an iterator over the values in the time-series from earliest to latest.
   * <p>
   * The iterator is in date-time order.
   * 
   * @return the values iterator, not null
   */
  Iterator<V> valuesIterator();

  //-------------------------------------------------------------------------
  /**
   * Gets part of this series as a sub-series between two date-times.
   * <p>
   * The date-times do not have to match exactly.
   * The sub-series contains all entries between the two date-times via
   * {@code Comparable}, with inclusive start and exclusive end.
   * 
   * @param startTimeInclusive  the start date-time, not null
   * @param endTimeExclusive  the end date-time, not null
   * @return the sub-series between the date-times, not null
   */
  TimeSeries<T, V> subSeries(T startTimeInclusive, T endTimeExclusive);

  /**
   * Gets part of this series as a sub-series between two date-times.
   * <p>
   * The date-times do not have to match exactly.
   * The sub-series contains all entries between the two date-times via
   * {@code Comparable}, as modified by the inclusive start/end flags.
   * 
   * @param startTime  the start date-time, not null
   * @param includeStart  true to include the start date-time in the result
   * @param endTime  the end date-time, not null
   * @param includeEnd  true to include the end date-time in the result
   * @return the sub-series between the date-times, not null
   */
  TimeSeries<T, V> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);

  /**
   * Gets part of this series as a sub-series, choosing the earliest entries.
   * <p>
   * The sub-series contains the specified number of entries taken from the
   * earliest date-time in this series.
   * 
   * @param numItems  the number of items to select, zero or greater
   * @return the sub-series of the requested size starting with the earliest entry, not null
   * @throws IndexOutOfBoundsException if the index is invalid, or the size of
   *  this series is less than the size requested
   */
  TimeSeries<T, V> head(int numItems);

  /**
   * Gets part of this series as a sub-series, choosing the latest entries.
   * <p>
   * The sub-series contains the specified number of entries taken from the
   * latest date-time in this series.
   * 
   * @param numItems  the number of items to select, zero or greater
   * @return the sub-series of the requested size ending with the latest entry, not null
   * @throws IndexOutOfBoundsException if the index is invalid, or the size of
   *  this series is less than the size requested
   */
  TimeSeries<T, V> tail(int numItems);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series where the values lag the date-times.
   * <p>
   * This returns a new time-series where each value occurs at a different date-time.
   * For example, the time series [(March,6),(April,7),(May,8),(June,9)] with a lag
   * of +2 would result in [(May,6),(June,7)]. Similarly, a lag of -1 would result
   * in [(March,7),(April,8),(May,9)].
   * Note that this operates on the entries, which are not necessarily continuous.
   * 
   * @param lagCount  the number of entries to lag by, positive or negative
   * @return the new time-series, not null
   */
  TimeSeries<T, V> lag(int lagCount);

  //-------------------------------------------------------------------------
  /**
   * Gets a list of all date-times in this series.
   * <p>
   * The index of each entry will match that used by the index lookup methods.
   * 
   * @return a list of all the date-times in order from earliest to latest, not null
   */
  List<T> times();

  /**
   * Gets an array of all date-times in this series.
   * <p>
   * The index of each entry will match that used by the index lookup methods.
   * 
   * @return an array of all the date-times in order from earliest to latest, not null
   */
  T[] timesArray();

  /**
   * Gets a list of all values in this series.
   * <p>
   * The index of each entry will match that used by the index lookup methods.
   * As such, the values will be in date-time order.
   * 
   * @return a list of all the values in order from earliest to latest, not null
   */
  List<V> values();

  /**
   * Gets an array of all values in this series.
   * <p>
   * The index of each entry will match that used by the index lookup methods.
   * As such, the values will be in date-time order.
   * 
   * @return an array of all the values in order from earliest to latest, not null
   */
  V[] valuesArray();

  //-------------------------------------------------------------------------
  /**
   * Creates a new instance with a new set of date-times and values.
   * 
   * @param dateTimes  the date-times, not null
   * @param values  the values, not null
   * @return the new time-series, not null
   * @throws RuntimeException if the array sizes differ or the instance cannot be created
   */
  TimeSeries<T, V> newInstance(T[] dateTimes, V[] values);

}
