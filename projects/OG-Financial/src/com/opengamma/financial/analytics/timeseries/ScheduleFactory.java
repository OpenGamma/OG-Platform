/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;

/**
 * 
 */
public class ScheduleFactory {

  public static LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final Frequency frequency, final boolean fromEnd) {
    return getSchedule(startDate, endDate, frequency, false, fromEnd);
  }

  public static LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final Frequency frequency, final boolean endOfMonth, final boolean fromEnd) {
    SimpleFrequency simple;
    if (frequency instanceof SimpleFrequency) {
      simple = (SimpleFrequency) frequency;
    } else if (frequency instanceof PeriodFrequency) {
      simple = ((PeriodFrequency) frequency).toSimpleFrequency();
    } else {
      throw new IllegalArgumentException("Can only handle SimpleFrequency and PeriodFrequency");
    }
    final int periodsPerYear = (int) simple.getPeriodsPerYear();
    return getSchedule(startDate, endDate, periodsPerYear, endOfMonth);
  }

  public static LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final int periodsPerYear, final boolean fromEnd) {
    return getSchedule(startDate, endDate, periodsPerYear, false, fromEnd);
  }

  public static LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final int periodsPerYear, final boolean endOfMonth, final boolean fromEnd) {
    final boolean generateRecursive = endOfMonth ? false : true;
    if (periodsPerYear == 1) {
      return ScheduleCalculatorFactory.ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    if (periodsPerYear == 2) {
      return ScheduleCalculatorFactory.SEMI_ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    if (periodsPerYear == 4) {
      return ScheduleCalculatorFactory.QUARTERLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    if (periodsPerYear == 12) {
      return ScheduleCalculatorFactory.MONTHLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    if (periodsPerYear == 52) {
      return ScheduleCalculatorFactory.WEEKLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    throw new IllegalArgumentException("Can only get annual, semi-annual, quarterly, montly and weekly schedules");
  }

  public static ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Frequency frequency, final boolean fromEnd) {
    return getSchedule(startDate, endDate, frequency, false, fromEnd);
  }

  public static ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final Frequency frequency, final boolean endOfMonth, final boolean fromEnd) {
    SimpleFrequency simple;
    if (frequency instanceof SimpleFrequency) {
      simple = (SimpleFrequency) frequency;
    } else if (frequency instanceof PeriodFrequency) {
      simple = ((PeriodFrequency) frequency).toSimpleFrequency();
    } else {
      throw new IllegalArgumentException("Can only handle SimpleFrequency and PeriodFrequency");
    }
    final int periodsPerYear = (int) simple.getPeriodsPerYear();
    return getSchedule(startDate, endDate, periodsPerYear, endOfMonth);
  }

  public static ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final int periodsPerYear, final boolean fromEnd) {
    return getSchedule(startDate, endDate, periodsPerYear, false, fromEnd);
  }

  public static ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final int periodsPerYear, final boolean endOfMonth, final boolean fromEnd) {
    final boolean generateRecursive = endOfMonth ? false : true;
    if (periodsPerYear == 1) {
      return ScheduleCalculatorFactory.ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    if (periodsPerYear == 2) {
      return ScheduleCalculatorFactory.SEMI_ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    if (periodsPerYear == 4) {
      return ScheduleCalculatorFactory.QUARTERLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    if (periodsPerYear == 12) {
      return ScheduleCalculatorFactory.MONTHLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    if (periodsPerYear == 52) {
      return ScheduleCalculatorFactory.WEEKLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    throw new IllegalArgumentException("Can only get annual, semi-annual, quarterly, montly and weekly schedules");
  }
}
