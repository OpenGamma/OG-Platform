/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.schedule;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthDay;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class AnnualScheduleOnDayAndMonthCalculator extends Schedule {
  private final int _dayOfMonth;
  private final MonthOfYear _monthOfYear;

  public AnnualScheduleOnDayAndMonthCalculator(final int dayOfMonth, final MonthOfYear monthOfYear) {
    Validate.isTrue(dayOfMonth > 0);
    Validate.isTrue(monthOfYear.maxLengthInDays() > dayOfMonth);
    _dayOfMonth = dayOfMonth;
    _monthOfYear = monthOfYear;
  }

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate);
  }

  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfMonth() == _dayOfMonth && startDate.getMonthOfYear() == _monthOfYear) {
        return new LocalDate[] {startDate};
      }
      throw new IllegalArgumentException("Start date and end date were the same but the day of month and month of year were not those required");
    }
    final DateAdjuster dateAdjuster = MonthDay.of(_monthOfYear, _dayOfMonth);
    LocalDate date = startDate.with(dateAdjuster);
    if (date.isBefore(startDate)) {
      date = date.plusYears(1);
    }
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusYears(1);
    }
    return dates.toArray(EMPTY_LOCAL_DATE_ARRAY);
  }

  @Override
  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate);
  }

  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfMonth() == _dayOfMonth && startDate.getMonthOfYear() == _monthOfYear) {
        return new ZonedDateTime[] {startDate};
      }
      throw new IllegalArgumentException("Start date and end date were the same but the day of month and month of year were not those required");
    }
    final DateAdjuster dateAdjuster = MonthDay.of(_monthOfYear, _dayOfMonth);
    ZonedDateTime date = startDate.with(dateAdjuster);
    if (date.isBefore(startDate)) {
      date = date.plusYears(1);
    }
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusYears(1);
    }
    return dates.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
  }
}
