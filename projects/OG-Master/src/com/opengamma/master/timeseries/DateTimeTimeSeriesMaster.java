/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;

import java.util.Date;

import com.opengamma.util.PublicSPI;

/**
 * A time-series master that uses {@code java.util.Date}.
 * <p>
 * This master provides information at resolutions up to millisecond.
 */
@PublicSPI
public interface DateTimeTimeSeriesMaster extends TimeSeriesMaster<Date> {

}
