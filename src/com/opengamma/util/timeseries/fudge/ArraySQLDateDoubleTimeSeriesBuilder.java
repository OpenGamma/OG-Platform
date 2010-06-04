/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.sql.Date;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.ArraySQLDateDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ArrayDateDoubleTimeSeries
 */
public class ArraySQLDateDoubleTimeSeriesBuilder extends FastBackedDoubleTimeSeriesBuilder<Date, ArraySQLDateDoubleTimeSeries> {
  @Override
  public ArraySQLDateDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new ArraySQLDateDoubleTimeSeries(converter, (FastIntDoubleTimeSeries) dts);
  }
}
