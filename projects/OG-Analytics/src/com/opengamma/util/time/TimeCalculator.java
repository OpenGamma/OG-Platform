/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

import javax.time.calendar.ZonedDateTime;

/**
 * Converts dates to 'Analytics Time'. The latter are stored as doubles, 
 * and typically represent the fraction of years between some date and the current one.
 */
public abstract class TimeCalculator {
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");

  public static double getTimeBetween(final ZonedDateTime date1, final ZonedDateTime date2) {
    ArgumentChecker.notNull(date1, "date1");
    ArgumentChecker.notNull(date1, "date2");
    ArgumentChecker.isTrue(date1.getZone().equals(date2.getZone()), "Attempted to compute Analytics-Time between instants in two different TimeZones. " +
        "This is not permitted. ZonedDateTime's are: {} and {}", date1, date2);
    final boolean timeIsNegative = date1.isAfter(date2); // date1 >= date2

    if (!timeIsNegative) {
      final double time = ACT_ACT.getDayCountFraction(date1, date2);
      return time;
    }
    return -1.0 * ACT_ACT.getDayCountFraction(date2, date1);
  }
}
