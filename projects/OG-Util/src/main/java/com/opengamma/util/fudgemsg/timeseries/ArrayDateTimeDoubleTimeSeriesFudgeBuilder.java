/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import java.util.Date;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.date.time.ArrayDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ArrayDateTimeDoubleTimeSeries
 */
@FudgeBuilderFor(ArrayDateTimeDoubleTimeSeries.class)
public class ArrayDateTimeDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<Date, ArrayDateTimeDoubleTimeSeries> {
  @Override
  public ArrayDateTimeDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new ArrayDateTimeDoubleTimeSeries(converter, (FastLongDoubleTimeSeries) dts);
  }
}
