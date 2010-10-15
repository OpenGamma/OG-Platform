/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.List;

import javax.time.calendar.LocalDate;

import org.apache.commons.collections.iterators.ReverseListIterator;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * 
 */
public abstract class Schedule {
  /** Empty array of LocalDate */
  protected static final LocalDate[] EMPTY_ARRAY = new LocalDate[0];
  private static final Calendar NO_HOLIDAY = new NoHolidayCalendar();

  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd) {
    return getScheduleWorkingDaysOnly(startDate, endDate, fromEnd, NO_HOLIDAY);
  }

  public abstract LocalDate[] getScheduleWorkingDaysOnly(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, Calendar holidayCalendar);

  protected LocalDate[] getReversedDates(final List<LocalDate> dates) {
    final LocalDate[] result = new LocalDate[dates.size()];
    final ReverseListIterator iterator = new ReverseListIterator(dates);
    int i = 0;
    while (iterator.hasNext()) {
      result[i++] = (LocalDate) iterator.next();
    }
    return result;
  }
}
