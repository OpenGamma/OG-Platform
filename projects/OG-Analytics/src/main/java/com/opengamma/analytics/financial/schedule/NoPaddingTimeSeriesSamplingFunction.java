/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

public class NoPaddingTimeSeriesSamplingFunction implements TimeSeriesSamplingFunction {

  @Override
  public LocalDateDoubleTimeSeries getSampledTimeSeries(DateDoubleTimeSeries<?> ts, LocalDate[] schedule) {
    ArgumentChecker.notNull(ts, "time series");
    ArgumentChecker.notNull(schedule, "schedule");
    LocalDateDoubleTimeSeries localDateTS = ImmutableLocalDateDoubleTimeSeries.of(ts);
    Set<LocalDate> timeSeriesSet = Sets.newHashSet(localDateTS.times());
    Set<LocalDate> scheduleSet = Sets.newHashSet(schedule);
    List<LocalDate> scheduledDates = Lists.newArrayList(Sets.intersection(scheduleSet, timeSeriesSet));
    Collections.sort(scheduledDates);
    List<Double> scheduledData = Lists.newArrayList();
    for (LocalDate localDate : scheduledDates) {
      scheduledData.add(localDateTS.getValue(localDate));
    }
    return ImmutableLocalDateDoubleTimeSeries.of(scheduledDates, scheduledData);
  }
}
