/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import java.util.NoSuchElementException;

import com.opengamma.timeseries.EntryIterator;

/**
 * Specialized iterator for time-series of {@code Object} values.
 * <p>
 * This is a map-based iterator that avoids working with {@code Map.Entry}.
 * Using this iterator typically involves using a while loop.
 * This iterator is dedicated to {@code DateTimeSeries}.
 * <p>
 * The "time" key to the time-series is a date.
 * See {@link DateTimeSeries} for details about the "time" represented as an {@code int}.
 * 
 * @param <T>  the date type
 * @param <V>  the value being viewed over time
 */
public interface DateEntryIterator<T, V>
    extends EntryIterator<T, V> {

  /**
   * The next available date in the iterator.
   * Use instead of calling {@code next()}, use this method and {@code currentValue()}.
   * 
   * @return the next date
   * @throws NoSuchElementException if the iterator is exhausted
   */
  int nextTimeFast();

  /**
   * The current date in the iterator.
   * This returns the same as the last call to {@code nextTimeFast()}.
   * 
   * @return the current date
   * @throws IllegalStateException if the iterator has not been started
   */
  int currentTimeFast();

}
