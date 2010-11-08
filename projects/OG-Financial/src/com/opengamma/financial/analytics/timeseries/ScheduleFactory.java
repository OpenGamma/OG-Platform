/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class ScheduleFactory {

  public static LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final Frequency frequency, final boolean fromEnd) {
    return getSchedule(startDate, endDate, frequency, false, fromEnd);
  }

  public static LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final Frequency frequency, final boolean endOfMonth, final boolean fromEnd) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.notNull(frequency, "frequency");
    SimpleFrequency simple;
    if (frequency instanceof SimpleFrequency) {
      simple = (SimpleFrequency) frequency;
    } else if (frequency instanceof PeriodFrequency) {
      simple = ((PeriodFrequency) frequency).toSimpleFrequency();
    } else {
      throw new IllegalArgumentException("Can only handle SimpleFrequency and PeriodFrequency");
    }
    final int periodsPerYear = (int) simple.getPeriodsPerYear();
    return getSchedule(startDate, endDate, periodsPerYear, endOfMonth, fromEnd);
  }

  public static LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final int periodsPerYear, final boolean fromEnd) {
    return getSchedule(startDate, endDate, periodsPerYear, false, fromEnd);
  }

  public static LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final int periodsPerYear, final boolean endOfMonth, final boolean fromEnd) {
    //TODO generateRecursive as an input
    LocalDate[] result = null;
    if (periodsPerYear == 1) { //TODO EOM
      result = ScheduleCalculatorFactory.ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
    }
    if (periodsPerYear == 2) {
      result = ScheduleCalculatorFactory.SEMI_ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
      if (endOfMonth) {
        if (fromEnd && endDate.getDayOfMonth() == endDate.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(endDate))) {
          result = ScheduleCalculatorFactory.SEMI_ANNUAL_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
        }
      } else if (startDate.getDayOfMonth() == startDate.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(startDate))) {
        result = ScheduleCalculatorFactory.SEMI_ANNUAL_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
      }
    }
    if (periodsPerYear == 4) { //TODO EOM
      result = ScheduleCalculatorFactory.QUARTERLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
    }
    if (periodsPerYear == 12) {
      result = ScheduleCalculatorFactory.MONTHLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
      if (endOfMonth) {
        if (fromEnd && endDate.getDayOfMonth() == endDate.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(endDate))) {
          result = ScheduleCalculatorFactory.END_OF_MONTH_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
        }
      } else if (startDate.getDayOfMonth() == startDate.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(startDate))) {
        result = ScheduleCalculatorFactory.END_OF_MONTH_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
      }
    }
    if (periodsPerYear == 52) {
      result = ScheduleCalculatorFactory.WEEKLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
    }
    if (periodsPerYear == 365 || periodsPerYear == 366) {
      result = ScheduleCalculatorFactory.DAILY_CALCULATOR.getSchedule(startDate, endDate);
    }
    Validate.notNull(result, "schedule");
    if (endOfMonth) {
      if (periodsPerYear == 52) {
        throw new IllegalArgumentException("Could not get EOM adjustment for a weekly frequency");
      }
      if (periodsPerYear == 365 || periodsPerYear == 366) {
        throw new IllegalArgumentException("Could not get EOM adjustment for a daily frequency");
      }
      final int n = result.length;
      if (fromEnd) {
        final LocalDate lastDate = result[n - 1];
        if (lastDate.getDayOfMonth() == lastDate.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(lastDate))) {
          for (int i = 0; i < n - 1; i++) {
            final LocalDate date = result[i];
            result[i] = date.withDayOfMonth(date.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(date)));
          }
        }
      } else {
        final LocalDate firstDate = result[0];
        if (firstDate.getDayOfMonth() == firstDate.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(firstDate))) {
          for (int i = 1; i < n; i++) {
            final LocalDate date = result[i];
            result[i] = date.withDayOfMonth(date.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(date)));
          }
        }
      }
    }
    return result;
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
    return getSchedule(startDate, endDate, periodsPerYear, endOfMonth, fromEnd);
  }

  public static ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final int periodsPerYear, final boolean fromEnd) {
    return getSchedule(startDate, endDate, periodsPerYear, false, fromEnd);
  }

  public static ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final int periodsPerYear, final boolean endOfMonth, final boolean fromEnd) {
    final boolean generateRecursive = endOfMonth ? false : true;
    ZonedDateTime[] result = null;
    if (periodsPerYear == 1) {
      result = ScheduleCalculatorFactory.ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    if (periodsPerYear == 2) {
      result = ScheduleCalculatorFactory.SEMI_ANNUAL_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
      if (endOfMonth) {
        if (fromEnd && endDate.getDayOfMonth() == endDate.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(endDate))) {
          result = ScheduleCalculatorFactory.SEMI_ANNUAL_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
        }
      } else if (startDate.getDayOfMonth() == startDate.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(startDate))) {
        result = ScheduleCalculatorFactory.SEMI_ANNUAL_EOM_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
      }
    }
    if (periodsPerYear == 4) {
      result = ScheduleCalculatorFactory.QUARTERLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    if (periodsPerYear == 12) {
      result = ScheduleCalculatorFactory.MONTHLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
      if (endOfMonth) {
        if (fromEnd && endDate.getDayOfMonth() == endDate.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(endDate))) {
          result = ScheduleCalculatorFactory.END_OF_MONTH_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
        }
      } else if (startDate.getDayOfMonth() == startDate.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(startDate))) {
        result = ScheduleCalculatorFactory.END_OF_MONTH_CALCULATOR.getSchedule(startDate, endDate, fromEnd, false);
      }
    }
    if (periodsPerYear == 52) {
      result = ScheduleCalculatorFactory.WEEKLY_CALCULATOR.getSchedule(startDate, endDate, fromEnd, generateRecursive);
    }
    if (periodsPerYear == 365 || periodsPerYear == 366) {
      result = ScheduleCalculatorFactory.DAILY_CALCULATOR.getSchedule(startDate, endDate);
    }
    Validate.notNull(result, "schedule");
    if (endOfMonth) {
      final int n = result.length;
      if (fromEnd) {
        if (periodsPerYear == 52) {
          throw new IllegalArgumentException("Could not get EOM adjustment for a weekly frequency");
        }
        if (periodsPerYear == 365 || periodsPerYear == 366) {
          throw new IllegalArgumentException("Could not get EOM adjustment for a daily frequency");
        }
        final ZonedDateTime lastDate = result[n - 1];
        if (lastDate.getDayOfMonth() == lastDate.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(lastDate))) {
          for (int i = 0; i < n - 1; i++) {
            final ZonedDateTime date = result[i];
            result[i] = date.withDayOfMonth(date.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(date)));
          }
        }
      } else {
        final ZonedDateTime firstDate = result[0];
        if (firstDate.getDayOfMonth() == firstDate.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(firstDate))) {
          for (int i = 1; i < n; i++) {
            final ZonedDateTime date = result[i];
            result[i] = date.withDayOfMonth(date.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(date)));
          }
        }
      }
    }
    return result;
  }
}
