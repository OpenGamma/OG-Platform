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
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class AnnualScheduleCalculator extends Schedule {

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final boolean generateRecursive) {
    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(endDate, "end date");
    ArgumentChecker.isFalse(startDate.isAfter(endDate), "start date must not be after end date");
    if (startDate.equals(endDate)) {
      return new LocalDate[] {startDate};
    }
    final List<LocalDate> dates = new ArrayList<>();
    if (fromEnd) {
      LocalDate date = endDate;
      int i = 1;
      while (!date.isBefore(startDate)) {
        dates.add(date);
        date = generateRecursive ? date.minus(Period.ofYears(1)) : endDate.minus(Period.ofYears(i++));
      }
      Collections.reverse(dates);
      return dates.toArray(EMPTY_LOCAL_DATE_ARRAY);
    }
    LocalDate date = startDate;
    int i = 1;
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = generateRecursive ? date.plus(Period.ofYears(1)) : startDate.plus(Period.ofYears(i++));
    }
    return dates.toArray(EMPTY_LOCAL_DATE_ARRAY);
  }

  @Override
  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final boolean fromEnd, final boolean generateRecursive) {
    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(endDate, "end date");
    ArgumentChecker.isFalse(startDate.isAfter(endDate), "start date must not be after end date");
    if (startDate.equals(endDate)) {
      return new ZonedDateTime[] {startDate};
    }
    final List<ZonedDateTime> dates = new ArrayList<>();
    if (fromEnd) {
      ZonedDateTime date = endDate;
      int i = 1;
      while (!date.isBefore(startDate)) {
        dates.add(date);
        date = generateRecursive ? date.minus(Period.ofYears(1)) : endDate.minus(Period.ofYears(i++));
      }
      Collections.reverse(dates);
      return dates.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
    }
    ZonedDateTime date = startDate;
    int i = 1;
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = generateRecursive ? date.plus(Period.ofYears(1)) : startDate.plus(Period.ofYears(i++));
    }
    return dates.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
  }

}
