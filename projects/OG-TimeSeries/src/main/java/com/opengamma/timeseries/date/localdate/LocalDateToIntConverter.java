/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import org.threeten.bp.LocalDate;

/**
 * An encoder between {@code LocalDate} and {@code int}.
 * <p>
 * The {@linkplain LocalDate#MAX maximum date} is converted to {@code Integer.MAX_VALUE}.
 * The {@linkplain LocalDate#MIN minimum date} is converted to {@code Integer.MIN_VALUE}.
 * Other values are encoded by multiplying the year by 10,000 and the month by 100.
 * Thus the date 2012-06-30 will be converted to the number 20,120,630.
 * Any date with a year outside the range 0000 to 9999 throws an exception.
 */
public final class LocalDateToIntConverter {

  /**
   * Restricted constructor.
   */
  private LocalDateToIntConverter() {
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a {@code LocalDate} to an {@code int}.
   * <p>
   * See the class Javadoc for the format of the {@code int}.
   * 
   * @param date  the date to convert, not null
   * @return the {@code int} equivalent
   * @throws IllegalArgumentException if the date is too large
   */
  public static int convertToInt(LocalDate date) {
    int year = date.getYear();
    int month = date.getMonthValue();
    int day = date.getDayOfMonth();
    if (year > 9999) {
      if (date.equals(LocalDate.MAX)) {
        return Integer.MAX_VALUE;
      }
      throw new IllegalArgumentException("LocalDate has year > 9999");
    }
    if (year < 0) {
      if (date.equals(LocalDate.MIN)) {
        return Integer.MIN_VALUE;
      }
      throw new IllegalArgumentException("LocalDate has year < 0");
    }
    return year * 10000 + month * 100 + day;
  }

  /**
   * Converts an {@code int} to an {@code LocalDate}.
   * <p>
   * See the class Javadoc for the format of the {@code int}.
   * 
   * @param date  the {@code int} date to convert
   * @return the {@code LocalDate} equivalent, not null
   */
  public static LocalDate convertToLocalDate(int date) {
    if (date == Integer.MAX_VALUE) {
      return LocalDate.MAX;
    }
    if (date == Integer.MIN_VALUE) {
      return LocalDate.MIN;
    }
    int year = date / 10000;
    int month = (date / 100) % 100;
    int day = date % 100;
    return LocalDate.of(year, month, day);
  }

  /**
   * Checks an {@code int} date is valid.
   * <p>
   * See the class Javadoc for the format of the {@code int}.
   * 
   * @param date  the {@code int} date to check
   * @throws IllegalArgumentException if the date is invalid
   */
  public static void checkValid(int date) {
    int year = date / 10000;
    int month = (date / 100) % 100;
    int day = date % 100;
    if (year < 0 && date != Integer.MIN_VALUE) {
      throw new IllegalArgumentException("Date must be year zero or later");
    }
    if (year > 9999 && date != Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Date must be year 9999 or earlier");
    }
    if (month < 1 || month > 12 || day < 1 || day > 31) {
      throw new IllegalArgumentException("Invalid month-day");
    }
    if (day > 29 && VALID_MONTH_DAY[(month - 1) * 2 + (day - 30)] == false) {
      throw new IllegalArgumentException("Invalid month-day");
    }
  }

  private static final boolean[] VALID_MONTH_DAY = {
    true, true,   // Jan
    false, false, // Feb
    true, true,   // Mar
    true, false,  // Apr
    true, true,   // May
    true, false,  // Jun
    true, true,   // Jul
    true, true,   // Aug
    true, false,  // Sep
    true, true,   // Oct
    true, false,  // Nov
    true, true,   // Dec
  };

}
