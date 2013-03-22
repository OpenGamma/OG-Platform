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
import com.opengamma.timeseries.localdate.ListLocalDateDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ListLocalDateDoubleTimeSeries
 */
@FudgeBuilderFor(ListLocalDateDoubleTimeSeries.class)
public class ListLocalDateDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<LocalDate, ListLocalDateDoubleTimeSeries> {
  @Override
  public ListLocalDateDoubleTimeSeries makeSeries(DateTimeConverter<LocalDate> converter, FastTimeSeries<?> dts) {
    return new ListLocalDateDoubleTimeSeries(converter, (FastMutableIntDoubleTimeSeries) dts);
  }
}
