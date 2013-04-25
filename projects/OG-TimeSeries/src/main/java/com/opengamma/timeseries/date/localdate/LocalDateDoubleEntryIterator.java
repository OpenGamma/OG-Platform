/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.DateDoubleEntryIterator;
import com.opengamma.timeseries.date.DateTimeSeries;

/**
 * Specialized iterator for time-series of {@code double} values.
 * <p>
 * This is a map-based iterator that avoids working with {@code Map.Entry}.
 * Using this iterator typically involves using a while loop.
 * This iterator is dedicated to {@code LocalDateDoubleTimeSeries}.
 * <p>
 * The "time" key to the time-series is a {@code LocalDate}.
 * See {@link DateTimeSeries} for details about the "time" represented as an {@code int}.
 */
public interface LocalDateDoubleEntryIterator extends DateDoubleEntryIterator<LocalDate> {

}
