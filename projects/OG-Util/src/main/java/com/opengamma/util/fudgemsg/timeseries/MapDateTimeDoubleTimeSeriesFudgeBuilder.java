/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import java.util.Date;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.date.time.MapDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for MapDateTimeDoubleTimeSeries.
 */
@FudgeBuilderFor(MapDateTimeDoubleTimeSeries.class)
public class MapDateTimeDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<Date, MapDateTimeDoubleTimeSeries> {
  @Override
  public MapDateTimeDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new MapDateTimeDoubleTimeSeries(converter, (FastMutableLongDoubleTimeSeries) dts);
  }
}
