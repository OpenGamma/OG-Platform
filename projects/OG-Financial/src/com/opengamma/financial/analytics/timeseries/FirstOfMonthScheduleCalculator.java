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

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * 
 */
public class FirstOfMonthScheduleCalculator extends Schedule {

  @Override
  public LocalDate[] getScheduleWorkingDaysOnly(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final Calendar holidayCalendar) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfMonth() == 1) {
        return new LocalDate[] {startDate};
      }
      throw new IllegalArgumentException(
          "Start date and end date were the same but neither was the first day of the month");
    }
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    if (fromEnd) {
      LocalDate date = endDate;
      date = date.with(DateAdjusters.firstDayOfMonth());
      while (!date.isBefore(startDate)) {
        if (holidayCalendar.isWorkingDay(date)) {
          dates.add(date);
        }
        date = date.minusMonths(1, DateResolvers.previousValid());
      }
      return getReversedDates(dates);
    }
    LocalDate date = startDate;
    date = date.with(DateAdjusters.firstDayOfMonth());
    if (date.isBefore(startDate)) {
      date = date.plusMonths(1, DateResolvers.nextValid());
    }
    while (!date.isAfter(endDate)) {
      if (holidayCalendar.isWorkingDay(date)) {
        dates.add(date);
      }
      date = date.plusMonths(1, DateResolvers.nextValid());
    }
    return dates.toArray(EMPTY_ARRAY);
  }
}
