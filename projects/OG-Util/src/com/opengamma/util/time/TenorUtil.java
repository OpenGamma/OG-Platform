/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.calendar.ZonedDateTime;

/**
 * Utilities for working with tenors.
 */
public class TenorUtil {

  /**
   * Gets the number of days in the tenor.
   * This method assumes 24-hour days.
   * @param tenor  the tenor, not null
   * @return the number of days
   */
  public static double getDaysInTenor(final Tenor tenor) {
    return tenor.getPeriod().totalHoursWith24HourDays() / 24d;
  }

  /**
   * Adjusts a date-time with a tenor.
   * @param dateTime  the date-time to adjust, not null
   * @param tenor  the tenor, not null
   * @return the adjusted date-time
   */
  public static ZonedDateTime getDateWithTenorOffset(final ZonedDateTime dateTime, final Tenor tenor) {
    return dateTime.minus(tenor.getPeriod());
  }

  /**
   * Gets the fraction representing the the number of days in the first tenor
   * divided by the number of days in the second tenor.
   * @param first  the first tenor, not null
   * @param second  the second tenor, not null
   * @return the number of the second tenor in the first
   */
  public static double getTenorsInTenor(final Tenor first, final Tenor second) {
    return TenorUtil.getDaysInTenor(first) / TenorUtil.getDaysInTenor(second);
  }

}
