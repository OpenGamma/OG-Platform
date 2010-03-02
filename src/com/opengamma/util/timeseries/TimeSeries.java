package com.opengamma.util.timeseries;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public interface TimeSeries<DATE_TYPE, VALUE_TYPE> extends Iterable<Map.Entry<DATE_TYPE, VALUE_TYPE>> {
  /**
   * Get the size of the time series, the number of data points.
   * 
   * @return the size
   */
  public int size();

  /**
   * @return true if the time series has no elements
   */
  public boolean isEmpty();

  /**
   * Gets the data point on the (exact) moment in time provided. If no entry in
   * present
   * in the time series, a NoSuchElementException will be thrown.
   * 
   * @param the
   *          instant of the requested sample
   * @return the requested sample
   */
  public VALUE_TYPE getValue(DATE_TYPE instant);

  /**
   * Gets the data point at the (zero-based) index provided. If no entry is
   * present, an IndexOutOfBoundsException is thrown. It should be noted that in
   * some implementations, this call will not be O(1), so it's use should be
   * avoided inside loops.
   * 
   * @param index
   *          of data point required
   * @return the requested sample
   */
  public VALUE_TYPE getValueAt(int index);

  /**
   * Get the time at the (zero-based) index provided. If no entry is present
   * an IndexOutOfBoundsException is thrown. It should be noted that in some
   * implementations, this call will not be O(1), so it's use should be
   * avoided inside loops.
   */
  public DATE_TYPE getTime(int index);

  /**
   * Gets the latest time for which there is a data point (most positive).
   * 
   * @return the requested time
   */
  public DATE_TYPE getLatestTime();

  /**
   * Gets the value associated with the latest data point in the series.
   * 
   * @throws NoSuchElementException
   *           if the series is empty.
   * @return the requested value
   */
  public VALUE_TYPE getLatestValue();

  /**
   * Gets the earliest time for which there is a data point (least positive).
   * 
   * @throws NoSuchElementException
   *           if the series is empty.
   * @return the requested time
   */
  public DATE_TYPE getEarliestTime();

  /**
   * Gets the value associated with the earliest data point in the series.
   * 
   * @throws NoSuchELementException
   *           if the series is empty.
   * @return the requested value
   */
  public VALUE_TYPE getEarliestValue();

  /**
   * Get an iterator that will allow iteration over the values in the series,
   * from earliest to latest.
   * 
   * @throws NoSuchElementException
   *           if the series is empty.
   * @return the iterator
   */
  public Iterator<VALUE_TYPE> valuesIterator();

  /**
   * Get an iterator that will allow iteration over the times in the series,
   * from the earliest to the latest.
   * 
   * @return the iterator
   */
  public Iterator<DATE_TYPE> timeIterator();

  /**
   * The standard iterator that returns pairs of times and values as instances
   * of KeyValuePair/Map.Entry
   * 
   * @return the iterator
   */
  public Iterator<Map.Entry<DATE_TYPE, VALUE_TYPE>> iterator();

  /**
   * Return the subset of the current TimeSeries from the start to the end time.
   * If the start or end time are not present in the series then the nearest element is
   * found instead.
   * 
   * @param startTime
   * @param inclusiveStart whether or not the startTime is included in the result.
   * @param endTime
   * @param inclusiveEnd whether or not the endTime is included in the result.
   * @return subset of TimeSeries
   */
  public TimeSeries<DATE_TYPE, VALUE_TYPE> subSeries(DATE_TYPE startTime, boolean inclusiveStart, DATE_TYPE endTime, boolean exclusiveEnd);
  
  /**
   * Return the subset of the current TimeSeries from the start to the end time.
   * If the start or end time are not present in the series then the nearest element is
   * found instead.  This version follows the standard Collections pattern of being 
   * start INCLUSIVE and end EXCLUSIVE.
   * 
   * @param startTime
   * @param endTime
   * @return subset of TimeSeries
   */
  public TimeSeries<DATE_TYPE, VALUE_TYPE> subSeries(DATE_TYPE startTime, DATE_TYPE endTime);

  /**
   * return the first numItems from the time series as a new time series. These
   * represent the earliest items in the series, by time.
   * 
   * @param number
   *          of items from the beginning, must be positive.
   * @throws IndexOutOfBoundsException
   *           if the number of items requested is greater than the length of
   *           the time series.
   * @return TimeSeries of first numItems items.
   */
  public TimeSeries<DATE_TYPE, VALUE_TYPE> head(int numItems);

  /**
   * return the last numItems from the time series as a new time series. These
   * represent the latest items in the series, by time.
   * 
   * @param number
   *          of items from the end, must be positive.
   * @throws IndexOutOfBoundsException
   *           if the number of items requested is greater than the length of
   *           the time series.
   * @return TimeSeries of first numItems items.
   */
  public TimeSeries<DATE_TYPE, VALUE_TYPE> tail(int numItems);

  public List<DATE_TYPE> times();

  public DATE_TYPE[] timesArray();

  public List<VALUE_TYPE> values();

  public VALUE_TYPE[] valuesArray();

  public TimeSeries<DATE_TYPE, VALUE_TYPE> newInstance(DATE_TYPE[] dateTimes, VALUE_TYPE[] values);
  
}
