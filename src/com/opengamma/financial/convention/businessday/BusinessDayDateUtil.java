/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

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
    int result = includeStart ? 1 : 0;
    while (!convention.adjustDate(startDate.toLocalDate()).equals(endDate.toLocalDate())) {
      result++;
    }
    return includeEnd ? result + 1 : result;
  }
}
