/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import javax.time.calendar.LocalDate;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for MapLocalDateDoubleTimeSeries.
 */
@FudgeBuilderFor(MapLocalDateDoubleTimeSeries.class)
public class MapLocalDateDoubleTimeSeriesBuilder extends FastBackedDoubleTimeSeriesBuilder<LocalDate, MapLocalDateDoubleTimeSeries> {
  @Override
  public MapLocalDateDoubleTimeSeries makeSeries(DateTimeConverter<LocalDate> converter, FastTimeSeries<?> dts) {
    return new MapLocalDateDoubleTimeSeries(converter, (FastMutableIntDoubleTimeSeries) dts);
  }
}
