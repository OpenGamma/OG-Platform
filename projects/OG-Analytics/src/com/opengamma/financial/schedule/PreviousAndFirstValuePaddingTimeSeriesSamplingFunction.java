/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.schedule;

import javax.time.calendar.LocalDate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Pad with previous value, pad with first value in series if there is insufficient data
 */
public class PreviousAndFirstValuePaddingTimeSeriesSamplingFunction implements TimeSeriesSamplingFunction {

  @Override
  public DoubleTimeSeries<?> getSampledTimeSeries(final DoubleTimeSeries<?> ts, final LocalDate[] schedule) {
    ArgumentChecker.notNull(ts, "time series");
    ArgumentChecker.notNull(schedule, "schedule");
    final LocalDateDoubleTimeSeries localDateTS = ts.toLocalDateDoubleTimeSeries();
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
    return new ArrayLocalDateDoubleTimeSeries(schedule, scheduledData);
  }
}
