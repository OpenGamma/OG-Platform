/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple utility to calculate the number of business days between two dates
 */
public abstract class BusinessDays {

  /**
   * Get the number of business days between two dates
   * @param firstDate The first date
   * @param secondDate the second date
   * @param calendar Calendar defining what is a working day
   * @return The number of business (working) days between two dates
   */
  public static int getDaysBetween(LocalDate firstDate, LocalDate secondDate, Calendar calendar) {
    ArgumentChecker.notNull(firstDate, "first date");
    ArgumentChecker.notNull(secondDate, "second date");
    if (!(secondDate.isAfter(firstDate) || secondDate.equals(firstDate))) {
      throw new OpenGammaRuntimeException("d2 must be on or after d1: have d1 = " + firstDate + " and d2 = " + secondDate);
    }
    ArgumentChecker.notNull(calendar, "calendar");
    LocalDate date = firstDate;
    while (!calendar.isWorkingDay(date)) {
      date = date.plusDays(1);
    }
    date = date.plusDays(1);
    int count = 0;
    while (!date.isAfter(secondDate)) {
      if (calendar.isWorkingDay(date)) {
        count++;
      }
      date = date.plusDays(1);
    }
    return count;
  }

  /**
   * Get the number of business days between two dates. <b>Note:<b> these {@link ZonedDateTime} dates are converted to {@link LocalDate}, so any time-of-day and time zone information is lost
   * @param firstDate The first date
   * @param secondDate the second date
   * @param calendar Calendar defining what is a working day
   * @return The number of business (working) days between two dates
   */
  public static int getDaysBetween(final ZonedDateTime firstDate, final ZonedDateTime secondDate, final Calendar calendar) {
    ArgumentChecker.notNull(firstDate, "first date");
    ArgumentChecker.notNull(secondDate, "second date");
    return getDaysBetween(firstDate.toLocalDate(), secondDate.toLocalDate(), calendar);
  }

}
