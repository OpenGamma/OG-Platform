/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.DateResolvers;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class MonthlyScheduleCalculator extends Schedule {

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      return new LocalDate[] {startDate};
    }
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    if (fromEnd) {
      LocalDate date = endDate;
      while (!date.isBefore(startDate)) {
        dates.add(date);
        date = date.minusMonths(1, DateResolvers.strict()); //TODO have to work out what to do for things like 31-2
      }
      return getReversedDates(dates);
    }
    LocalDate date = startDate;
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusMonths(1, DateResolvers.strict());
    }
    return dates.toArray(EMPTY_ARRAY);
  };
}
