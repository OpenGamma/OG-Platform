/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * 
 */
public class WeeklyScheduleOnDayCalculator extends Schedule {
  private final DayOfWeek _dayOfWeek;

  public WeeklyScheduleOnDayCalculator(final DayOfWeek dayOfWeek) {
    _dayOfWeek = dayOfWeek;
  }

  @Override
  public LocalDate[] getScheduleWorkingDaysOnly(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final Calendar holidayCalendar) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfWeek() == _dayOfWeek) {
        return new LocalDate[] {startDate};
      }
      throw new IllegalArgumentException(
          "Start date and end date were the same but their day of week was not the same as that required");
    }
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    if (fromEnd) {
      LocalDate date = endDate;
      date = date.with(DateAdjusters.previousOrCurrent(_dayOfWeek));
      while (!date.isBefore(startDate)) {
        if (holidayCalendar.isWorkingDay(date)) {
          dates.add(date);
        }
        date = date.with(DateAdjusters.previous(_dayOfWeek));
      }
      return getReversedDates(dates);
    }
    LocalDate date = startDate;
    date = date.with(DateAdjusters.nextOrCurrent(_dayOfWeek));
    while (!date.isAfter(endDate)) {
      if (holidayCalendar.isWorkingDay(date)) {
        dates.add(date);
      }
      date = date.with(DateAdjusters.next(_dayOfWeek));
    }
    return dates.toArray(EMPTY_ARRAY);
  }
}
