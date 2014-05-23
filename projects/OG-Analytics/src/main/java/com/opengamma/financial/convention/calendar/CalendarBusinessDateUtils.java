/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.calendar;

import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;

/**
 * Utilities related to Calendar. In particular to compute the next or n-th (non-)good business date from a starting point.
 */
public class CalendarBusinessDateUtils {

  /**
   * Returns the next good business day in the calendar after a starting point. If the starting date itself is a good business day, the starting date is returned.
   * @param startingDate The stating date for the search.
   * @param calendar The calendar.
   * @return The next good business date.
   */
  public static LocalDate nextGoodBusinessDate(final LocalDate startingDate, final Calendar calendar) {
    LocalDate currentDate = startingDate;
    while (!calendar.isWorkingDay(currentDate)) {
      currentDate = currentDate.plusDays(1);
    }
    return currentDate;
  }

  /**
   * Returns the next non-good business day in the calendar after a starting point. 
   * If the starting date itself is a non-good business day, the starting date is returned.
   * @param startingDate The stating date for the search.
   * @param calendar The calendar.
   * @return The next non-good business date.
   */
  public static LocalDate nextNonGoodBusinessDate(final LocalDate startingDate, final Calendar calendar) {
    LocalDate currentDate = startingDate;
    while (calendar.isWorkingDay(currentDate)) {
      currentDate = currentDate.plusDays(1);
    }
    return currentDate;
  }

  /**
   * Compute the nth good business date for a given calendar.
   * @param startingDate The starting date. The date itself is included in the roll period, 
   * i.e. if the starting date is in the good business date, the first date is the starting date.
   * @param calendar The calendar.
   * @param numberDate The number of times the date should be rolled.
   * @return The n-th good business date.
   */
  public static LocalDate nthGoodBusinessDate(final LocalDate startingDate, final Calendar calendar, final int numberDate) {
    ArgumentChecker.isTrue(numberDate >= 1, "At least one roll date");
    LocalDate nthDate = nextGoodBusinessDate(startingDate, calendar);
    for (int loopNumber = 1; loopNumber < numberDate; loopNumber++) {
      nthDate = nextGoodBusinessDate(nthDate.plusDays(1), calendar);
    }
    return nthDate;
  }

  /**
   * Compute the nth non-good business date for a given calendar.
   * @param startingDate The starting date. The date itself is included in the roll period, 
   * i.e. if the starting date is in the non-good business date, the first date is the starting date.
   * @param calendar The calendar.
   * @param numberDate The number of times the date should be rolled.
   * @return The n-th non-good business date.
   */
  public static LocalDate nthNonGoodBusinessDate(final LocalDate startingDate, final Calendar calendar, final int numberDate) {
    ArgumentChecker.isTrue(numberDate >= 1, "At least one roll date");
    LocalDate nthDate = nextNonGoodBusinessDate(startingDate, calendar);
    for (int loopNumber = 1; loopNumber < numberDate; loopNumber++) {
      nthDate = nextNonGoodBusinessDate(nthDate.plusDays(1), calendar);
    }
    return nthDate;
  }

}
