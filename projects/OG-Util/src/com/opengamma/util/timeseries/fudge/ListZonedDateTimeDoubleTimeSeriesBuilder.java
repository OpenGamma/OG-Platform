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
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ListZonedDateTimeDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ListZonedDateTimeDoubleTimeSeries
 */
@FudgeBuilderFor(ListZonedDateTimeDoubleTimeSeries.class)
public class ListZonedDateTimeDoubleTimeSeriesBuilder extends FastBackedDoubleTimeSeriesBuilder<ZonedDateTime, ListZonedDateTimeDoubleTimeSeries> {
  @Override
  public ListZonedDateTimeDoubleTimeSeries makeSeries(DateTimeConverter<ZonedDateTime> converter, FastTimeSeries<?> dts) {
    return new ListZonedDateTimeDoubleTimeSeries(converter, (FastMutableLongDoubleTimeSeries) dts);
  }
}
