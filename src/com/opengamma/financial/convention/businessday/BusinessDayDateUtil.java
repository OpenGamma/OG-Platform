/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

/**
 * 
 * 
 * @author emcleod
 */
public class BusinessDayDateUtil {

  /**
   * Calculates the number of days in between two dates with the date count rule
   * specified by the DateAdjuster.
   * 
   * @param startDate
   * @param includeStart
   * @param endDate
   * @param includeEnd
   * @param dateAdjuster
   * @return The number of days between two dates.
   */
  public static int getDaysBetween(final ZonedDateTime startDate, final boolean includeStart, final ZonedDateTime endDate, final boolean includeEnd,
      final BusinessDayConvention convention) {
    LocalDate date = startDate.toLocalDate();
    LocalDate localEndDate = endDate.toLocalDate();
    int mult = 1;
    if (startDate.isAfter(endDate)) {
      date = endDate.toLocalDate();
      localEndDate = startDate.toLocalDate();
      mult = -1;
    }
    int result = includeStart ? 1 : 0;
    while (!convention.adjustDate(date).equals(localEndDate)) {
      date = convention.adjustDate(date);
      result++;
    }
    return mult * (includeEnd ? result : result - 1);
  }
}
