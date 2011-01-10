/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.MapYearOffsetDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for MapYearOffsetDoubleTimeSeries.
 */
@FudgeBuilderFor(MapYearOffsetDoubleTimeSeries.class)
public class MapYearOffsetDoubleTimeSeriesBuilder extends FastBackedDoubleTimeSeriesBuilder<Double, MapYearOffsetDoubleTimeSeries> {
  @Override
  public MapYearOffsetDoubleTimeSeries makeSeries(DateTimeConverter<Double> converter, FastTimeSeries<?> dts) {
    return new MapYearOffsetDoubleTimeSeries(converter, (FastMutableLongDoubleTimeSeries) dts);
  }
}
