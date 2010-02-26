/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.FirstDateOfYearAdjuster;

/**
 * Definition for the Actual/Actual day count convention. The day count fraction
 * is the actual number of days in the period divided by 365 or, if any portion
 * of the period falls in a leap year, the sum of the actual number of days that
 * fall in the leap year divided by 366 and the actual number of days that fall
 * in the non-leap year.
 * <p>
 * This convention is also known as "Actual/Actual", "Act/Act" or
 * "Act/Act (ISDA)".
 * 
 * @author emcleod
 */
public class ActualActualISDADayCount extends StatelessDayCount {
  private static final DateAdjuster FIRST_DAY_OF_YEAR = new FirstDateOfYearAdjuster();

  @Override
  public double getBasis(final ZonedDateTime date) {
    return DateUtil.isLeapYear(date) ? 366 : 365;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    if (firstDate.getYear() == secondDate.getYear()) {
      return DateUtil.getDaysBetween(firstDate, false, secondDate, true) / getBasis(firstDate);
    }
    if (DateUtil.isLeapYear(firstDate) || DateUtil.isLeapYear(secondDate)) {
      final ZonedDateTime lastDayOfFirstYear = ZonedDateTime.dateTime(DateAdjusters.lastDayOfYear().adjustDate(firstDate.toLocalDate()), firstDate.toLocalTime(), firstDate
          .getZone());
      final ZonedDateTime firstDayOfSecondYear = ZonedDateTime.dateTime(FIRST_DAY_OF_YEAR.adjustDate(secondDate.toLocalDate()), secondDate.toLocalTime(), secondDate.getZone());
      return (1 + DateUtil.getDaysBetween(firstDate, false, lastDayOfFirstYear, true)) / getBasis(firstDate)
          + DateUtil.getDaysBetween(firstDayOfSecondYear, false, secondDate, true) / getBasis(secondDate);
    }
    return DateUtil.getDaysBetween(firstDate, false, secondDate, true) / getBasis(firstDate);
  }

  @Override
  public String getConventionName () {
    return "Actual/Actual";
  }

}
