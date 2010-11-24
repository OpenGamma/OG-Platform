/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;

import javax.time.calendar.LocalDate;

/**
 * A time-series master that uses {@code LocalDate}.
 * <p>
 * This master provides information at daily resolution.
 */
public interface LocalDateTimeSeriesMaster extends TimeSeriesMaster<LocalDate> {

}
