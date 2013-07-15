/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Specialized iterator for time-series.
 * <p>
 * This is a map-based iterator that avoids working with {@code Map.Entry}.
 * Using this iterator typically involves using a while loop.
 * 
 * @param <T>  the time type
 * @param <V>  the value being viewed over time
 */
public interface EntryIterator<T, V> extends Iterator<Map.Entry<T, V>> {

  /**
   * The next available time in the iterator.
   * Use instead of calling {@code next()}, use this method and {@code currentValue()}.
   * 
   * @return the next date, not null
   * @throws NoSuchElementException if the iterator is exhausted
   */
  T nextTime();

  /**
   * The current time in the iterator.
   * This returns the same as the last call to {@code nextTime()}.
   * 
   * @return the current date, not null
   * @throws IllegalStateException if the iterator has not been started
   */
  T currentTime();

  /**
   * The current value in the iterator.
   * This returns the value associated with the last call to {@code next()}.
   * 
   * @return the current value
   * @throws IllegalStateException if the iterator has not been started
   */
  V currentValue();

  /**
   * The current index of the iterator.
   * This returns the index of the time-value pair associated with the
   * last call to {@code next()}, or -1 if iteration has not yet started.
   * 
   * @return the current index, or -1 if iteration has not yet started
   */
  int currentIndex();

}
