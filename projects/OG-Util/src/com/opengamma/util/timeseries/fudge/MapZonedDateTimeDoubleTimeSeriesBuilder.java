/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.MapZonedDateTimeDoubleTimeSeries;

/**
 * Fudge message builder (serializer/deserializer) for MapZonedDateTimeDoubleTimeSeries.
 */
@FudgeBuilderFor(MapZonedDateTimeDoubleTimeSeries.class)
public class MapZonedDateTimeDoubleTimeSeriesBuilder extends FastBackedDoubleTimeSeriesBuilder<ZonedDateTime, MapZonedDateTimeDoubleTimeSeries> {
  @Override
  public MapZonedDateTimeDoubleTimeSeries makeSeries(DateTimeConverter<ZonedDateTime> converter, FastTimeSeries<?> dts) {
    return new MapZonedDateTimeDoubleTimeSeries(converter, (FastMutableLongDoubleTimeSeries) dts);
  }
}
