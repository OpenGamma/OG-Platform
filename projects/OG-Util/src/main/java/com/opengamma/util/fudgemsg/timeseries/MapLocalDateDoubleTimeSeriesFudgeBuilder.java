/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.mapping.FudgeBuilderFor;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.timeseries.localdate.MapLocalDateDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for MapLocalDateDoubleTimeSeries.
 */
@FudgeBuilderFor(MapLocalDateDoubleTimeSeries.class)
public class MapLocalDateDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<LocalDate, MapLocalDateDoubleTimeSeries> {
  @Override
  public MapLocalDateDoubleTimeSeries makeSeries(DateTimeConverter<LocalDate> converter, FastTimeSeries<?> dts) {
    return new MapLocalDateDoubleTimeSeries(converter, (FastMutableIntDoubleTimeSeries) dts);
  }
}
