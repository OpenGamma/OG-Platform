/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.ZonedDateTime;

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
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate);
  }

  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate) {
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
    int year = endDate.getYear();
    MonthOfYear month = startDate.getMonthOfYear();
    LocalDate date = startDate.withMonthOfYear(month.getValue()).withDayOfMonth(_dayOfMonth);
    if (date.isBefore(startDate)) {
      month = month.next();
      date = date.withMonthOfYear(month.getValue());
    }
    year = date.getYear();
    while (!date.isAfter(endDate)) {
      dates.add(date);
      month = month.next();
      if (month == MonthOfYear.JANUARY) {
        year++;
      }
      date = LocalDate.of(year, month.getValue(), _dayOfMonth);
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
      if (startDate.getDayOfMonth() == _dayOfMonth) {
        return new ZonedDateTime[] {startDate};
      }
      throw new IllegalArgumentException("Start date and end date were the same but their day of month was not the same as that required");
    }
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    int year = endDate.getYear();
    MonthOfYear month = startDate.getMonthOfYear();
    ZonedDateTime date = startDate.withMonthOfYear(month.getValue()).withDayOfMonth(_dayOfMonth);
    if (date.isBefore(startDate)) {
      month = month.next();
      date = date.withMonthOfYear(month.getValue());
    }
    year = date.getYear();
    while (!date.isAfter(endDate)) {
      dates.add(date);
      month = month.next();
      if (month == MonthOfYear.JANUARY) {
        year++;
      }
      date = date.withDate(year, month.getValue(), _dayOfMonth);
    }
    return dates.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
  }

}
