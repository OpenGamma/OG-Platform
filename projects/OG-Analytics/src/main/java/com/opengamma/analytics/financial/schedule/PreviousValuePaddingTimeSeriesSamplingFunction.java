/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class PreviousValuePaddingTimeSeriesSamplingFunction implements TimeSeriesSamplingFunction {

  @Override
  public DoubleTimeSeries<?> getSampledTimeSeries(final DoubleTimeSeries<?> ts, final LocalDate[] schedule) {
    ArgumentChecker.notNull(ts, "time series");
    ArgumentChecker.notNull(schedule, "schedule");
    final LocalDateDoubleTimeSeries localDateTS = ts.toLocalDateDoubleTimeSeries();
    final LocalDate[] tsDates = localDateTS.timesArray();
    final double[] values = localDateTS.valuesArrayFast();
    final List<LocalDate> scheduledDates = new ArrayList<LocalDate>();
    final List<Double> scheduledData = new ArrayList<Double>();
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
    return new ArrayLocalDateDoubleTimeSeries(scheduledDates, scheduledData);
  }
}
