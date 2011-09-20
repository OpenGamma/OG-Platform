/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.sql.Date;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.ArraySQLDateDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ArrayDateDoubleTimeSeries
 */
@FudgeBuilderFor(ArraySQLDateDoubleTimeSeries.class)
public class ArraySQLDateDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<Date, ArraySQLDateDoubleTimeSeries> {
  @Override
  public ArraySQLDateDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new ArraySQLDateDoubleTimeSeries(converter, (FastIntDoubleTimeSeries) dts);
  }
}
