/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
 * 
 */
public class PreviousValuePaddingTimeSeriesSamplingFunction implements TimeSeriesSamplingFunction {

  @Override
  public DoubleTimeSeries<?> getSampledTimeSeries(final DoubleTimeSeries<?> ts, final LocalDate[] schedule) {
    Validate.notNull(ts, "time series");
    Validate.notNull(schedule, "schedule");
    final LocalDateDoubleTimeSeries localDateTS = ts.toLocalDateDoubleTimeSeries();
    final List<LocalDate> tsDates = localDateTS.times();
    final List<LocalDate> scheduledDates = new ArrayList<LocalDate>();
    final List<Double> scheduledData = new ArrayList<Double>();
    for (final LocalDate localDate : schedule) {
      scheduledDates.add(localDate);
      if (tsDates.contains(localDate)) {
        scheduledData.add(localDateTS.getValue(localDate));
      } else {
        if (localDateTS.getEarliestTime().isAfter(localDate)) {
          throw new IllegalArgumentException("Could not get any data for date " + localDate);
        } else {
          LocalDate temp = localDate.minusDays(1);
          while (!tsDates.contains(temp)) {
            temp = temp.minusDays(1);
            if (temp.isBefore(schedule[0].toLocalDate()) || temp.isBefore(tsDates.get(0))) {
              throw new IllegalArgumentException("Could not get any data for date " + localDate);
            }
          }
          scheduledData.add(localDateTS.getValue(temp));
        }
      }
    }
    return new ArrayLocalDateDoubleTimeSeries(scheduledDates, scheduledData);
  }
}
