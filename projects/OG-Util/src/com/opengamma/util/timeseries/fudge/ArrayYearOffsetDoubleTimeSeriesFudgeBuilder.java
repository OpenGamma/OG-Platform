/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.ArrayYearOffsetDoubleTimeSeries;

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
