package com.opengamma.plot;

import java.util.Map;

import javax.time.Instant;
import javax.time.calendar.ZonedDateTime;

import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;

import com.opengamma.timeseries.DoubleTimeSeries;

public class JFreeChartTimeSeriesWrapper {
  private static final JFreeChartTimeSeriesWrapper WRAPPER = new JFreeChartTimeSeriesWrapper();

  public static TimeSeries getJFreeChartTimeSeries(final DoubleTimeSeries ts, final String title) {
    final TimeSeries jFreeChartTimeSeries = new TimeSeries(title);
    for (final Map.Entry<ZonedDateTime, Double> entry : ts) {
      jFreeChartTimeSeries.add(WRAPPER.new ConvertedTimePeriod(entry.getKey().toInstant()), entry.getValue());
    }
    return jFreeChartTimeSeries;
  }

  private class ConvertedTimePeriod extends FixedMillisecond {
    private static final long serialVersionUID = 2685226417956465236L;
    private final Instant _instant;

    public ConvertedTimePeriod(final Instant instant) {
      super(instant.toEpochMillis());
      _instant = instant;
    }

    public Instant getInstant() {
      return _instant;
    }
  }
}
