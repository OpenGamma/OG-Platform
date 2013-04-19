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
   * Converts a {@code int} to an {@code LocalDate}.
   * <p>
   * See the class Javadoc for the format of the {@code int}.
   * 
   * @param date  the {@code int} date to convert, not null
   * @return the {@code LocalDate} equivalent, not null
   */
  public static LocalDate convertToLocalDate(int date) {
    if (date == Integer.MAX_VALUE) {
      return LocalDate.MAX;
    }
    if (date == Integer.MIN_VALUE) {
      return LocalDate.MIN;
    }
    return LocalDate.of(date / 10000, (date / 100) % 100, date % 100);
  }

}
