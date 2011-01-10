/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.sql.Date;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.ListSQLDateDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ListDateDoubleTimeSeries
 */
@FudgeBuilderFor(ListSQLDateDoubleTimeSeries.class)
public class ListSQLDateDoubleTimeSeriesBuilder extends FastBackedDoubleTimeSeriesBuilder<Date, ListSQLDateDoubleTimeSeries> {
  @Override
  public ListSQLDateDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new ListSQLDateDoubleTimeSeries(converter, (FastMutableIntDoubleTimeSeries) dts);
  }
}
