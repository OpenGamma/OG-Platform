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
 * Definition for the 30E/360 (ISDA) day count convention. The day count
 * fraction is defined as:<br>
 * <i>fraction = 360(Y<sub>2</sub> - Y<sub>1</sub> + 30(M<sub>2</sub> -
 * M<sub>1</sub> + (D<sub>2</sub> - D<sub>1</sub> / 360</i><br>
 * where:<br>
 * <i>Y<sub>1</sub></i> is the year in which the first day of the period falls;<br>
 * <i>Y<sub>2</sub></i> is the year in which the day immediately following the
 * last day of the period falls;<br>
 * <i>M<sub>1</sub></i> is the month in which the first day of the period falls;<br>
 * <i>M<sub>2</sub></i> is the year in which the day immediately following the
 * last day of the period falls;<br>
 * <i>D<sub>1</sub></i> is the day in which the first day of the period falls
 * unless (i) the day number is 31 or (ii) it is the last day of February, in
 * which case it is adjusted to 30; and<br>
 * <i>D<sub>2</sub></i> is the year in which the day immediately following the
 * last day of the period falls unless (i) the day number is 31 or (ii) it is
 * the last day of February, in which case it is adjusted to 30.<br>
 * 
 * @author emcleod
 */
public class ThirtyEThreeSixtyISDADayCount implements DayCount {

  @Override
  public double getBasis(final ZonedDateTime date) {
    return 360;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    final int firstYear = firstDate.getYear();
    final int secondYear = secondDate.getYear();
    final MonthOfYear firstMonth = firstDate.toMonthOfYear();
    final MonthOfYear secondMonth = secondDate.toMonthOfYear();
    final int firstDay = getAdjustedDayNumber(firstDate);
    final int secondDay = getAdjustedDayNumber(secondDate);
    return (360 * (secondYear - firstYear) + 30 * (secondMonth.getValue() - firstMonth.getValue()) + secondDay - firstDay) / 360.;
  }

  private int getAdjustedDayNumber(final ZonedDateTime date) {
    final MonthOfYear month = date.toMonthOfYear();
    int day = date.getDayOfMonth();
    if (day == 31) {
      day = 30;
    } else if (month == MonthOfYear.FEBRUARY) {
      if (DateUtil.isLeapYear(date)) {
        day = day == 29 ? 30 : day;
      } else {
        day = day == 28 ? 30 : day;
      }
    }
    return day;
  }
  
  @Override
  public String getConventionName () {
    return "30E/360 (ISDA)";
  }

}
