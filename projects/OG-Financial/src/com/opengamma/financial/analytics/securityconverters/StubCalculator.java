/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.securityconverters;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class StubCalculator {
  /** Stub type enum */
  public enum StubType {
    /** No stub */
    NONE,
    /** Short stub at the start of the schedule */
    SHORT_START,
    /** Long stub at the start of the schedule */
    LONG_START,
    /** Short stub at the end of the schedule */
    SHORT_END,
    /** Long stub at the end of the schedule */
    LONG_END
  }

  public static StubType getStartStubType(final LocalDate[] schedule, final int paymentsPerYear) {
    return getStartStubType(schedule, paymentsPerYear, false);
  }

  public static StubType getStartStubType(final LocalDate[] schedule, final int paymentsPerYear, final boolean isEOM) {
    Validate.notNull(schedule, "schedule");
    Validate.noNullElements(schedule, "schedule");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(12 % paymentsPerYear == 0);
    final int months = 12 / paymentsPerYear;
    final LocalDate first = schedule[0];
    final LocalDate second = schedule[1];
    LocalDate date;
    if (isEOM && second.getDayOfMonth() == second.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(second))) {
      date = second.minusMonths(months);
      date = date.withDayOfMonth(date.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(date)));
    } else {
      date = second.minusMonths(months);
    }
    if (date.equals(first)) {
      return StubType.NONE;
    }
    if (date.isBefore(first)) {
      return StubType.SHORT_START;
    }
    return StubType.LONG_START;
  }

  public static StubType getEndStubType(final LocalDate[] schedule, final int paymentsPerYear) {
    return getEndStubType(schedule, paymentsPerYear, false);
  }

  public static StubType getEndStubType(final LocalDate[] schedule, final int paymentsPerYear, final boolean isEOM) {
    Validate.notNull(schedule, "schedule");
    Validate.noNullElements(schedule, "schedule");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(12 % paymentsPerYear == 0);
    final int months = 12 / paymentsPerYear;
    final int n = schedule.length;
    final LocalDate first = schedule[n - 2];
    final LocalDate second = schedule[n - 1];
    LocalDate date;
    if (isEOM && first.getDayOfMonth() == first.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(first))) {
      date = first.plusMonths(months);
      date = date.withDayOfMonth(date.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(date)));
    } else {
      date = first.plusMonths(months);
    }
    if (date.equals(second)) {
      return StubType.NONE;
    }
    if (date.isAfter(second)) {
      return StubType.SHORT_END;
    }
    return StubType.LONG_END;
  }

  public static StubType getStartStubType(final ZonedDateTime[] schedule, final int paymentsPerYear) {
    return getStartStubType(schedule, paymentsPerYear, false);
  }

  public static StubType getStartStubType(final ZonedDateTime[] schedule, final int paymentsPerYear, final boolean isEOM) {
    Validate.notNull(schedule, "schedule");
    Validate.noNullElements(schedule, "schedule");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(12 % paymentsPerYear == 0);
    final int months = 12 / paymentsPerYear;
    final ZonedDateTime first = schedule[0];
    final ZonedDateTime second = schedule[1];
    ZonedDateTime date;
    if (isEOM && second.getDayOfMonth() == second.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(second))) {
      date = second.minusMonths(months);
      date = date.withDayOfMonth(date.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(date)));
    } else {
      date = second.minusMonths(months);
    }
    if (date.equals(first)) {
      return StubType.NONE;
    }
    if (date.isBefore(first)) {
      return StubType.SHORT_START;
    }
    return StubType.LONG_START;
  }

  public static StubType getEndStubType(final ZonedDateTime[] schedule, final int paymentsPerYear) {
    return getEndStubType(schedule, paymentsPerYear, false);
  }

  public static StubType getEndStubType(final ZonedDateTime[] schedule, final int paymentsPerYear, final boolean isEOM) {
    Validate.notNull(schedule, "schedule");
    Validate.noNullElements(schedule, "schedule");
    Validate.isTrue(paymentsPerYear > 0);
    Validate.isTrue(12 % paymentsPerYear == 0);
    final int months = 12 / paymentsPerYear;
    final int n = schedule.length;
    final ZonedDateTime first = schedule[n - 2];
    final ZonedDateTime second = schedule[n - 1];
    ZonedDateTime date;
    if (isEOM && first.getDayOfMonth() == first.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(first))) {
      date = first.plusMonths(months);
      date = date.withDayOfMonth(date.getMonthOfYear().getLastDayOfMonth(DateUtil.isLeapYear(date)));
    } else {
      date = first.plusMonths(months);
    }
    if (date.equals(second)) {
      return StubType.NONE;
    }
    if (date.isAfter(second)) {
      return StubType.SHORT_END;
    }
    return StubType.LONG_END;
  }
}
