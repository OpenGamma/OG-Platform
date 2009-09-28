/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

/**
 * Definition for the 30/360 day count convention. The day count fraction is
 * defined as:<br>
 * <i>fraction = 360(Y<sub>2</sub> - Y<sub>1</sub> + 30(M<sub>2</sub> -
 * M<sub>1</sub> + (D<sub>2</sub> - D<sub>1</sub> / 360</i><br>
 * where:<br>
 * <i>Y<sub>1</sub></i> is the year in which the first day of the period falls;<br>
 * <i>Y<sub>2</sub></i> is the year in which the day immediately following the
 * last day of the period falls;<br>
 * <i>M<sub>1</sub></i> is the month in which the first day of the period falls;<br>
 * <i>M<sub>2</sub></i> is the year in which the day immediately following the
 * last day of the period falls;<br>
 * <i>D<sub>1</sub></i> is the day in which the first day of the period falls,
 * unless the number would be 31, in which case it will be 30; and<br>
 * <i>D<sub>2</sub></i> is the year in which the day immediately following the
 * last day of the period falls, unless the number would be 31 and D<sub>1</sub>
 * is greater than 29.<br>
 * <p>
 * This convention is also known as "360/360" or "Bond Basis".
 * 
 * @author emcleod
 */

public class ThirtyThreeSixtyDayCount implements DayCount {

  @Override
  public double getBasis(final ZonedDateTime date) {
    return 360;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    final int firstYear = firstDate.getYear();
    final int secondYear = secondDate.getYear();
    final int firstMonth = firstDate.toMonthOfYear().getValue();
    final int secondMonth = secondDate.toMonthOfYear().getValue();
    int firstDay = firstDate.getDayOfMonth();
    int secondDay = secondDate.getDayOfMonth();
    firstDay = firstDay == 31 ? 30 : firstDay;
    secondDay = secondDay == 31 && firstDay > 29 ? 30 : secondDay;
    return (360 * (secondYear - firstYear) + 30 * (secondMonth - firstMonth) + secondDay - firstDay) / 360.;
  }
}
