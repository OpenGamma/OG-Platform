/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise;

import java.util.NoSuchElementException;

import com.opengamma.timeseries.EntryIterator;

/**
 * Specialized iterator for time-series of {@code Object} values.
 * <p>
 * This is a map-based iterator that avoids working with {@code Map.Entry}.
 * Using this iterator typically involves using a while loop.
 * This iterator is dedicated to {@code InstantTimeSeries}.
 * <p>
 * The "time" key to the time-series is an instant.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 * 
 * @param <T>  the instant type
 * @param <V>  the value being viewed over time
 */
public interface PreciseEntryIterator<T, V>
    extends EntryIterator<T, V> {

  /**
   * The next available instant in the iterator.
   * Use instead of calling {@code next()}, use this method and {@code currentValue()}.
   * 
   * @return the next instant
   * @throws NoSuchElementException if the iterator is exhausted
   */
  long nextTimeFast();

  /**
   * The current instant in the iterator.
   * This returns the same as the last call to {@code nextTimeFast()}.
   * 
   * @return the current instant
   * @throws IllegalStateException if the iterator has not been started
   */
  long currentTimeFast();

}
