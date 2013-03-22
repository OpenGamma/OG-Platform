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
import com.opengamma.timeseries.sqldate.MapSQLDateDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for MapDateDoubleTimeSeries.
 */
@FudgeBuilderFor(MapSQLDateDoubleTimeSeries.class)
public class MapSQLDateDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<Date, MapSQLDateDoubleTimeSeries> {
  @Override
  public MapSQLDateDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new MapSQLDateDoubleTimeSeries(converter, (FastMutableIntDoubleTimeSeries) dts);
  }
}
