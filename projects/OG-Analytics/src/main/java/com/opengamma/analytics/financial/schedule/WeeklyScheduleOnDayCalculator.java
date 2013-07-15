/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class WeeklyScheduleOnDayCalculator extends Schedule {
  private final DayOfWeek _dayOfWeek;

  public WeeklyScheduleOnDayCalculator(final DayOfWeek dayOfWeek) {
    _dayOfWeek = dayOfWeek;
  }

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate);
  }

  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate) {
    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(endDate, "end date");
    ArgumentChecker.isFalse(startDate.isAfter(endDate), "start date must not be after end date");
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfWeek() == _dayOfWeek) {
        return new LocalDate[] {startDate};
      }
      throw new IllegalArgumentException("Start date and end date were the same but their day of week was not the same as that required");
    }
    final List<LocalDate> dates = new ArrayList<>();
    LocalDate date = startDate;
    date = date.with(TemporalAdjusters.nextOrSame(_dayOfWeek));
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.with(TemporalAdjusters.next(_dayOfWeek));
    }
    return dates.toArray(EMPTY_LOCAL_DATE_ARRAY);
  }

  @Override
  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate);
  }

  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate) {
    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(endDate, "end date");
    ArgumentChecker.isFalse(startDate.isAfter(endDate), "start date must not be after end date");
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfWeek() == _dayOfWeek) {
        return new ZonedDateTime[] {startDate};
      }
      throw new IllegalArgumentException("Start date and end date were the same but their day of week was not the same as that required");
    }
    final List<ZonedDateTime> dates = new ArrayList<>();
    ZonedDateTime date = startDate;
    date = date.with(TemporalAdjusters.nextOrSame(_dayOfWeek));
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.with(TemporalAdjusters.next(_dayOfWeek));
    }
    return dates.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _dayOfWeek.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final WeeklyScheduleOnDayCalculator other = (WeeklyScheduleOnDayCalculator) obj;
    return _dayOfWeek == other._dayOfWeek;
  }
}
