/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries.sampling;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Pad with previous value, pad with first value in series if there is insufficient data
 */
public class PreviousAndFirstValuePaddingTimeSeriesSamplingFunction implements TimeSeriesSamplingFunction {

  @Override
  public DoubleTimeSeries<?> getSampledTimeSeries(final DoubleTimeSeries<?> ts, final LocalDate[] schedule) {
    Validate.notNull(ts, "time series");
    Validate.notNull(schedule, "schedule");
    final LocalDateDoubleTimeSeries localDateTS = ts.toLocalDateDoubleTimeSeries();
    final LocalDate[] tsDates = localDateTS.timesArray();
    final double[] values = localDateTS.valuesArrayFast();
    
    final double[] scheduledData = new double[schedule.length];
    
    int j = 0;
    for (int i = 0; i < schedule.length; i++) {
      LocalDate localDate = schedule[i];
      if (j < tsDates.length) { //TODO break out
        if (tsDates[j].equals(localDate)) {
          scheduledData[i] = values[j];
          j++;
          continue;
        }
        while (j < tsDates.length && tsDates[j].isBefore(localDate)) {
          j++;
        }
        if (j < tsDates.length && tsDates[j].equals(localDate)) {
          scheduledData[i] = values[j];
          j++;
          continue;
        }
      }
      if (j > 0) {
        scheduledData[i] = values[j - 1]; //Should never go too high
      } else {
        scheduledData[i] = values[0];
      }
    }
    
    return new ArrayLocalDateDoubleTimeSeries(schedule, scheduledData);
  }
}
