/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.EnumSet;
import java.util.Set;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

/**
 * {@code DatAdjuster} that finds the next Expiry in Interest Rate Futures Options. 
 * This is the 3rd Wednesday of the next IMM Future Expiry Month.
 */
public class NextExpiryAdjuster implements DateAdjuster {

  /** An adjuster finding the 3rd Wednesday in a month. May be before or after date */
  private static final DateAdjuster s_dayOfMonth = DateAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);

  /** An adjuster moving to the next quarter. */
  private static final DateAdjuster s_nextQuarterAdjuster = new NextQuarterAdjuster();

  /** The IMM Expiry months  */
  private final Set<MonthOfYear> _futureQuarters = EnumSet.of(MonthOfYear.MARCH, MonthOfYear.JUNE, MonthOfYear.SEPTEMBER, MonthOfYear.DECEMBER);

  @Override
  public LocalDate adjustDate(final LocalDate date) {
    if (_futureQuarters.contains(date.getMonthOfYear()) &&
        date.with(s_dayOfMonth).isAfter(date)) { // in a quarter
      return date.with(s_dayOfMonth);
    } else {
      return date.with(s_nextQuarterAdjuster).with(s_dayOfMonth);
    }
  }

}
