/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.util.Date;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.date.time.MapDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for MapDateTimeDoubleTimeSeries.
 */
@FudgeBuilderFor(MapDateTimeDoubleTimeSeries.class)
public class MapDateTimeDoubleTimeSeriesBuilder extends FastBackedDoubleTimeSeriesBuilder<Date, MapDateTimeDoubleTimeSeries> {
  @Override
  public MapDateTimeDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new MapDateTimeDoubleTimeSeries(converter, (FastMutableLongDoubleTimeSeries) dts);
  }
}
