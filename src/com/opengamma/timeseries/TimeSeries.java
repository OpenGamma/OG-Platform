package com.opengamma.timeseries;

import java.util.Iterator;
import java.util.Map;

import javax.time.Duration;
import javax.time.InstantProvider;

public interface TimeSeries<T> extends Iterable<Map.Entry<InstantProvider, T>> {
  /**
   * Get the size of the time series, the number of data points.  This is long so that we don't have to recode if we need >2GB timeseries.
   * @return the size
   */
  public int size();
  /**
   * @return true if the time series has no elements
   */
  public boolean isEmpty();
  /**
   * Gets the data point on the moment in time provided.  If no entry in present in the time series, a NoSuchElementException will be thrown.
   * @param the instant of the requested sample
   * @return the requested sample
   */
  public T getDataPoint(InstantProvider instant);
  /**
   * Gets the data point at the (zero-based) index provided.  If no entry is present, an IndexOutOfBoundsException is thrown.
   * It should be noted that in some implementations, this call will not be O(1), so it's use should be avoided inside loops.
   * @param index of data point required
   * @return the requested sample
   */
  public T getDataPoint(int index);
  /**
   * Gets the latest time for which there is a data point (most positive).
   * @return the requested time
   */
  public InstantProvider getLatestInstant();
  /**
   * Gets the value associated with the latest data point in the series.
   * @throws NoSuchElementException if the series is empty.
   * @return the requested value
   */
  public T getLatestValue();
  /**
   * Gets the earliest time for which there is a data point (least positive).
   * @throws NoSuchElementException if the series is empty.
   * @return the requested time
   */
  public InstantProvider getEarliestInstant();
  /**
   * Gets the value associated with the earliest data point in the series.
   * @throws NoSuchELementException if the series is empty.
   * @return the requested value
   */
  public T getEarliestValue();
  /**
   * Get an iterator that will allow iteration over the values in the series, from earliest to latest.
   * @throws NoSuchElementException if the series is empty.
   * @return the iterator
   */
  public Iterator<T> valuesIterator();
  /**
   * Get an iterator that will allow iteration over the times in the series, from the earliest to the latest.
   * @return the iterator
   */
  public Iterator<InstantProvider> timeIterator();
  /**
   * The standard iterator that returns pairs of times and values as instances of KeyValuePair/Map.Entry
   * @return the iterator
   */
  public Iterator<Map.Entry<InstantProvider, T>> iterator();
  /**
   * Return the subset of the current TimeSeries from the start to the end time.  If the start or end time are not present
   * in the series then a NoSuchElementException will be thrown.
   * @throws NoSuchElementException
   * @param start time
   * @param end time
   * @return subset of TimeSeries
   */
  public TimeSeries<T> subSeries(InstantProvider startTime, InstantProvider endTime);
  /**
   * Return the subset of the current TimeSeries from the start time for the duration.  If the start or end time are not present
   * in the series then a NoSuchElementException will be thrown.
   * @throws NoSuchElementException
   * @param start time
   * @param duration
   * @return subset of TimeSeries.
   */
  public TimeSeries<T> subSeries(InstantProvider startTime, Duration duration);
  /**
   * return the first numItems from the time series as a new time series.  These represent the earliest items in the series, by time.
   * @param number of items from the beginning, must be positive.
   * @throws IndexOutOfBoundsException if the number of items requested is greater than the length of the time series.
   * @return TimeSeries of first numItems items.
   */
  public TimeSeries<T> head(int numItems);
  /**
   * return the last numItems from the time series as a new time series.  These represent the lateest items in the series, by time.
   * @param number of items from the end, must be positive.
   * @throws IndexOutOfBoundsException if the number of items requested is greater than the length of the time series.
   * @return TimeSeries of first numItems items.
   */
  public TimeSeries<T> tail(int numItems);
}
