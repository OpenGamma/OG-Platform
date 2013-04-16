/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class EndOfMonthSemiAnnualScheduleCalculator extends Schedule {

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate, fromEnd);
  }

  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd) {
    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(endDate, "end date");
    final LocalDate[] monthly = ScheduleCalculatorFactory.END_OF_MONTH_CALCULATOR.getSchedule(startDate, endDate);
    final List<LocalDate> result = new ArrayList<>();
    if (fromEnd) {
      for (int i = monthly.length - 1; i >= 0; i -= 6) {
        result.add(monthly[i]);
      }
      Collections.reverse(result);
      return result.toArray(EMPTY_LOCAL_DATE_ARRAY);
    }
    for (int i = 0; i < monthly.length; i += 6) {
      result.add(monthly[i]);
    }
    return result.toArray(EMPTY_LOCAL_DATE_ARRAY);
  }

  @Override
  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate, fromEnd);
  }

  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final boolean fromEnd) {
    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(endDate, "end date");
    final ZonedDateTime[] monthly = ScheduleCalculatorFactory.END_OF_MONTH_CALCULATOR.getSchedule(startDate, endDate);
    final List<ZonedDateTime> result = new ArrayList<>();
    if (fromEnd) {
      for (int i = monthly.length - 1; i >= 0; i -= 6) {
        result.add(monthly[i]);
      }
      Collections.reverse(result);
      return result.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
    }
    for (int i = 0; i < monthly.length; i += 6) {
      result.add(monthly[i]);
    }
    return result.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
  }
}
