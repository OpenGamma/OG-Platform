/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.precise.PreciseDoubleEntryIterator;
import com.opengamma.timeseries.precise.PreciseTimeSeries;

/**
 * Specialized iterator for time-series of {@code double} values.
 * <p>
 * This is a map-based iterator that avoids working with {@code Map.Entry}.
 * Using this iterator typically involves using a while loop.
 * This iterator is dedicated to {@code ZonedDateTimeDoubleTimeSeries}.
 * <p>
 * The "time" key to the time-series is an {@code ZonedDateTime}.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 */
public interface ZonedDateTimeDoubleEntryIterator extends PreciseDoubleEntryIterator<ZonedDateTime> {

}
