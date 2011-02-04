/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.schedule;

import java.lang.reflect.Array;
import java.util.List;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.collections.iterators.ReverseListIterator;

/**
 * 
 */
public abstract class Schedule {
  /** Empty array of LocalDate */
  protected static final LocalDate[] EMPTY_LOCAL_DATE_ARRAY = new LocalDate[0];
  /** Empty array of ZonedDateTime */
  protected static final ZonedDateTime[] EMPTY_ZONED_DATE_TIME_ARRAY = new ZonedDateTime[0];

  public abstract LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final boolean generateRecursive);

  public abstract ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final boolean fromEnd, final boolean generateRecursive);

  @SuppressWarnings("unchecked")
  protected <T> T[] getReversedDates(final List<T> dates) {
    if (dates.isEmpty()) {
      throw new IllegalArgumentException("List of dates was empty");
    }
    final Class<?> clazz = dates.get(0).getClass();
    final T[] result = (T[]) Array.newInstance(clazz, dates.size());
    final ReverseListIterator iterator = new ReverseListIterator(dates);
    int i = 0;
    while (iterator.hasNext()) {
      result[i++] = (T) iterator.next();
    }
    return result;
  }
}
