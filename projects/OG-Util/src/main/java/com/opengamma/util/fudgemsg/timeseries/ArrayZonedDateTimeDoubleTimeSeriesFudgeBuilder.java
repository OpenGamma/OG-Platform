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
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

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
