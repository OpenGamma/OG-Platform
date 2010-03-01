package com.opengamma.timeseries;


import java.util.Date;
import java.util.List;

import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.MapDateDoubleTimeSeries;

public class MapDoubleTimeSeriesTest extends DateDoubleTimeSeriesTest {

  @Override
  public DateDoubleTimeSeries createEmptyTimeSeries() {
    return new MapDateDoubleTimeSeries();
  }

  @Override
  public DateDoubleTimeSeries createTimeSeries(Date[] times, double[] values) {
    return new MapDateDoubleTimeSeries(times, values);
  }

  @Override
  public DateDoubleTimeSeries createTimeSeries(List<Date> times, List<Double> values) {
    return new MapDateDoubleTimeSeries(times, values);
  }
  
  @Override
  public DateDoubleTimeSeries createTimeSeries(DateDoubleTimeSeries dts) {
    return new MapDateDoubleTimeSeries(dts);
  }

}
