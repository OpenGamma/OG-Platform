/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ArrayZonedDateTimeDoubleTimeSeries
 */
@FudgeBuilderFor(ArrayZonedDateTimeDoubleTimeSeries.class)
public class ArrayZonedDateTimeDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<ZonedDateTime, ArrayZonedDateTimeDoubleTimeSeries> {
  @Override
  public ArrayZonedDateTimeDoubleTimeSeries makeSeries(DateTimeConverter<ZonedDateTime> converter, FastTimeSeries<?> dts) {
    return new ArrayZonedDateTimeDoubleTimeSeries(converter, (FastLongDoubleTimeSeries) dts);
  }
}
