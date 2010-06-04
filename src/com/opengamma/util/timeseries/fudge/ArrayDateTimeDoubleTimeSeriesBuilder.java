/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.util.Date;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.date.time.ArrayDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ArrayDateTimeDoubleTimeSeries
 */
public class ArrayDateTimeDoubleTimeSeriesBuilder extends FastBackedDoubleTimeSeriesBuilder<Date, ArrayDateTimeDoubleTimeSeries> {
  @Override
  public ArrayDateTimeDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new ArrayDateTimeDoubleTimeSeries(converter, (FastLongDoubleTimeSeries) dts);
  }
}
