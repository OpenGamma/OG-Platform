/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.DateDoubleEntryIterator;

/**
 * Specialized iterator that can access primitive values.
 * This iterator is dedicated to {@code LocalDateDoubleTimeSeries}.
 */
public interface LocalDateDoubleEntryIterator extends DateDoubleEntryIterator<LocalDate> {

}
