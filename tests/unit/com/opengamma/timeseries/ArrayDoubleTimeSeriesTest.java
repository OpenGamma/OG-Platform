 package com.opengamma.timeseries;


import java.util.Date;
import java.util.List;

import com.opengamma.timeseries.date.ArrayDateDoubleTimeSeries;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;

public class ArrayDoubleTimeSeriesTest extends DateDoubleTimeSeriesTest {

  @Override
  public DateDoubleTimeSeries createEmptyTimeSeries() {
    return new ArrayDateDoubleTimeSeries();
  }

  @Override
  public DateDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new ArrayDateDoubleTimeSeries(times, values);
  }

  @Override
  public DateDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new ArrayDateDoubleTimeSeries(times, values);
  }

  @Override
  public DateDoubleTimeSeries createTimeSeries(DateDoubleTimeSeries dts) {
    return new ArrayDateDoubleTimeSeries(dts);
  }

}
