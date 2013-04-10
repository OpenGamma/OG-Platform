/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.localdate;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.DateEntryIterator;

/**
 * Specialized iterator that can access primitive values.
 * This iterator is dedicated to {@code LocalDateDoubleTimeSeries}.
 * 
 * @param <V>  the value being viewed over time
 */
public interface LocalDateObjectEntryIterator<V> extends DateEntryIterator<LocalDate, V> {

}
