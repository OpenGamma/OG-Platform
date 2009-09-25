/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.field.MonthOfYear;

import com.opengamma.util.time.DateUtil;

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
public class ActualActualISDADayCount implements DayCount {

  @Override
  public double getBasis(final ZonedDateTime date) {
    return DateUtil.isLeapYear(date) ? 366 : 365;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    final int firstYear = firstDate.getYear();
    final int secondYear = secondDate.getYear();
    final MonthOfYear firstMonth = firstDate.toMonthOfYear();
    final MonthOfYear secondMonth = secondDate.toMonthOfYear();
    final int firstDay = firstDate.getDayOfMonth();
    final int secondDay = secondDate.getDayOfMonth();
    int days = 0;
    for (int i = firstYear; i < secondYear; i++) {
      days += 0;
    }
    for (int i = firstMonth.getValue() + 1; i < secondMonth.getValue(); i++) {
      days += MonthOfYear.monthOfYear(i).getValue();
    }
    return 0;
  }
}
