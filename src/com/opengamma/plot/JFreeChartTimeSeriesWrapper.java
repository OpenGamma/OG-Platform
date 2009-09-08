package com.opengamma.plot;

import java.util.Map;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;

import com.opengamma.financial.timeseries.DoubleTimeSeries;

public class JFreeChartTimeSeriesWrapper {
  private static final JFreeChartTimeSeriesWrapper WRAPPER = new JFreeChartTimeSeriesWrapper();

  public static TimeSeries getJFreeChartTimeSeries(DoubleTimeSeries ts, String title) {
    TimeSeries jFreeChartTimeSeries = new TimeSeries(title, ConvertedTimePeriod.class);
    for (Map.Entry<InstantProvider, Double> entry : ts) {
      jFreeChartTimeSeries.add(WRAPPER.new ConvertedTimePeriod(entry.getKey().toInstant()), entry.getValue());
    }
    return jFreeChartTimeSeries;
  }

  private class ConvertedTimePeriod extends FixedMillisecond {
    private static final long serialVersionUID = 2685226417956465236L;
    private Instant _instant;

    public ConvertedTimePeriod(Instant instant) {
      super(instant.toEpochMillis());
      _instant = instant;
    }

    public Instant getInstant() {
      return _instant;
    }
  }
}
