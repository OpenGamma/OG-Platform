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
public class MonthlyScheduleOnDayCalculator extends Schedule {
  private final int _dayOfMonth;

  public MonthlyScheduleOnDayCalculator(final int dayOfMonth) {
    Validate.isTrue(dayOfMonth > 0 && dayOfMonth < 32);
    _dayOfMonth = dayOfMonth;
  }

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfMonth() == _dayOfMonth) {
        return new LocalDate[] {startDate};
      }
      throw new IllegalArgumentException("Start date and end date were the same but their day of month was not the same as that required");
    }
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    if (fromEnd) {
      LocalDate date = endDate.withDayOfMonth(_dayOfMonth, DateResolvers.strict());
      if (date.isAfter(endDate)) {
        date = date.minusMonths(1, DateResolvers.strict());
      }
      while (!date.isBefore(startDate)) {
        dates.add(date);
        date = date.minusMonths(1, DateResolvers.strict()); //TODO have to work out what to do for things like 31-2
      }
      return getReversedDates(dates);
    }
    LocalDate date = startDate;
    date = date.withDayOfMonth(_dayOfMonth, DateResolvers.strict());
    if (date.isBefore(startDate)) {
      date = date.plusMonths(1, DateResolvers.strict());
    }
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusMonths(1, DateResolvers.strict());
    }
    return dates.toArray(EMPTY_ARRAY);
  };

}
