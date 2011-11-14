/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries.sampling;

import java.util.ArrayList;
import java.util.List;

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
    final List<LocalDate> tsDates = localDateTS.times();
    double[] values = localDateTS.valuesArrayFast();
    
    
    final List<LocalDate> scheduledDates = new ArrayList<LocalDate>(schedule.length);
    final List<Double> scheduledData = new ArrayList<Double>(schedule.length);
    
    int j = 0;
    for (int i = 0; i < schedule.length; i++) {
      LocalDate localDate = schedule[i];
      scheduledDates.add(localDate);
      
      if (j < tsDates.size()) { //TODO break out
        if (tsDates.get(j).equals(localDate)) {
          scheduledData.add(values[j]);
          j++;
          continue;
        }
        while (j < tsDates.size() && tsDates.get(j).isBefore(localDate)) {
          j++;
        }
        if (j < tsDates.size() && tsDates.get(j).equals(localDate)) {
          scheduledData.add(values[j]);
          j++;
          continue;
        }
      }
      if (j > 0) {
        scheduledData.add(values[j - 1]); //Should never go too high
      } else {
        scheduledData.add(values[0]);
      }
    }
    
    return new ArrayLocalDateDoubleTimeSeries(scheduledDates, scheduledData);
  }
}
