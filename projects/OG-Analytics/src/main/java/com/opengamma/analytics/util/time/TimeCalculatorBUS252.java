/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.util.time;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;

/**
 *  Converts dates to 'Analytics Time'. The latter are stored as doubles,
 * and typically represent the fraction of years between some date and the current one.
 */
public final class TimeCalculatorBUS252 {
  /**
   * The day count used to convert to time.
   */
  private static final DayCount MODEL_DAYCOUNT;

  static {
    /*
     * Initialise MODEL_DAYCOUNT to BUSINESS_252
     */
    MODEL_DAYCOUNT = DayCounts.BUSINESS_252;
  }

  private TimeCalculatorBUS252() {
  }

  /**
   * Computes the time between two dates using a user-supplied day count convention. Dates can be in any order.
   * If date1 is after date2, the result will be negative.
   * @param date1 The first date.
   * @param date2 The second date.
   * @param dayCount The day count.
   * @param calendar the calendar.
   * @return The time.
   */
  public static double getTimeBetween(final ZonedDateTime date1, final ZonedDateTime date2, final DayCount dayCount, final Calendar calendar) {
    ArgumentChecker.notNull(date1, "date1");
    ArgumentChecker.notNull(date1, "date2");
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(calendar, "calendar");
    // Implementation note: here we convert date2 to the same zone as date1 so we don't accidentally gain or lose a day.
    final ZonedDateTime rebasedDate2 = date2.withZoneSameInstant(date1.getZone());

    final boolean timeIsNegative = date1.isAfter(rebasedDate2); // date1 >= date2

    if (!timeIsNegative) {
      return dayCount.getDayCountFraction(date1, rebasedDate2, calendar);
    }
    return -1.0 * dayCount.getDayCountFraction(rebasedDate2, date1, calendar);
  }

  /**
   * Computes the time between two dates using a user-supplied day count convention. Dates can be in any order.
   * If date1 is after date2, the result will be negative.
   * @param date1 The first date.
   * @param date2 The second date.
   * @param dayCount The day count.
   * @return The time.
   */
  public static double getTimeBetween(final ZonedDateTime date1, final ZonedDateTime date2, final DayCount dayCount) {
    ArgumentChecker.notNull(date1, "date1");
    ArgumentChecker.notNull(date1, "date2");
    ArgumentChecker.notNull(dayCount, "day count");
    // Implementation note: here we convert date2 to the same zone as date1 so we don't accidentally gain or lose a day.
    final ZonedDateTime rebasedDate2 = date2.withZoneSameInstant(date1.getZone());

    final boolean timeIsNegative = date1.isAfter(rebasedDate2); // date1 >= date2

    if (!timeIsNegative) {
      return dayCount.getDayCountFraction(date1, rebasedDate2);
    }
    return -1.0 * dayCount.getDayCountFraction(rebasedDate2, date1);
  }

  /**
   * Computes the time between two dates. Dates can be in any order. If date1 is after date2, the result will be negative.
   * @param date1 The first date.
   * @param date2 The second date.
   * @param calendar the calendar.
   * @return The time.
   */
  public static double getTimeBetween(final ZonedDateTime date1, final ZonedDateTime date2, final Calendar calendar) {
    return getTimeBetween(date1, date2, MODEL_DAYCOUNT, calendar);
  }

  /**
   * Computes the time between two arrays of dates.
   * @param date1 The first dates array.
   * @param date2 The second dates array.
   * @param calendar the calendar.
   * @return The times.
   */
  public static double[] getTimeBetween(final ZonedDateTime[] date1, final ZonedDateTime[] date2, final Calendar calendar) {
    ArgumentChecker.notNull(date1, "First date");
    ArgumentChecker.notNull(date2, "Second date");
    ArgumentChecker.notNull(calendar, "calendar");
    final int nbDates = date1.length;
    ArgumentChecker.isTrue(nbDates == date2.length, "Number of dates should be equal");
    final double[] result = new double[nbDates];
    for (int loopdate = 0; loopdate < nbDates; loopdate++) {
      result[loopdate] = getTimeBetween(date1[loopdate], date2[loopdate], calendar);
    }
    return result;
  }

  /**
   * Computes the time between a given date and an array of dates. The same first date is used for all computations.
   * @param date1 The first date.
   * @param date2 The second dates array.
   * @param calendar the calendar.
   * @return The times.
   */
  public static double[] getTimeBetween(final ZonedDateTime date1, final ZonedDateTime[] date2, final Calendar calendar) {
    ArgumentChecker.notNull(date1, "First date");
    ArgumentChecker.notNull(date2, "Second date");
    final int nbDates = date2.length;
    final double[] result = new double[nbDates];
    for (int loopdate = 0; loopdate < nbDates; loopdate++) {
      result[loopdate] = getTimeBetween(date1, date2[loopdate], calendar);
    }
    return result;
  }

  public static double getTimeBetween(final LocalDate date1, final LocalDate date2, final Calendar calendar) {
    ArgumentChecker.notNull(date1, "date1");
    ArgumentChecker.notNull(date2, "date2");
    return getTimeBetween(date1.atStartOfDay(ZoneOffset.UTC), date2.atStartOfDay(ZoneOffset.UTC), calendar);
  }

  public static double getTimeBetween(final ZonedDateTime zdt1, final LocalDate date2, final Calendar calendar) {
    ArgumentChecker.notNull(zdt1, "date1");
    ArgumentChecker.notNull(date2, "date2");
    final ZonedDateTime zdt2 = date2.atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime rebasedZdt1 = zdt1.withZoneSameInstant(ZoneOffset.UTC);
    return getTimeBetween(rebasedZdt1, zdt2, calendar);
  }

  public static double getTimeBetween(final LocalDate date1, final ZonedDateTime zdt2, final Calendar calendar) {
    ArgumentChecker.notNull(date1, "date1");
    ArgumentChecker.notNull(zdt2, "date2");
    final ZonedDateTime zdt1 = date1.atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime rebasedZdt2 = zdt2.withZoneSameInstant(ZoneOffset.UTC);
    return getTimeBetween(zdt1, rebasedZdt2, calendar);
  }

}
