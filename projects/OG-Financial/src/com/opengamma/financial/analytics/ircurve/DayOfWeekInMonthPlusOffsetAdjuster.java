/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

/**
 *
 */
public class DayOfWeekInMonthPlusOffsetAdjuster implements DateAdjuster {

  private final int _week;
  private final DayOfWeek _day;
  private final int _offset;

  public DayOfWeekInMonthPlusOffsetAdjuster(int week, DayOfWeek day, int offset) {
    _week = week;
    _day = day;
    _offset = offset;
  }

  @Override
  public LocalDate adjustDate(LocalDate date) {

    final DateAdjuster unadjustedDayInMonth = DateAdjusters.dayOfWeekInMonth(_week, _day);
    return date.with(unadjustedDayInMonth).plusDays(_offset);
  }
}
