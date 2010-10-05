/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DateResolvers;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class FirstOfYearScheduleCalculator extends Schedule {

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfMonth() == 1 && startDate.getMonthOfYear() == MonthOfYear.JANUARY) {
        return new LocalDate[] {startDate};
      }
      throw new IllegalArgumentException("Start date and end date were the same but neither was the first day of the year");
    }
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    if (fromEnd) {
      LocalDate date = endDate;
      date = date.with(DateAdjusters.firstDayOfYear());
      while (!date.isBefore(startDate)) {
        dates.add(date);
        date = date.minusYears(1, DateResolvers.previousValid());
      }
      return getReversedDates(dates);
    }
    LocalDate date = startDate;
    date = date.with(DateAdjusters.firstDayOfYear());
    if (date.isBefore(startDate)) {
      date = date.plusYears(1, DateResolvers.nextValid());
    }
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusYears(1, DateResolvers.nextValid());
    }
    return dates.toArray(EMPTY_ARRAY);
  }
}
