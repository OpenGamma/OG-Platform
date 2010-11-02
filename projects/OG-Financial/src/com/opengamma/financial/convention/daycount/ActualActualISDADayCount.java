/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.time.DateUtil;

/**
 * The Actual/Actual day count convention.
 * <p>
 * The day count fraction is the actual number of days in the period divided by 365
 * or, if any portion of the period falls in a leap year, the sum of the actual number
 * of days that fall in the leap year divided by 366 and the actual number of days that
 * fall in the non-leap year.
 * <p>
 * This convention is also known as "Actual/Actual", "Act/Act" or "Act/Act (ISDA)".
 */
public class ActualActualISDADayCount extends StatelessDayCount {

  @Override
  public double getBasis(final ZonedDateTime date) {
    return DateUtil.isLeapYear(date) ? 366 : 365;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime dateTime1, final ZonedDateTime dateTime2) {
    final int year1 = dateTime1.getYear();
    final int year2 = dateTime2.getYear();
    if (year1 == year2) {
      return DateUtil.getExactDaysBetween(dateTime1, dateTime2) / getBasis(dateTime1);
    }
    ZonedDateTime endOfYear1 = LocalDateTime.of(year1 + 1, 1, 1, 0, 0).atZone(dateTime1.getZone());
    ZonedDateTime startOfYear2 = LocalDateTime.of(year2, 1, 1, 0, 0).atZone(dateTime2.getZone());
    
    return (double) DateUtil.getExactDaysBetween(dateTime1, endOfYear1) / getBasis(dateTime1)
        + (double) DateUtil.getExactDaysBetween(startOfYear2, dateTime2) / getBasis(dateTime2) + (double) (year2 - year1 - 1);
  }

  @Override
  public String getConventionName() {
    return "Actual/Actual";
  }

}
