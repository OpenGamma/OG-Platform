/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import java.util.Date;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.timeseries.DateTimeConverter;
import com.opengamma.timeseries.date.time.ListDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.fast.FastTimeSeries;
import com.opengamma.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ListDateTimeDoubleTimeSeries
 */
@FudgeBuilderFor(ListDateTimeDoubleTimeSeries.class)
public class ListDateTimeDoubleTimeSeriesFudgeBuilder extends FastBackedDoubleTimeSeriesFudgeBuilder<Date, ListDateTimeDoubleTimeSeries> {
  @Override
  public ListDateTimeDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new ListDateTimeDoubleTimeSeries(converter, (FastMutableLongDoubleTimeSeries) dts);
  }
}
