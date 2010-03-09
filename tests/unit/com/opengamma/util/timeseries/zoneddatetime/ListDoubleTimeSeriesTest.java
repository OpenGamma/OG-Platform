package com.opengamma.util.timeseries.zoneddatetime;


import java.util.List;

import javax.time.calendar.ZonedDateTime;

public class ListDoubleTimeSeriesTest extends ZonedDateTimeDoubleTimeSeriesTest {

  @Override
  public ZonedDateTimeDoubleTimeSeries createEmptyTimeSeries() {
    return ListZonedDateTimeDoubleTimeSeries.EMPTY_SERIES;
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries createTimeSeries(ZonedDateTime[] times, double[] values) {
    return new ListZonedDateTimeDoubleTimeSeries(times, values);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries createTimeSeries(List<ZonedDateTime> times, List<Double> values) {
    return new ListZonedDateTimeDoubleTimeSeries(times, values);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries createTimeSeries(ZonedDateTimeDoubleTimeSeries dts) {
    return new ListZonedDateTimeDoubleTimeSeries(dts);
  }

}
