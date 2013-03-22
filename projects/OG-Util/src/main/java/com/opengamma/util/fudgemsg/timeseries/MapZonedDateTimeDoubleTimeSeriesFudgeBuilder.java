/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.mapping.FudgeBuilderFor;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.MapZonedDateTimeDoubleTimeSeries;

/**
 * Fudge message builder (serializer/deserializer) for MapZonedDateTimeDoubleTimeSeries.
 */
@FudgeBuilderFor(MapZonedDateTimeDoubleTimeSeries.class)
public class MapZonedDateTimeDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<ZonedDateTime, MapZonedDateTimeDoubleTimeSeries> {
  @Override
  public MapZonedDateTimeDoubleTimeSeries makeSeries(DateTimeConverter<ZonedDateTime> converter, FastTimeSeries<?> dts) {
    return new MapZonedDateTimeDoubleTimeSeries(converter, (FastMutableLongDoubleTimeSeries) dts);
  }
}
