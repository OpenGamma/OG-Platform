/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.EnumSet;
import java.util.Set;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

/**
 * A {@code DateAdjuster} that moves the date to the next March/June/September/December.
 */
public class NextQuarterAdjuster implements DateAdjuster {

  /**
   * The expiry months.
   */
  private final Set<MonthOfYear> _futureQuarters = EnumSet.of(MonthOfYear.MARCH, MonthOfYear.JUNE, MonthOfYear.SEPTEMBER, MonthOfYear.DECEMBER);

  @Override
  public LocalDate adjustDate(LocalDate date) {
    LocalDate result = date;
    do {
      result = result.plusMonths(1);
    } while (!_futureQuarters.contains(result.getMonthOfYear()));
    return result;
  }

}
