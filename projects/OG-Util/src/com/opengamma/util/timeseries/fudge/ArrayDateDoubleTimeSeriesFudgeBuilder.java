/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.util.Date;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.date.ArrayDateDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ArrayDateDoubleTimeSeries
 */
@FudgeBuilderFor(ArrayDateDoubleTimeSeries.class)
public class ArrayDateDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<Date, ArrayDateDoubleTimeSeries> {
  @Override
  public ArrayDateDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new ArrayDateDoubleTimeSeries(converter, (FastIntDoubleTimeSeries) dts);
  }
}
