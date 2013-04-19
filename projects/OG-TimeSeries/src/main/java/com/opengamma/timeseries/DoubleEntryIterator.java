/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

/**
 * Specialized iterator for time-series of {@code double} values.
 * <p>
 * This is a map-based iterator that avoids working with {@code Map.Entry}.
 * Using this iterator typically involves using a while loop.
 * 
 * @param <T>  the time type
 */
public interface DoubleEntryIterator<T> extends EntryIterator<T, Double> {

  /**
   * The current value in the iterator.
   * This returns the value associated with the last call to {@code next()}.
   * 
   * @return the current value
   * @throws IllegalStateException if the iterator has not been started
   */
  double currentValueFast();

}
