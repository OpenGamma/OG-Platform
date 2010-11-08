/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Set;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import com.google.common.collect.Sets;

class NextExpiryAdjuster implements DateAdjuster {
  private static final DateAdjuster s_thirdWedAdjuster = DateAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);
  private static final DateAdjuster s_nextQuarterAdjuster = new NextQuarterAdjuster();
  
  private final Set<MonthOfYear> _futureQuarters = Sets.newHashSet(MonthOfYear.MARCH, MonthOfYear.JUNE, MonthOfYear.SEPTEMBER, MonthOfYear.DECEMBER);
  @Override
  public LocalDate adjustDate(final LocalDate date) {
    if (_futureQuarters.contains(date.getMonthOfYear()) &&
        date.with(s_thirdWedAdjuster).isAfter(date)) { // in a quarter
      return date.with(s_thirdWedAdjuster);
    } else {
      return date.with(s_nextQuarterAdjuster).with(s_thirdWedAdjuster);
    }
  }
}
