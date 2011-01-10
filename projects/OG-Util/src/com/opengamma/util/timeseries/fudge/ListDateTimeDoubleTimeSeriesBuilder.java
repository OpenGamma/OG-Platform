/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.util.Date;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.date.time.ListDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.FastTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ListDateTimeDoubleTimeSeries
 */
@FudgeBuilderFor(ListDateTimeDoubleTimeSeries.class)
public class ListDateTimeDoubleTimeSeriesBuilder extends FastBackedDoubleTimeSeriesBuilder<Date, ListDateTimeDoubleTimeSeries> {
  @Override
  public ListDateTimeDoubleTimeSeries makeSeries(DateTimeConverter<Date> converter, FastTimeSeries<?> dts) {
    return new ListDateTimeDoubleTimeSeries(converter, (FastMutableLongDoubleTimeSeries) dts);
  }
}
