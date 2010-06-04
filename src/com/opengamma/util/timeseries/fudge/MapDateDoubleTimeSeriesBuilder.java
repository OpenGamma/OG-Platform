/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.util.Date;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.date.MapDateDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for MapDateDoubleTimeSeries
 */
public class MapDateDoubleTimeSeriesBuilder extends FastBackedDoubleTimeSeriesBuilder<Date, MapDateDoubleTimeSeries> {
  @Override
  public MapDateDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new MapDateDoubleTimeSeries(converter, (FastMutableIntDoubleTimeSeries) dts);
  }
}
