/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class WeeklyScheduleCalculator extends Schedule {

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate, fromEnd);
  }

  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd) {
    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(endDate, "end date");
    ArgumentChecker.isFalse(startDate.isAfter(endDate), "start date must not be after end date");
    final List<LocalDate> dates = new ArrayList<>();
    if (fromEnd) {
      LocalDate date = endDate;
      while (!date.isBefore(startDate)) {
        dates.add(date);
        date = date.minusDays(7);
      }
      Collections.reverse(dates);
      return dates.toArray(EMPTY_LOCAL_DATE_ARRAY);
    }
    LocalDate date = startDate;
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusDays(7);
    }
    return dates.toArray(EMPTY_LOCAL_DATE_ARRAY);
  }

  @Override
  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate, fromEnd);
  }

  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final boolean fromEnd) {
    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(endDate, "end date");
    ArgumentChecker.isFalse(startDate.isAfter(endDate), "start date must not be after end date");
    final List<ZonedDateTime> dates = new ArrayList<>();
    if (fromEnd) {
      ZonedDateTime date = endDate;
      while (!date.isBefore(startDate)) {
        dates.add(date);
        date = date.minusDays(7);
      }
      Collections.reverse(dates);
      return dates.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
    }
    ZonedDateTime date = startDate;
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusDays(7);
    }
    return dates.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
  }
}
