/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class YearlyScheduleOnDayAndMonthCalculator extends Schedule {
  private final int _dayOfMonth;
  private final MonthOfYear _monthOfYear;

  public YearlyScheduleOnDayAndMonthCalculator(final int dayOfMonth, final MonthOfYear monthOfYear) {
    Validate.isTrue(dayOfMonth > 0);
    Validate.isTrue(monthOfYear.maxLengthInDays() > dayOfMonth);
    _dayOfMonth = dayOfMonth;
    _monthOfYear = monthOfYear;
  }

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfMonth() == _dayOfMonth && startDate.getMonthOfYear() == _monthOfYear) {
        return new LocalDate[] {startDate};
      }
      throw new IllegalArgumentException("Start date and end date were the same but the day of month and month of year were not those required");
    }
    if (fromEnd) {
      LocalDate date = endDate.with(_monthOfYear).withDayOfMonth(_dayOfMonth);
      if (date.isAfter(endDate)) {
        date = date.minusYears(1);
      }
      final List<LocalDate> dates = new ArrayList<LocalDate>();
      while (!date.isBefore(startDate)) {
        dates.add(date);
        date = date.minusYears(1);
      }
      return getReversedDates(dates);
    }
    LocalDate date = startDate.with(_monthOfYear).withDayOfMonth(_dayOfMonth);
    if (date.isBefore(startDate)) {
      date = date.plusYears(1);
    }
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusYears(1);
    }
    return dates.toArray(EMPTY_ARRAY);
  }
}
