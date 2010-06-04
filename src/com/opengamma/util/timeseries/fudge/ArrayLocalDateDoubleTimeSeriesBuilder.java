/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import javax.time.calendar.LocalDate;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ArrayLocalDateDoubleTimeSeries
 */
public class ArrayLocalDateDoubleTimeSeriesBuilder extends FastBackedDoubleTimeSeriesBuilder<LocalDate, ArrayLocalDateDoubleTimeSeries> {
  @Override
  public ArrayLocalDateDoubleTimeSeries makeSeries(DateTimeConverter<LocalDate> converter, FastTimeSeries<?> dts) {
    return new ArrayLocalDateDoubleTimeSeries(converter, (FastIntDoubleTimeSeries) dts);
  }
}
