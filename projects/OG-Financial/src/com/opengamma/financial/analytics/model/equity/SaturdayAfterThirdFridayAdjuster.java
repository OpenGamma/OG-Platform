/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

/**
 * {@code DatAdjuster} that finds the Saturday immediately following the 3rd Friday of the date's month.
 * This may be before or after the date itself.
 */
public class SaturdayAfterThirdFridayAdjuster implements DateAdjuster {
  private static final DateAdjuster s_thirdFriday = DateAdjusters.dayOfWeekInMonth(3, DayOfWeek.FRIDAY);
  @Override
  public LocalDate adjustDate(final LocalDate date) {
    return date.with(s_thirdFriday).plusDays(1);
  }
}
