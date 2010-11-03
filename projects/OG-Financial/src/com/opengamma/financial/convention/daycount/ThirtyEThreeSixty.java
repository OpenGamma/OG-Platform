/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 */
public class ThirtyEThreeSixty extends ThirtyThreeSixtyTypeDayCount {

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    testDates(firstDate, secondDate);
    double d1 = firstDate.getDayOfMonth();
    double d2 = secondDate.getDayOfMonth();
    final double m1 = firstDate.getMonthOfYear().getValue();
    final double m2 = secondDate.getMonthOfYear().getValue();
    final double y1 = firstDate.getYear();
    final double y2 = secondDate.getYear();
    if (d1 == 31) {
      d1 = 30;
    }
    if (d2 == 31) {
      d2 = 30;
    }
    return getYears(d1, d2, m1, m2, y1, y2);
  }

  @Override
  public String getConventionName() {
    return "30E/360";
  }

}
