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

/**
 * 
 */
public class EndOfMonthScheduleCalculator extends Schedule {

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfMonth() == startDate.getMonthOfYear().lengthInDays(startDate.isLeapYear())) {
        return new LocalDate[] {startDate};
      }
      throw new IllegalArgumentException(
          "Start date and end date were the same but neither was the last day of the month");
    }
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    if (fromEnd) {
      LocalDate date = endDate;
      date = date.with(DateAdjusters.lastDayOfMonth());
      if (date.isAfter(endDate)) {
        date = date.minusMonths(1, DateResolvers.previousValid()).with(DateAdjusters.lastDayOfMonth());
      }
      while (!date.isBefore(startDate)) {
        dates.add(date);
        date = date.minusMonths(1, DateResolvers.previousValid()).with(DateAdjusters.lastDayOfMonth());
      }
      return getReversedDates(dates);
    }
    LocalDate date = startDate;
    date = date.with(DateAdjusters.lastDayOfMonth());
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusMonths(1, DateResolvers.previousValid()).with(DateAdjusters.lastDayOfMonth());
    }
    return dates.toArray(EMPTY_ARRAY);
  }
}
