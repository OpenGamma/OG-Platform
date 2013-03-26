/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.timeseries.yearoffset.MapYearOffsetDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for MapYearOffsetDoubleTimeSeries.
 */
@FudgeBuilderFor(MapYearOffsetDoubleTimeSeries.class)
public class MapYearOffsetDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<Double, MapYearOffsetDoubleTimeSeries> {
  @Override
  public MapYearOffsetDoubleTimeSeries makeSeries(DateTimeConverter<Double> converter, FastTimeSeries<?> dts) {
    return new MapYearOffsetDoubleTimeSeries(converter, (FastMutableLongDoubleTimeSeries) dts);
  }
}
