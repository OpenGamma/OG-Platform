/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.threeten.bp.LocalDate;

/**
 * Specialized iterator that can access primitive values.
 * This iterator is dedicated to {@code LocalDateDoubleTimeSeries}.
 */
public interface LocalDateDoubleIterator extends Iterator<Map.Entry<LocalDate, Double>> {

  /**
   * The next available date in the iterator.
   * Use instead of calling {@code next()}, use this method and {@code currentValue()}.
   * 
   * @return the next date
   * @throws NoSuchElementException if the iterator is exhausted
   */
  int nextDate();

  /**
   * The next available date in the iterator.
   * Use instead of calling {@code next()}, use this method and {@code currentValue()}.
   * 
   * @return the next date
   * @throws NoSuchElementException if the iterator is exhausted
   */
  LocalDate nextLocalDate();

  /**
   * The current date in the iterator.
   * This returns the same as the last call to {@code nextDate()}.
   * 
   * @return the current date
   * @throws IllegalStateException if the iterator has not been started
   */
  int currentDate();

  /**
   * The current date in the iterator.
   * This returns the same as the last call to {@code nextDate()}.
   * 
   * @return the current date
   * @throws IllegalStateException if the iterator has not been started
   */
  LocalDate currentLocalDate();

  /**
   * The current value in the iterator.
   * This returns the value associated with the last call to {@code nextDate()}.
   * 
   * @return the current value
   * @throws IllegalStateException if the iterator has not been started
   */
  double currentValue();

}
