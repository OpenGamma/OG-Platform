/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.timeseries.yearoffset.ArrayYearOffsetDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ArrayYearOffsetDoubleTimeSeries
 */
@FudgeBuilderFor(ArrayYearOffsetDoubleTimeSeries.class)
public class ArrayYearOffsetDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<Double, ArrayYearOffsetDoubleTimeSeries> {
  @Override
  public ArrayYearOffsetDoubleTimeSeries makeSeries(DateTimeConverter<Double> converter, FastTimeSeries<?> dts) {
    return new ArrayYearOffsetDoubleTimeSeries(converter, (FastLongDoubleTimeSeries) dts);
  }
}
