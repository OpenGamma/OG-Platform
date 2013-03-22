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
import com.opengamma.timeseries.yearoffset.ListYearOffsetDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ListYearOffsetDoubleTimeSeries
 */
@FudgeBuilderFor(ListYearOffsetDoubleTimeSeries.class)
public class ListYearOffsetDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<Double, ListYearOffsetDoubleTimeSeries> {
  @Override
  public ListYearOffsetDoubleTimeSeries makeSeries(DateTimeConverter<Double> converter, FastTimeSeries<?> dts) {
    return new ListYearOffsetDoubleTimeSeries(converter, (FastMutableLongDoubleTimeSeries) dts);
  }
}
