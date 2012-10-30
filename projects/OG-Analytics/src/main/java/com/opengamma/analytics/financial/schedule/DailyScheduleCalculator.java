/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class DailyScheduleCalculator extends Schedule {

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate);
  }

  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      return new LocalDate[] {startDate};
    }
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    LocalDate date = startDate;
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusDays(1);
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
      return new ZonedDateTime[] {startDate};
    }
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    ZonedDateTime date = startDate;
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusDays(1);
    }
    return dates.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
  }

}
