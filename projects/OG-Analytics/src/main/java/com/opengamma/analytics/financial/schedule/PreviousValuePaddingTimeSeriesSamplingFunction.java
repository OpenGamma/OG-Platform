/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class PreviousValuePaddingTimeSeriesSamplingFunction implements TimeSeriesSamplingFunction {

  @Override
  public LocalDateDoubleTimeSeries getSampledTimeSeries(final DateDoubleTimeSeries<?> ts, final LocalDate[] schedule) {
    ArgumentChecker.notNull(ts, "time series");
    ArgumentChecker.notNull(schedule, "schedule");
    final LocalDateDoubleTimeSeries localDateTS = ImmutableLocalDateDoubleTimeSeries.of(ts);
    final LocalDate[] tsDates = localDateTS.timesArray();
    final double[] values = localDateTS.valuesArrayFast();
    final List<LocalDate> scheduledDates = new ArrayList<>();
    final List<Double> scheduledData = new ArrayList<>();
    int dateIndex = 0;
    for (final LocalDate localDate : schedule) {
      if (dateIndex < tsDates.length) {
        if (tsDates[dateIndex].equals(localDate)) {
          scheduledDates.add(localDate);
          scheduledData.add(values[dateIndex]);
          dateIndex++;
          continue;
        }
        while (dateIndex < tsDates.length && tsDates[dateIndex].isBefore(localDate)) {
          dateIndex++;
        }
        if (dateIndex < tsDates.length && tsDates[dateIndex].equals(localDate)) {
          scheduledDates.add(localDate);
          scheduledData.add(values[dateIndex]);
          dateIndex++;
          continue;
        }
      }
      if (dateIndex > 0) {
        scheduledDates.add(localDate);
        scheduledData.add(values[dateIndex - 1]);
      }
    }
    return ImmutableLocalDateDoubleTimeSeries.of(scheduledDates, scheduledData);
  }
}
