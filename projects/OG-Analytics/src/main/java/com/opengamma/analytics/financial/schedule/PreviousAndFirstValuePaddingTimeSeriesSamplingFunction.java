/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Pad with previous value, pad with first value in series if there is insufficient data
 */
public class PreviousAndFirstValuePaddingTimeSeriesSamplingFunction implements TimeSeriesSamplingFunction {

  @Override
  public LocalDateDoubleTimeSeries getSampledTimeSeries(final DateDoubleTimeSeries<?> ts, final LocalDate[] schedule) {
    ArgumentChecker.notNull(ts, "time series");
    ArgumentChecker.notNull(schedule, "schedule");
    final LocalDateDoubleTimeSeries localDateTS = ImmutableLocalDateDoubleTimeSeries.of(ts);
    final LocalDate[] tsDates = localDateTS.timesArray();
    final double[] values = localDateTS.valuesArrayFast();
    final double[] scheduledData = new double[schedule.length];
    int dateIndex = 0;
    for (int i = 0; i < schedule.length; i++) {
      final LocalDate localDate = schedule[i];
      if (dateIndex < tsDates.length) { //TODO break out
        if (tsDates[dateIndex].equals(localDate)) {
          scheduledData[i] = values[dateIndex];
          dateIndex++;
          continue;
        }
        while (dateIndex < tsDates.length && tsDates[dateIndex].isBefore(localDate)) {
          dateIndex++;
        }
        if (dateIndex < tsDates.length && tsDates[dateIndex].equals(localDate)) {
          scheduledData[i] = values[dateIndex];
          dateIndex++;
          continue;
        }
      }
      if (dateIndex > 0) {
        scheduledData[i] = values[dateIndex - 1]; //Should never go too high
      } else {
        scheduledData[i] = values[0];
      }
    }
    return ImmutableLocalDateDoubleTimeSeries.of(schedule, scheduledData);
  }
}
