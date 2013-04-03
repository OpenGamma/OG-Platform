/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import java.sql.Date;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.sqldate.ListSQLDateDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ListDateDoubleTimeSeries
 */
@FudgeBuilderFor(ListSQLDateDoubleTimeSeries.class)
public class ListSQLDateDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<Date, ListSQLDateDoubleTimeSeries> {
  @Override
  public ListSQLDateDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new ListSQLDateDoubleTimeSeries(converter, (FastMutableIntDoubleTimeSeries) dts);
  }
}
